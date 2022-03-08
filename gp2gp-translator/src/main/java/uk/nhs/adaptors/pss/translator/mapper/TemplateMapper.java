package uk.nhs.adaptors.pss.translator.mapper;

import static uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil.getCompositionsContainingCompoundStatement;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getEffective;
import static uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil.getParticipantReference;
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
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.BloodPressureValidatorUtil;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.EncounterReferenceUtil;
import uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TemplateMapper {
    private static final List<String> COMPOUND_CODES = List.of("CLUSTER", "BATTERY");
    private static final String PATHOLOGY_CODE = "16488004";
    private static final String OBSERVATION_META_PROFILE = "Observation-1";
    private static final String QUESTIONNAIRE_META_PROFILE = "QuestionnaireResponse-1";
    private static final String QUESTIONNAIRE_REFERENCE = "%s-QRSP";

    private final CodeableConceptMapper codeableConceptMapper;

    public List<? extends DomainResource> mapTemplates(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        String practiseCode) {

        var ehrCompositions = getCompositionsContainingCompoundStatement(ehrExtract);
        List<DomainResource> mappedResources = new ArrayList<>();

        ehrCompositions.forEach(ehrComposition -> {
            ehrComposition.getComponent()
                .stream()
                .map(RCMRMT030101UK04Component4::getCompoundStatement)
                .filter(Objects::nonNull)
                .filter(this::isMappableTemplate)
                .forEach(compoundStatement -> {

                    var encounter = Optional.of(EncounterReferenceUtil.getEncounterReference(ehrCompositions, encounters,
                        ehrComposition.getId().getRoot()));

                    var parentObservation = createParentObservation(compoundStatement, practiseCode, patient, encounter,
                        ehrComposition, ehrExtract);

                    var questionnaireResponse = createQuestionnaireResponse(compoundStatement, practiseCode, patient,
                        encounter, parentObservation, ehrComposition, ehrExtract);

                    mappedResources.add(questionnaireResponse);
                    mappedResources.add(parentObservation);
                });
        });

        return mappedResources;
    }

    private Observation createParentObservation(RCMRMT030101UK04CompoundStatement compoundStatement, String practiseCode, Patient patient,
        Optional<Reference> encounter, RCMRMT030101UK04EhrComposition ehrComposition, RCMRMT030101UK04EhrExtract ehrExtract) {

        var parentObservation = new Observation();
        var id = compoundStatement.getId().get(0).getRoot();

        parentObservation
            .setIssuedElement(getIssued(ehrComposition, ehrExtract))
            .addPerformer(getParticipantReference(compoundStatement.getParticipant(), ehrComposition))
            .setCode(codeableConceptMapper.mapToCodeableConcept(compoundStatement.getCode()))
            .setStatus(Observation.ObservationStatus.FINAL)
            .addIdentifier(buildIdentifier(id, practiseCode))
            .setMeta(generateMeta(OBSERVATION_META_PROFILE))
            .setId(id);

        encounter.ifPresent(parentObservation::setContext);
        addEffective(parentObservation,
            getEffective(compoundStatement.getEffectiveTime(), compoundStatement.getAvailabilityTime())); // check with barry if this is a bug

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
            return DateFormatUtil.parseToInstantType(ehrComposition.getAuthor().getTime().getValue());
        }
        return DateFormatUtil.parseToInstantType(ehrExtract.getAvailabilityTime().getValue());
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
            // .setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED) TODO: This needs to be FINAL
            .setParent(List.of(new Reference(parentObservation)))
            .setIdentifier(buildIdentifier(id, practiseCode))
            .setMeta(generateMeta(QUESTIONNAIRE_META_PROFILE))
            .setId(QUESTIONNAIRE_REFERENCE.formatted(id));

        encounter.ifPresent(questionnaireResponse::setContext);

        return questionnaireResponse;
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent createdLinkedId(RCMRMT030101UK04CompoundStatement compoundStatement) {
        if (compoundStatement.getCode().hasOriginalText()) {
            return new QuestionnaireResponse.QuestionnaireResponseItemComponent().setLinkId(compoundStatement.getCode().getOriginalText());
        }
        return new QuestionnaireResponse.QuestionnaireResponseItemComponent().setLinkId(compoundStatement.getCode().getDisplayName());
    }

    private DateTimeType getAuthored(RCMRMT030101UK04EhrComposition ehrComposition, RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrComposition.getAuthor().getTime().hasValue()
            ? DateFormatUtil.parseToDateTimeType(ehrComposition.getAuthor().getTime().getValue())
            : DateFormatUtil.parseToDateTimeType(ehrExtract.getAvailabilityTime().getValue());
    }

    private boolean isMappableTemplate(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return COMPOUND_CODES.contains(compoundStatement.getClassCode().get(0))
            && !PATHOLOGY_CODE.equals(compoundStatement.getCode().getCode())
            && !BloodPressureValidatorUtil.containsValidBloodPressureTriple(compoundStatement);
    }
}
