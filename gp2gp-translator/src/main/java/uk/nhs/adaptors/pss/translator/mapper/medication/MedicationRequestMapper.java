package uk.nhs.adaptors.pss.translator.mapper.medication;

import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestIntent.ORDER;
import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestIntent.PLAN;
import static uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors.extractAllMedications;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hl7.fhir.dstu3.model.CodeableConcept;
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
import org.hl7.v3.RCMRMT030101UKComponent2;
import org.hl7.v3.RCMRMT030101UKEhrComposition;
import org.hl7.v3.RCMRMT030101UKEhrExtract;
import org.hl7.v3.RCMRMT030101UKMedicationStatement;
import org.hl7.v3.TS;
import org.jetbrains.annotations.NotNull;
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

    private static final String PRESCRIPTION_TYPE_EXTENSION_URL
        = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-PrescriptionType-1";
    private static final String PRESCRIPTION_TYPE_CODING_SYSTEM
        = "https://fhir.nhs.uk/STU3/CodeSystem/CareConnect-PrescriptionType-1";


    public List<DomainResource> mapResources(RCMRMT030101UKEhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        String practiseCode) {
        try {
            var resources = mapEhrExtractToFhirResource(ehrExtract, (extract, composition, component) ->
                extractAllMedications(component)
                    .filter(Objects::nonNull)
                    .flatMap(medicationStatement -> mapMedicationStatement(
                        ehrExtract, composition, medicationStatement, patient, encounters, practiseCode)))
                .collect(Collectors.toCollection(ArrayList::new));

            var medicationRequestOrderGroups = groupOrderMedicationRequestsByBasedOnReferenceToAcutePlans(resources);

            medicationRequestOrderGroups.forEach((referencedPlanId, medicationRequestOrders) -> {
                if (medicationRequestOrders.size() > 1) {
                    for (var index = 1; index < medicationRequestOrders.size(); index++) {
                        resources.add(new MedicationRequest().setIntent(PLAN));
                    }
                }
            });

            return resources;
        } finally {
            medicationMapperContext.reset();
        }
    }

    private @NotNull Map<String, List<MedicationRequest>> groupOrderMedicationRequestsByBasedOnReferenceToAcutePlans(
        ArrayList<DomainResource> resources
    ) {
        var acutePlanMedicationRequestIds = resources.stream()
            .filter(MedicationRequest.class::isInstance)
            .map(MedicationRequest.class::cast)
            .filter(medicationRequest -> PLAN.equals(medicationRequest.getIntent()))
            .filter(this::isAcute)
            .map(MedicationRequest::getId)
            .toList();

        return resources.stream()
            .filter(MedicationRequest.class::isInstance)
            .map(MedicationRequest.class::cast)
            .filter(medicationRequest -> ORDER.equals(medicationRequest.getIntent()))
            .flatMap(medicationRequest -> medicationRequest.getBasedOn()
                .stream()
                .map(reference ->
                    new AbstractMap.SimpleEntry<>(reference.getReference().split("/")[1], medicationRequest)
                )
            )
            .filter(entry -> acutePlanMedicationRequestIds.contains(entry.getKey()))
            .collect(
                Collectors.groupingBy(
                    Map.Entry::getKey,
                    Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                )
            );
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

        List<MedicationRequest> medicationRequestsOrder = mapMedicationRequestsOrder(
            ehrExtract,
            ehrComposition,
            medicationStatement,
            practiseCode
        );

        List<MedicationRequest> medicationRequestsPlan = mapMedicationRequestsPlan(
            ehrExtract,
            ehrComposition,
            medicationStatement,
            practiseCode
        );

        List<MedicationStatement> medicationStatements = mapMedicationStatements(
            ehrExtract,
            ehrComposition,
            medicationStatement,
            context,
            subject,
            authoredOn,
            practiseCode,
            dateAsserted
        );

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

        return null;
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
                                                               RCMRMT030101UKEhrComposition ehrComposition,
                                                               RCMRMT030101UKMedicationStatement medicationStatement,
                                                               String practiseCode) {

        return medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UKComponent2::hasEhrSupplyPrescribe)
            .map(RCMRMT030101UKComponent2::getEhrSupplyPrescribe)
            .map(supplyPrescribe -> medicationRequestOrderMapper.mapToOrderMedicationRequest(
                ehrExtract,
                ehrComposition,
                medicationStatement,
                supplyPrescribe,
                practiseCode)
            ).filter(Objects::nonNull)
            .toList();
    }

    private List<MedicationRequest> mapMedicationRequestsPlan(RCMRMT030101UKEhrExtract ehrExtract,
        RCMRMT030101UKEhrComposition ehrComposition,
        RCMRMT030101UKMedicationStatement medicationStatement, String practiseCode) {

        return medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UKComponent2::hasEhrSupplyAuthorise)
            .map(RCMRMT030101UKComponent2::getEhrSupplyAuthorise)
            .map(supplyAuthorise -> medicationRequestPlanMapper.mapToPlanMedicationRequest(ehrExtract, ehrComposition, medicationStatement,
                supplyAuthorise, practiseCode))
            .filter(Objects::nonNull)
            .toList();
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private List<MedicationStatement> mapMedicationStatements(RCMRMT030101UKEhrExtract ehrExtract,
          RCMRMT030101UKEhrComposition ehrComposition,
          RCMRMT030101UKMedicationStatement medicationStatement, Optional<Encounter> context, Patient subject,
          DateTimeType authoredOn, String practiseCode, DateTimeType dateAsserted) {

        return medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UKComponent2::hasEhrSupplyAuthorise)
            .map(RCMRMT030101UKComponent2::getEhrSupplyAuthorise)
            .map(supplyAuthorise -> medicationStatementMapper
                .mapToMedicationStatement(ehrExtract, ehrComposition, medicationStatement, supplyAuthorise, practiseCode, authoredOn))
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

    private boolean isAcute(MedicationRequest medicationRequest) {

        if (medicationRequest.hasExtension(PRESCRIPTION_TYPE_EXTENSION_URL)) {
            var extensionCodeableConcept = (CodeableConcept) medicationRequest
                .getExtensionByUrl(PRESCRIPTION_TYPE_EXTENSION_URL)
                .getValue();
            return (extensionCodeableConcept.hasCoding(PRESCRIPTION_TYPE_CODING_SYSTEM, "acute"));
        }

        return false;
    }
}
