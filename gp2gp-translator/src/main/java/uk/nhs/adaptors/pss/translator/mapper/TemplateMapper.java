package uk.nhs.adaptors.pss.translator.mapper;

import static org.hl7.fhir.dstu3.model.Observation.ObservationRelationshipType.DERIVEDFROM;
import static org.hl7.fhir.dstu3.model.Observation.ObservationRelationshipType.HASMEMBER;
import static org.hl7.fhir.dstu3.model.Observation.ObservationStatus.FINAL;
import static org.hl7.fhir.dstu3.model.QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED;

import static uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors.extractAllCompoundStatements;
import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToInstantType;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getEffective;
import static uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil.getParticipantReference;
import static uk.nhs.adaptors.pss.translator.util.ResourceFilterUtil.hasDiagnosticReportParent;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors;
import uk.nhs.adaptors.pss.translator.util.CompoundStatementUtil;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.DegradedCodeableConcepts;
import uk.nhs.adaptors.pss.translator.util.ResourceFilterUtil;
import uk.nhs.adaptors.pss.translator.util.ResourceReferenceUtil;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TemplateMapper extends AbstractMapper<DomainResource> {
    private static final String OBSERVATION_META_PROFILE = "Observation-1";
    private static final String QUESTIONNAIRE_META_PROFILE = "QuestionnaireResponse-1";
    private static final String QUESTIONNAIRE_REFERENCE = "%s-QRSP";

    private final CodeableConceptMapper codeableConceptMapper;
    private final ResourceReferenceUtil resourceReferenceUtil;

    @Override
    public List<DomainResource> mapResources(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        String practiseCode) {

        var mappings = mapEhrExtractToFhirResource(ehrExtract, (extract, composition, component) ->
            extractAllCompoundStatements(component)
                .filter(Objects::nonNull)
                .filter(ResourceFilterUtil::isTemplate)
                .filter(compoundStatement -> !hasDiagnosticReportParent(ehrExtract, compoundStatement))
                .map(compoundStatement -> mapTemplate(extract, composition, compoundStatement, patient, encounters, practiseCode))
                .flatMap(List::stream)
        ).toList();
        return mappings;
    }

    public void addReferences(List<DomainResource> templates, List<Observation> observations, RCMRMT030101UK04EhrExtract ehrExtract) {
        List<Observation> parentObservations = templates.stream()
            .filter(template -> template instanceof Observation)
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
                        RCMRMT030101UK04Component02::hasObservationStatement, RCMRMT030101UK04Component02::getObservationStatement)
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

    private List<DomainResource> mapTemplate(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04EhrComposition ehrComposition,
        RCMRMT030101UK04CompoundStatement compoundStatement, Patient patient, List<Encounter> encounters, String practiseCode) {
        var encounter = getEncounter(encounters, ehrComposition);

        var parentObservation = createParentObservation(compoundStatement, practiseCode, patient, encounter,
            ehrComposition, ehrExtract);

//      The following have been disabled as Questionnares have been dropped from the PS Specification. NIAD-2190
//        var questionnaireResponse = createQuestionnaireResponse(compoundStatement, practiseCode, patient,
//            encounter, parentObservation, ehrComposition, ehrExtract);
//        addChildReferencesToQuestionnaireResponse(questionnaireResponse, compoundStatement);
//        return List.of(questionnaireResponse, parentObservation);
        return List.of(parentObservation);
    }

    private void addChildReferencesToQuestionnaireResponse(QuestionnaireResponse questionnaireResponse,
        RCMRMT030101UK04CompoundStatement compoundStatement) {
        List<Reference> childResourceReferences = new ArrayList<>();
        resourceReferenceUtil.extractChildReferencesFromTemplate(compoundStatement, childResourceReferences);
        childResourceReferences.forEach(reference -> {
            questionnaireResponse.addItem().addAnswer(
                new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent().setValue(reference));
        });
    }

    private Optional<Reference> getEncounter(List<Encounter> encounters, RCMRMT030101UK04EhrComposition ehrComposition) {
        return encounters
            .stream()
            .filter(encounter -> encounter.getId().equals(ehrComposition.getId().getRoot()))
            .map(Reference::new)
            .findFirst();
    }

    private Observation createParentObservation(RCMRMT030101UK04CompoundStatement compoundStatement, String practiseCode, Patient patient,
        Optional<Reference> encounter, RCMRMT030101UK04EhrComposition ehrComposition, RCMRMT030101UK04EhrExtract ehrExtract) {

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

    private InstantType getIssued(RCMRMT030101UK04EhrComposition ehrComposition, RCMRMT030101UK04EhrExtract ehrExtract) {
        if (ehrComposition.getAuthor().getTime().hasValue()) {
            return parseToInstantType(ehrComposition.getAuthor().getTime().getValue());
        }
        return parseToInstantType(ehrExtract.getAvailabilityTime().getValue());
    }

    private QuestionnaireResponse createQuestionnaireResponse(RCMRMT030101UK04CompoundStatement compoundStatement, String practiseCode,
        Patient patient, Optional<Reference> encounter, Observation parentObservation,
        RCMRMT030101UK04EhrComposition ehrComposition, RCMRMT030101UK04EhrExtract ehrExtract) {

        var questionnaireResponse = new QuestionnaireResponse();
        var id = compoundStatement.getId().get(0).getRoot();

        questionnaireResponse
            .addItem(createdLinkedId(compoundStatement))
            .setAuthoredElement(getAuthored(ehrComposition, ehrExtract))
            .setSubject(new Reference(patient))
            .setStatus(COMPLETED)
            .setParent(List.of(new Reference(parentObservation)))
            .setIdentifier(buildIdentifier(id, practiseCode))
            .setMeta(generateMeta(QUESTIONNAIRE_META_PROFILE))
            .setId(QUESTIONNAIRE_REFERENCE.formatted(id));

        encounter.ifPresent(questionnaireResponse::setContext);

        return questionnaireResponse;
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent createdLinkedId(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement.getCode().hasOriginalText()
            ? new QuestionnaireResponse.QuestionnaireResponseItemComponent().setLinkId(compoundStatement.getCode().getOriginalText())
            : new QuestionnaireResponse.QuestionnaireResponseItemComponent().setLinkId(compoundStatement.getCode().getDisplayName());
    }

    private DateTimeType getAuthored(RCMRMT030101UK04EhrComposition ehrComposition, RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrComposition.getAuthor().getTime().hasValue()
            ? DateFormatUtil.parseToDateTimeType(ehrComposition.getAuthor().getTime().getValue())
            : DateFormatUtil.parseToDateTimeType(ehrExtract.getAvailabilityTime().getValue());
    }

    private List<RCMRMT030101UK04CompoundStatement> getCompoundStatementsByIds(RCMRMT030101UK04EhrExtract ehrExtract, List<String> ids) {
        return ehrExtract.getComponent().get(0).getEhrFolder().getComponent()
            .stream()
            .flatMap(component3 -> component3.getEhrComposition().getComponent().stream())
            .flatMap(CompoundStatementResourceExtractors::extractAllCompoundStatements)
            .filter(Objects::nonNull)
            .filter(compoundStatement -> ids.contains(compoundStatement.getId().get(0).getRoot()))
            .toList();
    }

    private boolean isObservationStatementTemplateParent(RCMRMT030101UK04CompoundStatement compoundStatement) {
        var hasObservationStatement = compoundStatement.getComponent().stream()
            .anyMatch(RCMRMT030101UK04Component02::hasObservationStatement);

        var onlyHasObservationOrNarrative = compoundStatement.getComponent().stream()
            .allMatch(component -> component.hasObservationStatement() || component.hasNarrativeStatement());

        return hasObservationStatement && onlyHasObservationOrNarrative;
    }
}
