package uk.nhs.adaptors.pss.translator.mapper.medication;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Dosage;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.v3.PQ;
import org.hl7.v3.RCMRMT030101UK04Authorise;
import org.hl7.v3.RCMRMT030101UK04Component2;
import org.hl7.v3.RCMRMT030101UK04MedicationDosage;
import org.hl7.v3.RCMRMT030101UK04MedicationStatement;
import org.hl7.v3.RCMRMT030101UK04PertinentInformation;
import org.hl7.v3.RCMRMT030101UK04PertinentInformation2;
import org.hl7.v3.RCMRMT030101UK04SupplyAnnotation;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MedicationMapperUtils {

    private static final String META_PROFILE = "MedicationRequest";
    private static final String ACUTE = "Acute";
    private static final String REPEAT = "Repeat";
    private static final String NO_INFORMATION_AVAILABLE = "No Information available";
    private static final String PRESCRIPTION_TYPE_EXTENSION_URL
        = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-PrescriptionType-1";
    private static final String PRESCRIPTION_TYPE_CODING_SYSTEM
        = "https://fhir.nhs.uk/STU3/CodeSystem/CareConnect-PrescriptionType-1";

    protected static MedicationRequest createMedicationRequestSkeleton(String id) {
        return (MedicationRequest) new MedicationRequest()
            .setMeta(generateMeta(META_PROFILE))
            .setId(id);
    }

    protected static Optional<Extension> buildPrescriptionTypeExtension(RCMRMT030101UK04Authorise supplyAuthorise) {
        if (supplyAuthorise != null && supplyAuthorise.hasRepeatNumber() && supplyAuthorise.getRepeatNumber().getValue().intValue() == 0) {
            return Optional.of(new Extension(PRESCRIPTION_TYPE_EXTENSION_URL, new CodeableConcept(
                new Coding(PRESCRIPTION_TYPE_CODING_SYSTEM, ACUTE.toLowerCase(), ACUTE)
            )));
        } else if (supplyAuthorise != null && supplyAuthorise.hasRepeatNumber()
            && supplyAuthorise.getRepeatNumber().getValue().intValue() >= 1) {
            return Optional.of(new Extension(PRESCRIPTION_TYPE_EXTENSION_URL, new CodeableConcept(
                new Coding(PRESCRIPTION_TYPE_CODING_SYSTEM, REPEAT.toLowerCase(), REPEAT)
            )));
        }
        return Optional.empty();
    }

    protected static List<Annotation> buildNotes(List<RCMRMT030101UK04PertinentInformation2> pertinentInformationList) {
        return pertinentInformationList
            .stream()
            .filter(RCMRMT030101UK04PertinentInformation2::hasPertinentSupplyAnnotation)
            .map(RCMRMT030101UK04PertinentInformation2::getPertinentSupplyAnnotation)
            .filter(RCMRMT030101UK04SupplyAnnotation::hasText)
            .map(RCMRMT030101UK04SupplyAnnotation::getText)
            .map(text -> text + System.lineSeparator())
            .map(StringType::new)
            .map(Annotation::new)
            .collect(Collectors.toList());
    }

    protected static Dosage buildDosage(List<RCMRMT030101UK04PertinentInformation> pertinentInformationList) {
        Dosage dosage = new Dosage();
        var pertinentInformationDosage = pertinentInformationList.stream()
            .filter(RCMRMT030101UK04PertinentInformation::hasPertinentMedicationDosage)
            .map(RCMRMT030101UK04PertinentInformation::getPertinentMedicationDosage)
            .filter(RCMRMT030101UK04MedicationDosage::hasText)
            .map(RCMRMT030101UK04MedicationDosage::getText)
            .findFirst();

        pertinentInformationDosage.ifPresentOrElse(dosage::setText,
            () -> dosage.setText(NO_INFORMATION_AVAILABLE));

        return dosage;
    }

    protected static Optional<SimpleQuantity> buildDosageQuantity(PQ quantitySupplied) {
        try {
            SimpleQuantity quantity = new SimpleQuantity();
            quantity.setValue(Long.parseLong(quantitySupplied.getValue()));
            if (quantitySupplied.hasTranslation()
                && quantitySupplied.getTranslation().get(0).hasOriginalText()) {
                quantity.setUnit(quantitySupplied.getTranslation().get(0).getOriginalText());
            }
            return Optional.of(quantity);
        } catch (NumberFormatException nfe) {
            LOGGER.info("Unable to parse value from quantity supplied: {}", quantitySupplied.getValue());
            LOGGER.error(nfe.getLocalizedMessage());
        }
        return Optional.empty();
    }

    protected static Optional<String> extractEhrSupplyAuthoriseId(RCMRMT030101UK04Authorise supplyAuthorise) {
        if (supplyAuthorise.hasId() && supplyAuthorise.getId().hasRoot()) {
            return Optional.of(supplyAuthorise.getId().getRoot());
        }
        return Optional.empty();
    }

    protected static RCMRMT030101UK04Authorise extractSupplyAuthorise(RCMRMT030101UK04MedicationStatement medicationStatement, String id) {
        return medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component2::hasEhrSupplyAuthorise)
            .map(RCMRMT030101UK04Component2::getEhrSupplyAuthorise)
            .filter(authorise -> authorise.getId().getRoot().equals(id))
            .findFirst()
            .orElse(null);
    }
}
