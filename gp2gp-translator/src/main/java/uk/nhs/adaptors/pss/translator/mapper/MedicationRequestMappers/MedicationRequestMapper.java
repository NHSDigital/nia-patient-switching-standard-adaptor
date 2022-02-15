package uk.nhs.adaptors.pss.translator.mapper.MedicationRequestMappers;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.RCMRMT030101UK04Component2;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04MedicationStatement;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@Service
@AllArgsConstructor
public class MedicationRequestMapper {
    private MedicationMapper medicationMapper;
    private MedicationRequestOrderMapper medicationRequestOrderMapper;
    private MedicationRequestPlanMapper medicationRequestPlanMapper;
    private MedicationStatementMapper medicationStatementMapper;

    public void mapMedicationStatement(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04EhrComposition ehrComposition,
        RCMRMT030101UK04MedicationStatement medicationStatement, Date ehrExtractAvailabilityTime, Patient subject, Encounter context) {

        var authoredOn = extractAuthoredOn(ehrComposition, ehrExtractAvailabilityTime);
        var requester = extractRequester(ehrComposition, medicationStatement);
        var recorder = extractRecorder(ehrComposition, medicationStatement);

        List<Medication> medications = medicationStatement.getConsumable()
            .stream()
            .map(medicationMapper::createMedication)
            .collect(Collectors.toList());

        List<MedicationRequest> medicationRequestsOrder = medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component2::hasEhrSupplyPrescribe)
            .map(RCMRMT030101UK04Component2::getEhrSupplyPrescribe)
            .map(supplyPrescribe -> medicationRequestOrderMapper.mapToOrderMedicationRequest(medicationStatement, supplyPrescribe, subject, context))
            .filter(Objects::nonNull)
            .peek(medicationRequest -> medicationRequest.setAuthoredOnElement(authoredOn))
            .peek(medicationRequest -> requester.map(MedicationRequest.MedicationRequestRequesterComponent::new).ifPresent(medicationRequest::setRequester))
            .peek(medicationRequest -> recorder.ifPresent(medicationRequest::setRecorder))
            .collect(Collectors.toList());

        List<MedicationRequest> medicationRequestsPlan = medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component2::hasEhrSupplyAuthorise)
            .map(RCMRMT030101UK04Component2::getEhrSupplyAuthorise)
            .map(supplyAuthorise -> medicationRequestPlanMapper.mapToPlanMedicationRequest(ehrExtract, medicationStatement, supplyAuthorise, subject, context))
            .filter(Objects::nonNull)
            .peek(medicationRequest -> medicationRequest.setAuthoredOnElement(authoredOn))
            .peek(medicationRequest -> requester.map(MedicationRequest.MedicationRequestRequesterComponent::new).ifPresent(medicationRequest::setRequester))
            .peek(medicationRequest -> recorder.ifPresent(medicationRequest::setRecorder))
            .collect(Collectors.toList());

        List<MedicationStatement> medicationStatements = medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component2::hasEhrSupplyAuthorise)
            .map(RCMRMT030101UK04Component2::getEhrSupplyAuthorise)
            .map(supplyAuthorise -> medicationStatementMapper.mapToMedicationStatement(medicationStatement, supplyAuthorise, subject, context))
            .filter(Objects::nonNull)
            .map(medicationStatement1 -> medicationStatement1.setEffective(authoredOn))
            .map(medicationStatement1 -> medicationStatement1.setDateAssertedElement(authoredOn))
            .collect(Collectors.toList());
    }

    private DateTimeType extractAuthoredOn(RCMRMT030101UK04EhrComposition ehrComposition, Date ehrExtractAvailabilityTime) {
        if (ehrComposition.hasAuthor() && ehrComposition.getAuthor().hasTime() && ehrComposition.getAuthor().getTime().hasValue()) {
            return DateFormatUtil.parseToDateTimeType(ehrComposition.getAuthor().getTime().getValue());
        } else {
            return new DateTimeType(ehrExtractAvailabilityTime);
        }
    }

    private Optional<Reference> extractRequester(RCMRMT030101UK04EhrComposition ehrComposition,
        RCMRMT030101UK04MedicationStatement medicationStatement) {
        if (medicationStatement.hasParticipant()) {
            var pprfRequester = medicationStatement.getParticipant()
                .stream()
                .filter(participant -> !participant.hasNullFlavour())
                .filter(participant -> participant.getTypeCode().contains("PPRF") || participant.getTypeCode().contains("PRF"))
                .findFirst();
            if (pprfRequester.isPresent()) {
                return pprfRequester
                    .map(requester -> new IdType(ResourceType.Practitioner.name(), requester.getAgentRef().getId().getRoot()))
                    .map(Reference::new);
            }
        }

        if (ehrComposition.hasParticipant2()) {
            var requester = ehrComposition.getParticipant2()
                .stream()
                .filter(participant -> !participant.hasNullFlavor())
                .findFirst();

            if (requester.isPresent()) {
                return requester
                    .map(requester1 -> new IdType(ResourceType.Practitioner.name(), requester1.getAgentRef().getId().getRoot()))
                    .map(Reference::new);
            }
        }
        return Optional.empty();
    }

    private Optional<Reference> extractRecorder(RCMRMT030101UK04EhrComposition ehrComposition,
        RCMRMT030101UK04MedicationStatement medicationStatement) {
        // todo: return NIAD-2026 empty practitioner if empty
        return extractRequester(ehrComposition, medicationStatement);
    }
}
