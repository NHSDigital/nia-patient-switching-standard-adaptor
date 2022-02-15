package uk.nhs.adaptors.pss.translator.mapper.medication;

import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapper.extractMedicationReference;
import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.buildDosage;
import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.buildDosageQuantity;
import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.buildNotes;
import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.buildPrescriptionTypeExtension;
import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.buildValidityPeriod;
import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.createMedicationRequestSkeleton;
import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.extractSupplyAuthorise;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.v3.RCMRMT030101UK04MedicationStatement;
import org.hl7.v3.RCMRMT030101UK04Prescribe;

public class MedicationRequestOrderMapper {

    private static final String NHS_PRESCRIPTION = "NHS prescription";
    private static final String PRESCRIPTION_TYPE = "Prescription type: ";

    protected MedicationRequest mapToOrderMedicationRequest(RCMRMT030101UK04MedicationStatement medicationStatement,
        RCMRMT030101UK04Prescribe supplyPrescribe, Patient subject, Encounter context) {
        var ehrSupplyPrescribeIdExtract = extractEhrSupplyPrescribeId(supplyPrescribe);
        if (ehrSupplyPrescribeIdExtract.isPresent()) {
            var ehrSupplyPrescribeId = ehrSupplyPrescribeIdExtract.get();
            var supplyAuthorise = extractSupplyAuthorise(medicationStatement, ehrSupplyPrescribeId);
            MedicationRequest medicationRequest = createMedicationRequestSkeleton(
                supplyAuthorise, subject, context, ehrSupplyPrescribeId
            );

            medicationRequest.addIdentifier(buildIdentifier(ehrSupplyPrescribeId, ""));
            medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.COMPLETED);
            medicationRequest.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);

            medicationRequest.addExtension(buildPrescriptionTypeExtension(supplyAuthorise));
            medicationRequest.addBasedOn(buildMedicationRequestReference(ehrSupplyPrescribeId));
            medicationRequest.addDosageInstruction(buildDosage(medicationStatement));
            medicationRequest.setDispenseRequest(buildDispenseRequestForPrescribe(supplyPrescribe));

            buildNotesForPrescribe(supplyPrescribe).forEach(medicationRequest::addNote);
            extractMedicationReference(medicationStatement).ifPresent(medicationRequest::setMedication);

            return medicationRequest;
        }
        return null;
    }

    private Reference buildMedicationRequestReference(String id) {
        return new Reference(new IdType(ResourceType.MedicationRequest.name(), id));
    }

    private MedicationRequest.MedicationRequestDispenseRequestComponent buildDispenseRequestForPrescribe(
        RCMRMT030101UK04Prescribe supplyPrescribe) {
        MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest
            = new MedicationRequest.MedicationRequestDispenseRequestComponent();

        if (supplyPrescribe.hasQuantity()) {
            buildDosageQuantity(supplyPrescribe.getQuantity()).ifPresent(dispenseRequest::setQuantity);
        }
        dispenseRequest.setValidityPeriod(buildValidityPeriod(supplyPrescribe.getAvailabilityTime()));

        return dispenseRequest;
    }

    private List<Annotation> buildNotesForPrescribe(RCMRMT030101UK04Prescribe supplyPrescribe) {
        var notes = buildNotes(supplyPrescribe.getPertinentInformation());
        if (supplyPrescribe.hasCode() && supplyPrescribe.getCode().hasDisplayName()
            && !NHS_PRESCRIPTION.equalsIgnoreCase(supplyPrescribe.getCode().getDisplayName())) {
            notes.add(new Annotation(
                new StringType(PRESCRIPTION_TYPE + supplyPrescribe.getCode().getDisplayName())
            ));
        }
        return notes;
    }

    private Optional<String> extractEhrSupplyPrescribeId(RCMRMT030101UK04Prescribe supplyPrescribe) {
        if (supplyPrescribe.hasInFulfillmentOf()
            && supplyPrescribe.getInFulfillmentOf().hasPriorMedicationRef()
            && supplyPrescribe.getInFulfillmentOf().getPriorMedicationRef().hasId()
            && supplyPrescribe.getInFulfillmentOf().getPriorMedicationRef().getId().hasRoot()) {

            return Optional.of(supplyPrescribe.getInFulfillmentOf().getPriorMedicationRef().getId().getRoot());
        }
        return Optional.empty();
    }

}
