package uk.nhs.adaptors.pss.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.RCMRMT030101UKComponent02;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.hl7.v3.RCMRMT030101UKCompoundStatement;
import org.hl7.v3.RCMRMT030101UKEhrComposition;
import org.hl7.v3.RCMRMT030101UKEhrExtract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors;
import uk.nhs.adaptors.pss.translator.util.ResourceFilterUtil;
import uk.nhs.adaptors.pss.translator.util.ResourceReferenceUtil;
import uk.nhs.adaptors.pss.translator.util.CompoundStatementUtil;
import uk.nhs.adaptors.pss.translator.util.DegradedCodeableConcepts;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.hl7.fhir.dstu3.model.Observation.ObservationRelationshipType.DERIVEDFROM;
import static org.hl7.fhir.dstu3.model.Observation.ObservationRelationshipType.HASMEMBER;
import static org.hl7.fhir.dstu3.model.Observation.ObservationStatus.FINAL;
import static uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors.extractAllCompoundStatements;
import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToInstantType;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getEffective;
import static uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil.getParticipantReference;
import static uk.nhs.adaptors.pss.translator.util.ResourceFilterUtil.hasDiagnosticReportParent;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TemplateMapper extends AbstractMapper<DomainResource> {
    private static final String OBSERVATION_META_PROFILE = "Observation-1";
    private final CodeableConceptMapper codeableConceptMapper;
    private final ResourceReferenceUtil resourceReferenceUtil;

    @Override
    public List<DomainResource> mapResources(RCMRMT030101UKEhrExtract ehrExtract, Patient patient,
                                             List<Encounter> encounters, String practiseCode) {

        return  mapEhrExtractToFhirResource(ehrExtract, (extract, composition, component) ->
            extractAllCompoundStatements(component)
                .filter(Objects::nonNull)
                .filter(ResourceFilterUtil::isTemplate)
                .filter(compoundStatement -> !hasDiagnosticReportParent(ehrExtract, compoundStatement))
                .map(compoundStatement -> mapTemplate(extract, composition, compoundStatement, patient, encounters, practiseCode))
                .flatMap(List::stream)
        ).toList();

    }

    public void addReferences(List<DomainResource> templates, List<Observation> observations, RCMRMT030101UKEhrExtract ehrExtract) {
        List<Observation> parentObservations = templates.stream()
            .filter(Observation.class::isInstance)
            .map(Observation.class::cast)
            .toList();

        List<String> parentObservationIds = parentObservations.stream()
            .map(Observation::getId)
            .toList();

        var parentCompoundStatements = getCompoundStatementsByIds(ehrExtract, parentObservationIds);

        parentCompoundStatements.forEach(parentCompoundStatement -> {

            if (isObservationStatementTemplateParent(parentCompoundStatement)) {
                Observation parentObservation = parentObservations.stream()
                    .filter(observation -> observation.getId().equals(parentCompoundStatement.getId().get(0).getRoot()))
                    .findFirst()
                    .orElseThrow();

                List<String> childObservationIds = CompoundStatementUtil
                    .extractResourcesFromCompound(parentCompoundStatement,
                        RCMRMT030101UKComponent02::hasObservationStatement, RCMRMT030101UKComponent02::getObservationStatement)
                    .stream()
                    .map(RCMRMT030101UK04ObservationStatement.class::cast)
                    .map(observationStatement -> observationStatement.getId().getRoot())
                    .toList();

                List<Observation> childObservations = observations.stream()
                    .filter(observation -> childObservationIds.contains(observation.getId()))
                    .toList();

                childObservations.forEach(childObservation -> {
                    parentObservation.addRelated(new Observation.ObservationRelatedComponent()
                        .setType(HASMEMBER)
                        .setTarget(new Reference(new IdType(ResourceType.Observation.name(), childObservation.getId())))
                    );

                    childObservation.addRelated(new Observation.ObservationRelatedComponent()
                        .setType(DERIVEDFROM)
                        .setTarget(new Reference(new IdType(ResourceType.Observation.name(), parentObservation.getId())))
                    );
                });
            }
        });

    }

    private List<DomainResource> mapTemplate(RCMRMT030101UKEhrExtract ehrExtract, RCMRMT030101UKEhrComposition ehrComposition,
                                             RCMRMT030101UKCompoundStatement compoundStatement, Patient patient, List<Encounter> encounters,
                                             String practiseCode) {
        var encounter = getEncounter(encounters, ehrComposition);

        var parentObservation = createParentObservation(compoundStatement, practiseCode, patient, encounter,
            ehrComposition, ehrExtract);

        return List.of(parentObservation);
    }

    private Optional<Reference> getEncounter(List<Encounter> encounters, RCMRMT030101UKEhrComposition ehrComposition) {
        return encounters
            .stream()
            .filter(encounter -> encounter.getId().equals(ehrComposition.getId().getRoot()))
            .map(Reference::new)
            .findFirst();
    }

    private Observation createParentObservation(RCMRMT030101UKCompoundStatement compoundStatement, String practiseCode, Patient patient,
        Optional<Reference> encounter, RCMRMT030101UKEhrComposition ehrComposition, RCMRMT030101UKEhrExtract ehrExtract) {

        var parentObservation = new Observation();
        var id = compoundStatement.getId().get(0).getRoot();

        var codeableConcept = codeableConceptMapper.mapToCodeableConcept(compoundStatement.getCode());
        DegradedCodeableConcepts.addDegradedEntryIfRequired(codeableConcept, DegradedCodeableConcepts.DEGRADED_OTHER);

        parentObservation
            .setSubject(new Reference(patient))
            .setIssuedElement(getIssued(ehrComposition, ehrExtract))
            .addPerformer(getParticipantReference(compoundStatement.getParticipant(), ehrComposition))
            .setCode(codeableConcept)
            .setStatus(FINAL)
            .addIdentifier(buildIdentifier(id, practiseCode))
            .setMeta(generateMeta(OBSERVATION_META_PROFILE))
            .setId(id);

        encounter.ifPresent(parentObservation::setContext);
        addEffective(parentObservation,
            getEffective(compoundStatement.getEffectiveTime(), compoundStatement.getAvailabilityTime()));

        return parentObservation;
    }

    private void addEffective(Observation observation, Object effective) {
        if (effective instanceof DateTimeType) {
            observation.setEffective((DateTimeType) effective);
        } else if (effective instanceof Period) {
            observation.setEffective((Period) effective);
        }
    }

    private InstantType getIssued(RCMRMT030101UKEhrComposition ehrComposition, RCMRMT030101UKEhrExtract ehrExtract) {
        if (ehrComposition.getAuthor().getTime().hasValue()) {

            return parseToInstantType(ehrComposition.getAuthor().getTime().getValue());
        }
        return parseToInstantType(ehrExtract.getAvailabilityTime().getValue());
    }

    private List<RCMRMT030101UKCompoundStatement> getCompoundStatementsByIds(RCMRMT030101UKEhrExtract ehrExtract, List<String> ids) {

        return ehrExtract.getComponent().get(0).getEhrFolder().getComponent()
            .stream()
            .flatMap(component3 -> component3.getEhrComposition().getComponent().stream())
            .flatMap(CompoundStatementResourceExtractors::extractAllCompoundStatements)
            .filter(Objects::nonNull)
            .filter(compoundStatement -> ids.contains(compoundStatement.getId().get(0).getRoot()))
            .toList();
    }

    private boolean isObservationStatementTemplateParent(RCMRMT030101UKCompoundStatement compoundStatement) {
        var hasObservationStatement = compoundStatement.getComponent().stream()
            .anyMatch(RCMRMT030101UKComponent02::hasObservationStatement);

        var onlyHasObservationOrNarrative = compoundStatement.getComponent().stream()
            .allMatch(component -> component.hasObservationStatement() || component.hasNarrativeStatement());

        return hasObservationStatement && onlyHasObservationOrNarrative;
    }
}
