package uk.nhs.adaptors.pss.translator.mapper.medication;

import static org.hl7.fhir.dstu3.model.MedicationStatement.MedicationStatementStatus.ACTIVE;
import static org.hl7.fhir.dstu3.model.MedicationStatement.MedicationStatementStatus.COMPLETED;
import static org.hl7.fhir.dstu3.model.MedicationStatement.MedicationStatementTaken.UNK;

import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.buildDispenseRequestPeriodEnd;
import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.buildDosage;
import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.extractEhrSupplyAuthoriseId;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.RCMRMT030101UK04Authorise;
import org.hl7.v3.RCMRMT030101UK04Component;
import org.hl7.v3.RCMRMT030101UK04Component2;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;
import org.hl7.v3.RCMRMT030101UK04MedicationStatement;
import org.hl7.v3.RCMRMT030101UK04Prescribe;
import org.hl7.v3.TS;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@Service
@AllArgsConstructor
public class MedicationStatementMapper {
    private static final String MEDICATION_STATEMENT_URL = "MedicationStatement-1";
    private static final String MS_LAST_ISSUE_DATE = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC"
        + "-MedicationStatementLastIssueDate-1";
    private static final String PRESCRIBING_AGENCY_URL
        = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-PrescribingAgency-1";

    private static final String PRESCRIBING_AGENCY_SYSTEM
            = "https://fhir.nhs.uk/STU3/CodeSystem/CareConnect-PrescribingAgency-1";

    private static final String MS_SUFFIX = "-MS";
    private static final String PRESCRIBED_CODE = "prescribed-at-gp-practice";
    private static final String PRESCRIBED_DISPLAY = "Prescribed at GP practice";
    private static final String COMPLETE = "COMPLETE";

    private final MedicationMapper medicationMapper;

    public MedicationStatement mapToMedicationStatement(RCMRMT030101UK04EhrExtract ehrExtract,
        RCMRMT030101UK04MedicationStatement medicationStatement,
        RCMRMT030101UK04Authorise supplyAuthorise, String practiseCode, DateTimeType authoredOn) {
        var ehrSupplyAuthoriseIdExtract = extractEhrSupplyAuthoriseId(supplyAuthorise);
        if (ehrSupplyAuthoriseIdExtract.isPresent()) {
            String ehrSupplyAuthoriseId = ehrSupplyAuthoriseIdExtract.get();
            MedicationStatement medicationStatement1 = new MedicationStatement();

            medicationStatement1.setId(ehrSupplyAuthoriseId + MS_SUFFIX);
            medicationStatement1.setMeta(generateMeta(MEDICATION_STATEMENT_URL));
            medicationStatement1.addIdentifier(buildIdentifier(ehrSupplyAuthoriseId + MS_SUFFIX, practiseCode));
            medicationStatement1.setTaken(UNK);

            medicationStatement1.addBasedOn(new Reference(
                new IdType(ResourceType.MedicationRequest.name(), ehrSupplyAuthoriseId)
            ));
            medicationStatement1.addExtension(generatePrescribingAgencyExtension());

            medicationStatement1.setStatus(buildMedicationStatementStatus(supplyAuthorise));
            medicationStatement1.addDosage(buildDosage(medicationStatement.getPertinentInformation()));

            extractHighestSupplyPrescribeTime(ehrExtract, ehrSupplyAuthoriseId)
                .map(dateTime -> new Extension(MS_LAST_ISSUE_DATE, dateTime))
                .ifPresent(medicationStatement1::addExtension);

            medicationMapper.extractMedicationReference(medicationStatement)
                .ifPresent(medicationStatement1::setMedication);

            MedicationMapperUtils.extractDispenseRequestPeriodStart(supplyAuthorise)
                .ifPresentOrElse(dateTimeType -> {
                    medicationStatement1.setEffective(
                        buildDispenseRequestPeriodEnd(supplyAuthorise, medicationStatement).setStartElement(dateTimeType));
                }, () -> {
                    medicationStatement1.setEffective(buildDispenseRequestPeriodEnd(supplyAuthorise, medicationStatement)
                        .setStartElement(authoredOn));
                });

            return medicationStatement1;
        }
        return null;
    }

    private MedicationStatement.MedicationStatementStatus buildMedicationStatementStatus(RCMRMT030101UK04Authorise supplyAuthorise) {
        if (supplyAuthorise.hasStatusCode() && supplyAuthorise.getStatusCode().hasCode()
            && COMPLETE.equals(supplyAuthorise.getStatusCode().getCode())) {
            return COMPLETED;
        } else {
            return ACTIVE;
        }
    }

    private Optional<DateTimeType> extractHighestSupplyPrescribeTime(RCMRMT030101UK04EhrExtract ehrExtract, String id) {
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
            .filter(RCMRMT030101UK04Component2::hasEhrSupplyPrescribe)
            .map(RCMRMT030101UK04Component2::getEhrSupplyPrescribe)
            .filter(prescribe -> hasLinkedInFulfillment(prescribe, id))
            .filter(RCMRMT030101UK04Prescribe::hasAvailabilityTime)
            .map(RCMRMT030101UK04Prescribe::getAvailabilityTime)
            .filter(TS::hasValue)
            .map(TS::getValue)
            .map(DateFormatUtil::parseToDateTimeType)
            .max(Comparator.comparing(DateTimeType::getValue));
    }

    private Extension generatePrescribingAgencyExtension() {
        return new Extension(PRESCRIBING_AGENCY_URL, new CodeableConcept(
            new Coding(PRESCRIBING_AGENCY_SYSTEM, PRESCRIBED_CODE, PRESCRIBED_DISPLAY)
        ));
    }

    private boolean hasLinkedInFulfillment(RCMRMT030101UK04Prescribe prescribe, String id) {
        return prescribe.hasInFulfillmentOf() && prescribe.getInFulfillmentOf().hasPriorMedicationRef()
            && prescribe.getInFulfillmentOf().getPriorMedicationRef().hasId()
            && prescribe.getInFulfillmentOf().getPriorMedicationRef().getId().hasRoot()
            && prescribe.getInFulfillmentOf().getPriorMedicationRef().getId().getRoot().equals(id);
    }
}
