package uk.nhs.adaptors.pss.translator.mapper.medication;

import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestIntent.ORDER;
import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestStatus.COMPLETED;

import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.buildDosage;
import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.buildDosageQuantity;
import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.buildNotes;
import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.buildPrescriptionTypeExtension;
import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.createMedicationRequestSkeleton;
import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.extractSupplyAuthorise;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04MedicationStatement;
import org.hl7.v3.RCMRMT030101UK04Prescribe;
import org.hl7.v3.TS;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@Service
@AllArgsConstructor
public class MedicationRequestOrderMapper {

    private static final String NHS_PRESCRIPTION = "NHS prescription";
    private static final String PRESCRIPTION_TYPE = "Prescription type: ";

    private final MedicationMapper medicationMapper;

    public MedicationRequest mapToOrderMedicationRequest(RCMRMT030101UK04EhrExtract ehrExtract,
        RCMRMT030101UK04MedicationStatement medicationStatement, RCMRMT030101UK04Prescribe supplyPrescribe, String practiseCode) {
        var ehrSupplyPrescribeIdExtract = extractEhrSupplyPrescribeId(supplyPrescribe);
        var inFulfillmentOfId = extractInFulfillmentOfId(supplyPrescribe);

        if (ehrSupplyPrescribeIdExtract.isPresent()) {
            var ehrSupplyPrescribeId = ehrSupplyPrescribeIdExtract.get();
            MedicationRequest medicationRequest = createMedicationRequestSkeleton(ehrSupplyPrescribeId);

            medicationRequest.addIdentifier(buildIdentifier(ehrSupplyPrescribeId, practiseCode));
            medicationRequest.setStatus(COMPLETED);
            medicationRequest.setIntent(ORDER);

            medicationRequest.addDosageInstruction(buildDosage(medicationStatement.getPertinentInformation()));
            medicationRequest.setDispenseRequest(buildDispenseRequestForPrescribe(supplyPrescribe));

            buildNotesForPrescribe(supplyPrescribe).forEach(medicationRequest::addNote);
            medicationMapper.extractMedicationReference(medicationStatement).ifPresent(medicationRequest::setMedication);
            inFulfillmentOfId.ifPresent(inFulfillmentId -> {
                var supplyAuthorise = extractSupplyAuthorise(ehrExtract, inFulfillmentId);
                buildPrescriptionTypeExtension(supplyAuthorise).ifPresent(medicationRequest::addExtension);
                medicationRequest.addBasedOn(buildMedicationRequestReference(inFulfillmentId));
            });

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

        if (supplyPrescribe.getAvailabilityTime().hasNullFlavor()) {
            dispenseRequest.setValidityPeriod(new Period());
        } else if (supplyPrescribe.getAvailabilityTime().hasValue()) {
            dispenseRequest.setValidityPeriod(buildValidityPeriod(supplyPrescribe.getAvailabilityTime()));
        }
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

    private Period buildValidityPeriod(TS timestamp) {
        return new Period().setStartElement(DateFormatUtil.parseToDateTimeType(timestamp.getValue()));
    }

    private Optional<String> extractInFulfillmentOfId(RCMRMT030101UK04Prescribe supplyPrescribe) {
        if (supplyPrescribe.hasInFulfillmentOf()
            && supplyPrescribe.getInFulfillmentOf().hasPriorMedicationRef()
            && supplyPrescribe.getInFulfillmentOf().getPriorMedicationRef().hasId()
            && supplyPrescribe.getInFulfillmentOf().getPriorMedicationRef().getId().hasRoot()) {

            return Optional.of(supplyPrescribe.getInFulfillmentOf().getPriorMedicationRef().getId().getRoot());
        }
        return Optional.empty();
    }

    private Optional<String> extractEhrSupplyPrescribeId(RCMRMT030101UK04Prescribe supplyPrescribe) {
        if (supplyPrescribe.hasId() && supplyPrescribe.getId().hasRoot()) {
            return Optional.of(supplyPrescribe.getId().getRoot());
        }
        return Optional.empty();
    }

}
