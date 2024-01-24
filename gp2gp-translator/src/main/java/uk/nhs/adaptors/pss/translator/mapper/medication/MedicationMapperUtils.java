package uk.nhs.adaptors.pss.translator.mapper.medication;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Dosage;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.v3.II;
import org.hl7.v3.PQ;
import org.hl7.v3.RCMRMT030101UKAuthorise;
import org.hl7.v3.RCMRMT030101UK04Component;
import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04Component2;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04Discontinue;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;
import org.hl7.v3.RCMRMT030101UK04MedicationRef;
import org.hl7.v3.RCMRMT030101UK04MedicationStatement;
import org.hl7.v3.RCMRMT030101UKPertinentInformation;
import org.hl7.v3.RCMRMT030101UKPertinentInformation2;
import org.hl7.v3.RCMRMT030101UK04ReversalOf;
import org.hl7.v3.RCMRMT030101UKMedicationDosage;

import lombok.extern.slf4j.Slf4j;
import org.hl7.v3.RCMRMT030101UKSupplyAnnotation;
import uk.nhs.adaptors.pss.translator.util.CompoundStatementUtil;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MedicationMapperUtils {

    private static final String META_PROFILE = "MedicationRequest-1";
    private static final String ACUTE = "Acute";
    private static final String REPEAT = "Repeat";
    private static final String NO_INFORMATION_AVAILABLE = "No Information available";
    private static final String PRESCRIPTION_TYPE_EXTENSION_URL
        = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-PrescriptionType-1";
    private static final String PRESCRIPTION_TYPE_CODING_SYSTEM
        = "https://fhir.nhs.uk/STU3/CodeSystem/CareConnect-PrescriptionType-1";

    public static MedicationRequest createMedicationRequestSkeleton(String id) {
        return (MedicationRequest) new MedicationRequest()
            .setMeta(generateMeta(META_PROFILE))
            .setId(id);
    }

    public static Optional<Extension> buildPrescriptionTypeExtension(RCMRMT030101UKAuthorise supplyAuthorise) {
        if (supplyAuthorise != null && supplyAuthorise.hasRepeatNumber() && supplyAuthorise.getRepeatNumber().getValue().intValue() == 0) {
            return Optional.of(new Extension(PRESCRIPTION_TYPE_EXTENSION_URL, new CodeableConcept(
                new Coding(PRESCRIPTION_TYPE_CODING_SYSTEM, ACUTE.toLowerCase(), ACUTE)
            )));
        }
        return Optional.of(new Extension(PRESCRIPTION_TYPE_EXTENSION_URL, new CodeableConcept(
            new Coding(PRESCRIPTION_TYPE_CODING_SYSTEM, REPEAT.toLowerCase(), REPEAT)
        )));
    }

    public static List<Annotation> buildNotes(List<RCMRMT030101UKPertinentInformation2> pertinentInformationList) {
        return pertinentInformationList
            .stream()
            .filter(RCMRMT030101UKPertinentInformation2::hasPertinentSupplyAnnotation)
            .map(RCMRMT030101UKPertinentInformation2::getPertinentSupplyAnnotation)
            .filter(RCMRMT030101UKSupplyAnnotation::hasText)
            .map(RCMRMT030101UKSupplyAnnotation::getText)
            .map(text -> text + System.lineSeparator())
            .map(StringType::new)
            .map(Annotation::new)
            .collect(Collectors.toList());
    }

    public static Dosage buildDosage(List<RCMRMT030101UKPertinentInformation> pertinentInformationList) {
        Dosage dosage = new Dosage();
        var pertinentInformationDosage = pertinentInformationList.stream()
            .filter(RCMRMT030101UKPertinentInformation::hasPertinentMedicationDosage)
            .map(RCMRMT030101UKPertinentInformation::getPertinentMedicationDosage)
            .filter(RCMRMT030101UKMedicationDosage::hasText)
            .map(RCMRMT030101UKMedicationDosage::getText)
            .findFirst();

        pertinentInformationDosage.ifPresentOrElse(dosage::setText,
            () -> dosage.setText(NO_INFORMATION_AVAILABLE));

        return dosage;
    }

    public static Optional<SimpleQuantity> buildDosageQuantity(PQ quantitySupplied) {
        SimpleQuantity quantity = new SimpleQuantity();
        quantity.setValue(Double.parseDouble(quantitySupplied.getValue()));
        if (quantitySupplied.hasTranslation()
            && quantitySupplied.getTranslation().get(0).hasOriginalText()) {
            quantity.setUnit(quantitySupplied.getTranslation().get(0).getOriginalText());
        }
        return Optional.of(quantity);
    }

    public static Optional<String> extractEhrSupplyAuthoriseId(RCMRMT030101UKAuthorise supplyAuthorise) {
        if (supplyAuthorise.hasId() && supplyAuthorise.getId().hasRoot()) {
            return Optional.of(supplyAuthorise.getId().getRoot());
        }
        return Optional.empty();
    }

    public static RCMRMT030101UKAuthorise extractSupplyAuthorise(RCMRMT030101UK04EhrExtract ehrExtract, String id) {
        return ehrExtract.getComponent()
            .stream()
            .map(RCMRMT030101UK04Component::getEhrFolder)
            .map(RCMRMT030101UK04EhrFolder::getComponent)
            .flatMap(List::stream)
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .map(RCMRMT030101UK04EhrComposition::getComponent)
            .flatMap(List::stream)
            .flatMap(MedicationMapperUtils::extractAllMedications)
            .filter(Objects::nonNull)
            .map(RCMRMT030101UK04MedicationStatement::getComponent)
            .flatMap(List::stream)
            .map(RCMRMT030101UK04Component2::getEhrSupplyAuthorise)
            .filter(Objects::nonNull)
            .filter(authorise -> authorise.getId().getRoot().equals(id))
            .findFirst()
            .orElse(null);
    }

    public static Optional<DateTimeType> extractDispenseRequestPeriodStart(RCMRMT030101UKAuthorise supplyAuthorise) {
        if (supplyAuthorise.hasEffectiveTime() && supplyAuthorise.getEffectiveTime().hasCenter()
            && supplyAuthorise.getEffectiveTime().getCenter().hasValue()) {
            return Optional.of(DateFormatUtil.parseToDateTimeType(supplyAuthorise.getEffectiveTime().getCenter().getValue()));
        }
        if (supplyAuthorise.hasEffectiveTime() && supplyAuthorise.getEffectiveTime().hasLow()
            && supplyAuthorise.getEffectiveTime().getLow().hasValue()) {
            return Optional.of(DateFormatUtil.parseToDateTimeType(supplyAuthorise.getEffectiveTime().getLow().getValue()));
        }
        if (supplyAuthorise.hasAvailabilityTime() && supplyAuthorise.getAvailabilityTime().hasValue()) {
            return Optional.of(DateFormatUtil.parseToDateTimeType(supplyAuthorise.getAvailabilityTime().getValue()));
        }
        return Optional.empty();
    }

    public static Stream<RCMRMT030101UK04MedicationStatement> extractAllMedications(RCMRMT030101UK04Component4 component4) {
        return Stream.concat(
            Stream.of(component4.getMedicationStatement()),
            extractNestedMedications(component4)
        );
    }

    public static Period buildDispenseRequestPeriodEnd(RCMRMT030101UKAuthorise supplyAuthorise,
                                                       RCMRMT030101UK04MedicationStatement medicationStatement) {

        if (supplyAuthorise.hasEffectiveTime() && supplyAuthorise.getEffectiveTime().hasHigh()) {

            String effectiveTimeValue = supplyAuthorise.getEffectiveTime().getHigh().getValue();
            String effectiveTimeValueSuffix = "000000"; //GP2GP can malform the value and add this suffix.

            String effectiveTimeEnd =
                    effectiveTimeValue.endsWith(effectiveTimeValueSuffix)
                        ? effectiveTimeValue.substring(0, effectiveTimeValue.length() - effectiveTimeValueSuffix.length())
                                : effectiveTimeValue;

            return new Period().setEndElement(
                DateFormatUtil.parseToDateTimeType(effectiveTimeEnd));
        }

        if (medicationStatement.hasEffectiveTime() && medicationStatement.getEffectiveTime().hasHigh()) {
            return new Period().setEndElement(
                DateFormatUtil.parseToDateTimeType(medicationStatement.getEffectiveTime().getHigh().getValue()));
        }
        return new Period();
    }

    public static Optional<Period> buildMedicationStatementEffectivePeriodEnd(RCMRMT030101UK04Discontinue supplyDiscontinue) {
        if (supplyDiscontinue.hasAvailabilityTime() && supplyDiscontinue.getAvailabilityTime().hasValue()) {
            return Optional.of(new Period().setEndElement(
               DateFormatUtil.parseToDateTimeType(supplyDiscontinue.getAvailabilityTime().getValue())
            ));
        }

        return Optional.empty();
    }

    public static Optional<RCMRMT030101UK04Discontinue> extractMatchingDiscontinue(String supplyAuthoriseId,
        RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component::hasEhrFolder)
            .map(RCMRMT030101UK04Component::getEhrFolder)
            .map(RCMRMT030101UK04EhrFolder::getComponent)
            .flatMap(List::stream)
            .filter(RCMRMT030101UK04Component3::hasEhrComposition)
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .map(RCMRMT030101UK04EhrComposition::getComponent)
            .flatMap(List::stream)
            .filter(RCMRMT030101UK04Component4::hasMedicationStatement)
            .map(RCMRMT030101UK04Component4::getMedicationStatement)
            .map(RCMRMT030101UK04MedicationStatement::getComponent)
            .flatMap(List::stream)
            .filter(RCMRMT030101UK04Component2::hasEhrSupplyDiscontinue)
            .map(RCMRMT030101UK04Component2::getEhrSupplyDiscontinue)
            .filter(discontinue1 -> hasReversalIdMatchingAuthorise(discontinue1.getReversalOf(), supplyAuthoriseId))
            .findFirst();
    }

    private static boolean hasReversalIdMatchingAuthorise(List<RCMRMT030101UK04ReversalOf> reversalOf, String supplyAuthoriseId) {
        return reversalOf.stream()
            .map(RCMRMT030101UK04ReversalOf::getPriorMedicationRef)
            .map(RCMRMT030101UK04MedicationRef::getId)
            .map(II::getRoot)
            .anyMatch(supplyAuthoriseId::equals);
    }

    public static List<RCMRMT030101UK04MedicationStatement> getMedicationStatements(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent()
            .stream()
            .map(RCMRMT030101UK04Component::getEhrFolder)
            .map(RCMRMT030101UK04EhrFolder::getComponent)
            .flatMap(List::stream)
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .map(RCMRMT030101UK04EhrComposition::getComponent)
            .flatMap(List::stream)
            .flatMap(MedicationMapperUtils::extractAllMedications)
            .filter(Objects::nonNull)
            .toList();
    }

    private static Stream<RCMRMT030101UK04MedicationStatement> extractNestedMedications(RCMRMT030101UK04Component4 component4) {
        return component4.hasCompoundStatement()
            ? CompoundStatementUtil.extractResourcesFromCompound(component4.getCompoundStatement(),
                RCMRMT030101UK04Component02::hasMedicationStatement, RCMRMT030101UK04Component02::getMedicationStatement)
            .stream()
            .map(RCMRMT030101UK04MedicationStatement.class::cast)
            : Stream.empty();
    }
}
