package uk.nhs.adaptors.pss.translator.mapper.medication;

import static uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors.extractAllMedications;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import org.hl7.v3.*;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.mapper.AbstractMapper;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@Service
@AllArgsConstructor
public class MedicationRequestMapper extends AbstractMapper<DomainResource> {
    private MedicationMapper medicationMapper;
    private MedicationRequestOrderMapper medicationRequestOrderMapper;
    private MedicationRequestPlanMapper medicationRequestPlanMapper;
    private MedicationStatementMapper medicationStatementMapper;
    private MedicationMapperContext medicationMapperContext;

    private static final String PPRF = "PPRF";
    private static final String PRF = "PRF";

    public List<DomainResource> mapResources(RCMRMT030101UKEhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        String practiseCode) {
        try {
            return mapEhrExtractToFhirResource(ehrExtract, (extract, composition, component) ->
                    extractAllMedications(component)
                            .filter(Objects::nonNull)
                            .flatMap(medicationStatement -> mapMedicationStatement(
                                    ehrExtract, composition, medicationStatement, patient, encounters, practiseCode)))
                    .toList();
        } finally {
            medicationMapperContext.reset();
        }
    }

    private Stream<DomainResource> mapMedicationStatement(RCMRMT030101UKEhrExtract ehrExtract,
                                                          RCMRMT030101UKEhrComposition ehrComposition,
                                                          RCMRMT030101UKMedicationStatement medicationStatement,
                                                          Patient subject, List<Encounter> encounters, String practiseCode) {

        var context = encounters.stream()
            .filter(encounter1 -> encounter1.getId().equals(ehrComposition.getId().getRoot())).findFirst();
        var authoredOn = getAuthoredOn(medicationStatement.getAvailabilityTime(), ehrComposition);
        var dateAsserted = extractDateAsserted(ehrComposition, ehrExtract);
        var requester = extractRequester(ehrComposition, medicationStatement);
        var recorder = extractRecorder(ehrComposition, medicationStatement);

        List<Medication> medications = mapMedications(medicationStatement);

        List<MedicationRequest> medicationRequestsOrder = mapMedicationRequestsOrder(ehrExtract, medicationStatement, practiseCode);

        List<MedicationRequest> medicationRequestsPlan = mapMedicationRequestsPlan(ehrExtract, medicationStatement, practiseCode);

        List<MedicationStatement> medicationStatements = mapMedicationStatements(ehrExtract, medicationStatement, context, subject,
            authoredOn, practiseCode, dateAsserted);

        return Stream.of(medications, medicationRequestsOrder, medicationRequestsPlan, medicationStatements)
            .flatMap(List::stream)
            .map(DomainResource.class::cast)
            .map(medicationRequest -> setCommonFields(
                medicationRequest, requester, recorder, subject, context, authoredOn));
    }

    private DomainResource setCommonFields(DomainResource resource, Optional<Reference> requester,
        Optional<Reference> recorder, Patient patient, Optional<Encounter> context, DateTimeType authoredOn) {

        if (ResourceType.MedicationRequest.equals(resource.getResourceType())) {
            ((MedicationRequest) resource).setSubject(new Reference(patient));
            context.ifPresent(context1 -> ((MedicationRequest) resource).setContext(new Reference(context1)));

            requester.map(MedicationRequest.MedicationRequestRequesterComponent::new)
                .ifPresent(((MedicationRequest) resource)::setRequester);
            recorder.ifPresent(((MedicationRequest) resource)::setRecorder);
            ((MedicationRequest) resource).setAuthoredOnElement(authoredOn);
        }

        return resource;
    }


    private DateTimeType getAuthoredOn(TS availabilityTime,
                                       RCMRMT030101UKEhrComposition ehrComposition) {
        if (availabilityTime != null && availabilityTime.hasValue()) {
            return DateFormatUtil.parseToDateTimeType(availabilityTime.getValue());
        } else {
            if (ehrComposition.getAvailabilityTime() != null && ehrComposition.getAvailabilityTime().hasValue()) {
                return DateFormatUtil.parseToDateTimeType(ehrComposition.getAvailabilityTime().getValue());
            }
            if (ehrComposition.hasAuthor() && ehrComposition.getAuthor().hasTime() && ehrComposition.getAuthor().getTime().hasValue()) {
                return DateFormatUtil.parseToDateTimeType(ehrComposition.getAuthor().getTime().getValue());
            }

        }
    }

