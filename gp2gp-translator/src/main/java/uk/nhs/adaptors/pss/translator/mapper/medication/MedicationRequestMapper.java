package uk.nhs.adaptors.pss.translator.mapper.medication;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.RCMRMT030101UK04Authorise;
import org.hl7.v3.RCMRMT030101UK04Component;
import org.hl7.v3.RCMRMT030101UK04Component2;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;
import org.hl7.v3.RCMRMT030101UK04MedicationStatement;
import org.hl7.v3.RCMRMT030101UK04Prescribe;
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

    public List<DomainResource> mapResources(RCMRMT030101UK04EhrExtract ehrExtract, List<Encounter> encounters,
        List<Reference> references, Patient patient) {

        List<RCMRMT030101UK04EhrComposition> ehrCompositions = ehrExtract.getComponent()
            .stream()
            .map(RCMRMT030101UK04Component::getEhrFolder)
            .map(RCMRMT030101UK04EhrFolder::getComponent)
            .flatMap(List::stream)
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .collect(Collectors.toList());

        List<DomainResource> medicationResources = new ArrayList<>();
        for (RCMRMT030101UK04EhrComposition ehrComposition : ehrCompositions) {
            var context = encounters.stream()
                .filter(encounter1 -> encounter1.getId().equals(ehrComposition.getId().getRoot())).findFirst();

            ehrComposition.getComponent()
                .stream()
                .filter(RCMRMT030101UK04Component4::hasMedicationStatement)
                .map(RCMRMT030101UK04Component4::getMedicationStatement)
                .map(medicationStatement -> mapMedicationStatement(ehrExtract, ehrComposition, medicationStatement, patient, context))
                .flatMap(List::stream)
                .forEach(medicationResources::add);
        }
        return medicationResources;
    }

    private List<DomainResource> mapMedicationStatement(RCMRMT030101UK04EhrExtract ehrExtract,
        RCMRMT030101UK04EhrComposition ehrComposition, RCMRMT030101UK04MedicationStatement medicationStatement,
        Patient subject, Optional<Encounter> context) {

        var authoredOn = extractAuthoredOn(ehrComposition,
            DateFormatUtil.parseToDateTimeType(ehrExtract.getAvailabilityTime().getValue()));
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
            .map(supplyPrescribe -> mapToOrderMedicationRequest(medicationStatement, supplyPrescribe))
            .filter(Objects::nonNull)
            .map(medicationRequest -> setCommonFields(
                medicationRequest, requester, recorder, subject, context, authoredOn
            ))
            .collect(Collectors.toList());

        List<MedicationRequest> medicationRequestsPlan = medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component2::hasEhrSupplyAuthorise)
            .map(RCMRMT030101UK04Component2::getEhrSupplyAuthorise)
            .map(supplyAuthorise -> mapToPlanMedicationRequest(ehrExtract, medicationStatement, supplyAuthorise))
            .filter(Objects::nonNull)
            .map(medicationRequest -> setCommonFields(
                medicationRequest, requester, recorder, subject, context, authoredOn
            ))
            .collect(Collectors.toList());

        List<MedicationStatement> medicationStatements = medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component2::hasEhrSupplyAuthorise)
            .map(RCMRMT030101UK04Component2::getEhrSupplyAuthorise)
            .map(supplyAuthorise -> mapToMedicationStatement(medicationStatement, supplyAuthorise))
            .filter(Objects::nonNull)
            .peek(medicationStatement1 -> {
                context.ifPresent(context1 -> medicationStatement1.setContext(new Reference(context1)));
                medicationStatement1.setSubject(new Reference(subject));
                medicationStatement1.setEffective(authoredOn);
                medicationStatement1.setDateAssertedElement(authoredOn);
            })
            .collect(Collectors.toList());

        return Stream.of(medications, medicationRequestsOrder, medicationRequestsPlan, medicationStatements)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    private MedicationStatement mapToMedicationStatement(RCMRMT030101UK04MedicationStatement medicationStatement,
        RCMRMT030101UK04Authorise supplyAuthorise) {
        return medicationStatementMapper.mapToMedicationStatement(medicationStatement, supplyAuthorise);
    }

    private MedicationRequest mapToOrderMedicationRequest(RCMRMT030101UK04MedicationStatement medicationStatement,
        RCMRMT030101UK04Prescribe supplyPrescribe) {
        return medicationRequestOrderMapper.mapToOrderMedicationRequest(medicationStatement, supplyPrescribe);
    }

    private MedicationRequest mapToPlanMedicationRequest(RCMRMT030101UK04EhrExtract ehrExtract,
        RCMRMT030101UK04MedicationStatement medicationStatement, RCMRMT030101UK04Authorise supplyAuthorise) {
        return medicationRequestPlanMapper.mapToPlanMedicationRequest(ehrExtract, medicationStatement, supplyAuthorise);
    }

    private DateTimeType extractAuthoredOn(RCMRMT030101UK04EhrComposition ehrComposition, DateTimeType ehrExtractAvailabilityTime) {
        if (ehrComposition.hasAuthor() && ehrComposition.getAuthor().hasTime() && ehrComposition.getAuthor().getTime().hasValue()) {
            return DateFormatUtil.parseToDateTimeType(ehrComposition.getAuthor().getTime().getValue());
        } else {
            return ehrExtractAvailabilityTime;
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

    private MedicationRequest setCommonFields(MedicationRequest medicationRequest, Optional<Reference> requester,
        Optional<Reference> recorder, Patient patient, Optional<Encounter> context, DateTimeType authoredOn) {

        medicationRequest.setSubject(new Reference(patient));
        context.ifPresent(context1 -> medicationRequest.setContext(new Reference(context1)));

        requester.map(MedicationRequest.MedicationRequestRequesterComponent::new).ifPresent(medicationRequest::setRequester);
        recorder.ifPresent(medicationRequest::setRecorder);
        medicationRequest.setAuthoredOnElement(authoredOn);

        return medicationRequest;
    }
}
