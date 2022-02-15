package uk.nhs.adaptors.pss.translator.mapper.MedicationRequestMappers;

import static uk.nhs.adaptors.pss.translator.mapper.MedicationRequestMappers.MedicationMapper.extractMedicationReference;
import static uk.nhs.adaptors.pss.translator.mapper.MedicationRequestMappers.MedicationMapperUtils.buildDosage;
import static uk.nhs.adaptors.pss.translator.mapper.MedicationRequestMappers.MedicationMapperUtils.extractEhrSupplyAuthoriseId;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.Date;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.RCMRMT030101UK04Authorise;
import org.hl7.v3.RCMRMT030101UK04Component2;
import org.hl7.v3.RCMRMT030101UK04MedicationStatement;
import org.hl7.v3.RCMRMT030101UK04Prescribe;
import org.hl7.v3.TS;

import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

public class MedicationStatementMapper {
    private static final String MEDICATION_STATEMENT_URL
        = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-MedicationStatement-1";
    private static final String MS_LAST_ISSUE_DATE = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-MedicationStatementLastIssueDate-1";
    private static final String PRESCRIBING_AGENCY_URL
        = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-MedicationStatusReason-1";

    private static final String MS_SUFFIX = "-MS";
    private static final String PRESCRIBED_CODE = "prescribed-at-gp-practice";
    private static final String PRESCRIBED_DISPLAY = "Prescribed at GP practice";
    private static final String COMPLETE = "COMPLETE";

    protected MedicationStatement mapToMedicationStatement(RCMRMT030101UK04MedicationStatement medicationStatement,
        RCMRMT030101UK04Authorise supplyAuthorise, Patient subject, Encounter context) {
        var ehrSupplyAuthoriseIdExtract = extractEhrSupplyAuthoriseId(supplyAuthorise);
        if (ehrSupplyAuthoriseIdExtract.isPresent()) {
            String ehrSupplyAuthoriseId = ehrSupplyAuthoriseIdExtract.get();
            MedicationStatement medicationStatement1 = new MedicationStatement();

            medicationStatement1.setId(ehrSupplyAuthoriseId + MS_SUFFIX);
            medicationStatement1.setContext(new Reference(context));
            medicationStatement1.setSubject(new Reference(subject));
            medicationStatement1.setMeta(generateMeta(MEDICATION_STATEMENT_URL));
            medicationStatement1.addIdentifier(buildIdentifier(ehrSupplyAuthoriseId + MS_SUFFIX, ""));
            medicationStatement1.setTaken(MedicationStatement.MedicationStatementTaken.UNK);

            medicationStatement1.addBasedOn(new Reference(ehrSupplyAuthoriseId));
            medicationStatement1.addExtension(generatePrescribingAgencyExtension());

            medicationStatement1.setStatus(buildMedicationStatementStatus(supplyAuthorise));
            medicationStatement1.addDosage(buildDosage(medicationStatement));

            extractHighestSupplyPrescribeTime(medicationStatement, ehrSupplyAuthoriseId)
                .map(dateTime -> new Extension(MS_LAST_ISSUE_DATE, dateTime))
                .ifPresent(medicationStatement1::addExtension);

            extractMedicationReference(medicationStatement)
                .ifPresent(medicationStatement1::setMedication);

            return medicationStatement1;
        }
        return null;
    }

    private MedicationStatement.MedicationStatementStatus buildMedicationStatementStatus(RCMRMT030101UK04Authorise supplyAuthorise) {
        if (supplyAuthorise.hasStatusCode() && supplyAuthorise.getStatusCode().hasCode()
            && COMPLETE.equals(supplyAuthorise.getStatusCode().getCode())) {
            return MedicationStatement.MedicationStatementStatus.COMPLETED;
        } else {
            return MedicationStatement.MedicationStatementStatus.ACTIVE;
        }
    }

    private Optional<DateTimeType> extractHighestSupplyPrescribeTime(RCMRMT030101UK04MedicationStatement medicationStatement, String id) {
        return medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component2::hasEhrSupplyPrescribe)
            .map(RCMRMT030101UK04Component2::getEhrSupplyPrescribe)
            .filter(prescribe -> hasLinkedInFulfillment(prescribe, id))
            .filter(RCMRMT030101UK04Prescribe::hasAvailabilityTime)
            .map(RCMRMT030101UK04Prescribe::getAvailabilityTime)
            .filter(TS::hasValue)
            .map(TS::getValue)
            .map(DateFormatUtil::parsePathwaysDate)
            .max(Date::compareTo)
            .map(DateTimeType::new);
    }

    private Extension generatePrescribingAgencyExtension() {
        Coding coding = new Coding(PRESCRIBING_AGENCY_URL, PRESCRIBED_CODE, PRESCRIBED_DISPLAY);
        CodeableConcept codeableConcept = new CodeableConcept(coding);
        return new Extension(PRESCRIBING_AGENCY_URL, codeableConcept);
    }

    private boolean hasLinkedInFulfillment(RCMRMT030101UK04Prescribe prescribe, String id) {
        return prescribe.hasInFulfillmentOf() && prescribe.getInFulfillmentOf().hasPriorMedicationRef()
            && prescribe.getInFulfillmentOf().getPriorMedicationRef().hasId()
            && prescribe.getInFulfillmentOf().getPriorMedicationRef().getId().hasRoot()
            && prescribe.getInFulfillmentOf().getPriorMedicationRef().getId().getRoot().equals(id);
    }
}