    private DateTimeType extractDateAsserted(RCMRMT030101UKEhrComposition ehrComposition, RCMRMT030101UKEhrExtract ehrExtract) {

        if (ehrComposition.hasAuthor() && ehrComposition.getAuthor().hasTime() && ehrComposition.getAuthor().getTime().hasValue()) {
            return DateFormatUtil.parseToDateTimeType(ehrComposition.getAuthor().getTime().getValue());
        } else {
            if (ehrExtract.getAvailabilityTime().hasValue()) {
                return DateFormatUtil.parseToDateTimeType(ehrExtract.getAvailabilityTime().getValue());
            }
            return null;
        }
    }

    private List<Medication> mapMedications(RCMRMT030101UKMedicationStatement medicationStatement) {
        return medicationStatement.getConsumable()
            .stream()
            .map(medicationMapper::createMedication)
            .filter(Objects::nonNull)
            .toList();
    }

    private List<MedicationRequest> mapMedicationRequestsOrder(RCMRMT030101UKEhrExtract ehrExtract,
                                                               RCMRMT030101UKMedicationStatement medicationStatement,
                                                               String practiseCode) {

        return medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UKComponent2::hasEhrSupplyPrescribe)
            .map(RCMRMT030101UKComponent2::getEhrSupplyPrescribe)
            .map(supplyPrescribe -> medicationRequestOrderMapper.mapToOrderMedicationRequest(ehrExtract, medicationStatement,
                supplyPrescribe, practiseCode))
            .filter(Objects::nonNull)
            .toList();
    }

    private List<MedicationRequest> mapMedicationRequestsPlan(RCMRMT030101UKEhrExtract ehrExtract,
        RCMRMT030101UKMedicationStatement medicationStatement, String practiseCode) {

        return medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UKComponent2::hasEhrSupplyAuthorise)
            .map(RCMRMT030101UKComponent2::getEhrSupplyAuthorise)
            .map(supplyAuthorise -> medicationRequestPlanMapper.mapToPlanMedicationRequest(ehrExtract, medicationStatement,
                supplyAuthorise, practiseCode))
            .filter(Objects::nonNull)
            .toList();
    }

    private List<MedicationStatement> mapMedicationStatements(RCMRMT030101UKEhrExtract ehrExtract,
        RCMRMT030101UKMedicationStatement medicationStatement, Optional<Encounter> context, Patient subject,
        DateTimeType authoredOn, String practiseCode, DateTimeType dateAsserted) {

        return medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UKComponent2::hasEhrSupplyAuthorise)
            .map(RCMRMT030101UKComponent2::getEhrSupplyAuthorise)
            .map(supplyAuthorise -> medicationStatementMapper
                .mapToMedicationStatement(ehrExtract, medicationStatement, supplyAuthorise, practiseCode, authoredOn))
            .filter(Objects::nonNull)
            .peek(medicationStatement1 -> {
                context.ifPresent(context1 -> medicationStatement1.setContext(new Reference(context1)));
                medicationStatement1.setSubject(new Reference(subject));
                medicationStatement1.setDateAssertedElement(dateAsserted);
            })
            .toList();
    }

    private Optional<Reference> extractRequester(RCMRMT030101UKEhrComposition ehrComposition,
        RCMRMT030101UKMedicationStatement medicationStatement) {

        if (medicationStatement.hasParticipant()) {
            var pprfRequester = medicationStatement.getParticipant()
                .stream()
                .filter(participant -> !participant.hasNullFlavour())
                .filter(participant -> participant.getTypeCode().stream().anyMatch(PPRF::equals)
                    || participant.getTypeCode().stream().anyMatch(PRF::equals))
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

    private Optional<Reference> extractRecorder(RCMRMT030101UKEhrComposition ehrComposition,
                                                RCMRMT030101UKMedicationStatement medicationStatement) {

        return extractRequester(ehrComposition, medicationStatement);
    }
}
