package uk.nhs.adaptors.pss.translator.mapper;

import static org.hl7.fhir.dstu3.model.Observation.ObservationStatus.FINAL;
import static org.hl7.fhir.dstu3.model.QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED;

import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToInstantType;
import static uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil.extractEhrCompositionsFromEhrExtract;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getEffective;
import static uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil.getParticipantReference;
import static uk.nhs.adaptors.pss.translator.util.ResourceReferenceUtil.extractChildReferencesFromTemplate;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.CompoundStatementUtil;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.ResourceFilterUtil;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TemplateMapper {
    private static final String OBSERVATION_META_PROFILE = "Observation-1";
    private static final String QUESTIONNAIRE_META_PROFILE = "QuestionnaireResponse-1";
    private static final String QUESTIONNAIRE_REFERENCE = "%s-QRSP";

    private final CodeableConceptMapper codeableConceptMapper;

    public List<? extends DomainResource> mapTemplates(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        String practiseCode) {

        var ehrCompositions = extractEhrCompositionsFromEhrExtract(ehrExtract);
        List<DomainResource> mappedResources = new ArrayList<>();

        ehrCompositions.forEach(ehrComposition -> ehrComposition.getComponent()
            .stream()
            .flatMap(CompoundStatementUtil::extractAllCompoundStatements)
            .filter(Objects::nonNull)
            .filter(ResourceFilterUtil::isTemplate)
            .forEach(compoundStatement -> {
                var encounter = getEncounter(encounters, ehrComposition);

                var parentObservation = createParentObservation(compoundStatement, practiseCode, patient, encounter,
                    ehrComposition, ehrExtract);

                var questionnaireResponse = createQuestionnaireResponse(compoundStatement, practiseCode, patient,
                    encounter, parentObservation, ehrComposition, ehrExtract);

                addChildReferencesToQuestionnaireResponse(questionnaireResponse, compoundStatement);

                mappedResources.add(questionnaireResponse);
                mappedResources.add(parentObservation);
            }));

        return mappedResources;
    }

    private void addChildReferencesToQuestionnaireResponse(QuestionnaireResponse questionnaireResponse,
        RCMRMT030101UK04CompoundStatement compoundStatement) {
        List<Reference> childResourceReferences = new ArrayList<>();
        extractChildReferencesFromTemplate(compoundStatement, childResourceReferences);
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

        parentObservation
            .setSubject(new Reference(patient))
            .setIssuedElement(getIssued(ehrComposition, ehrExtract))
            .addPerformer(getParticipantReference(compoundStatement.getParticipant(), ehrComposition))
            .setCode(codeableConceptMapper.mapToCodeableConcept(compoundStatement.getCode()))
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
}
