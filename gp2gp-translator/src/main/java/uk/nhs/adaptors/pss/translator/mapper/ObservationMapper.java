package uk.nhs.adaptors.pss.translator.mapper;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.v3.CD;
import org.hl7.v3.CR;
import org.hl7.v3.CV;
import org.hl7.v3.RCMRMT030101UK04Annotation;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.hl7.v3.RCMRMT030101UK04PertinentInformation02;
import org.hl7.v3.RCMRMT030101UK04RequestStatement;
import org.hl7.v3.RCMRMT030101UK04Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.nhs.adaptors.pss.translator.util.DatabaseImmunizationChecker;
import uk.nhs.adaptors.pss.translator.util.DegradedCodeableConcepts;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hl7.fhir.dstu3.model.Observation.ObservationStatus.FINAL;

import static uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors.extractAllObservationStatementsWithoutAllergiesAndBloodPressures;
import static uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors.extractAllRequestStatements;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getEffective;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getInterpretation;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getIssued;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getReferenceRange;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getValueQuantity;
import static uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil.getParticipantReference;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.addContextToObservation;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ObservationMapper extends AbstractMapper<Observation> {
    private static final String META_PROFILE = "Observation-1";
    private static final String SUBJECT_COMMENT = "Subject: %s";
    private static final String SELF_REFERRAL = "SelfReferral";
    private static final String URGENCY = "Urgency";
    private static final String TEXT = "Text";
    private static final String EPISODICITY_COMMENT = "Episodicity : %s";
    private static final BigInteger MINUS_ONE = new BigInteger("-1");

    private final CodeableConceptMapper codeableConceptMapper;
    private final DatabaseImmunizationChecker immunizationChecker;

    public List<Observation> mapResources(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        String practiseCode) {

        List<Observation> selfReferralObservations =
                mapEhrExtractToFhirResource(ehrExtract, (extract, composition, component) ->
                extractAllRequestStatements(component)
                        .filter(Objects::nonNull)
                        .filter(this::isSelfReferral)
                        .map(observationStatement
                                -> mapObservationFromRequestStatement(extract, composition, observationStatement,
                                patient, encounters, practiseCode)))
                .toList();

        List<Observation> observations =
                mapEhrExtractToFhirResource(ehrExtract, (extract, composition, component) ->
                extractAllObservationStatementsWithoutAllergiesAndBloodPressures(component)
                .filter(Objects::nonNull)
                .filter(this::isNotImmunization)
                .map(observationStatement
                    -> mapObservation(extract, composition, observationStatement, patient, encounters, practiseCode)))
            .toList();

        return Stream.concat(selfReferralObservations.stream(), observations.stream()).collect(Collectors.toList());
    }

    private Observation mapObservation(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04EhrComposition ehrComposition,
        RCMRMT030101UK04ObservationStatement observationStatement, Patient patient, List<Encounter> encounters, String practiseCode) {
        var id = observationStatement.getId().getRoot();

        Observation observation = new Observation()
            .setStatus(FINAL)
            .addIdentifier(buildIdentifier(id, practiseCode))
            .setCode(getCode(observationStatement.getCode()))
            .setIssuedElement(getIssued(ehrExtract, ehrComposition))
            .addPerformer(getParticipantReference(observationStatement.getParticipant(), ehrComposition))
            .setInterpretation(getInterpretation(observationStatement.getInterpretationCode()))
            .setComment(getComment(
                    observationStatement.getPertinentInformation(),
                    observationStatement.getSubject(),
                    observationStatement.getCode(),
                    Optional.of(Optional.ofNullable(observationStatement.getCode())
                            .map(CD::getQualifier)
                            .orElse(Collections.emptyList()))
            ))
            .setReferenceRange(getReferenceRange(observationStatement.getReferenceRange()))
            .setSubject(new Reference(patient));
        observation.setId(id);
        observation.setMeta(generateMeta(META_PROFILE));

        addContextToObservation(observation, encounters, ehrComposition);
        addValue(observation, getValueQuantity(observationStatement.getValue(), observationStatement.getUncertaintyCode()),
            getValueString(observationStatement.getValue()));
        addEffective(observation,
            getEffective(observationStatement.getEffectiveTime(), observationStatement.getAvailabilityTime()));

        return observation;
    }

    private Observation mapObservationFromRequestStatement(RCMRMT030101UK04EhrExtract ehrExtract,
                                                           RCMRMT030101UK04EhrComposition ehrComposition,
                                                           RCMRMT030101UK04RequestStatement requestStatement, Patient patient,
                                                           List<Encounter> encounters, String practiseCode) {
        var id = requestStatement.getId().get(0).getRoot();

        Observation observation = new Observation()
                .setStatus(FINAL)
                .addIdentifier(buildIdentifier(id, practiseCode))
                .setCode(getCode(requestStatement.getCode()))
                .setIssuedElement(getIssued(ehrExtract, ehrComposition))
                .addPerformer(getParticipantReference(requestStatement.getParticipant(), ehrComposition))
                .setComment(SELF_REFERRAL)
                .setSubject(new Reference(patient))
                .setComponent(createComponentList(requestStatement));

        observation.setId(id);
        observation.setMeta(generateMeta(META_PROFILE));

        addContextToObservation(observation, encounters, ehrComposition);
        addEffective(observation,
                getEffective(requestStatement.getEffectiveTime(), requestStatement.getAvailabilityTime()));

        return observation;
    }

    private boolean isNotImmunization(RCMRMT030101UK04ObservationStatement observationStatement) {
        if (observationStatement.hasCode() && observationStatement.getCode().hasCode()) {
            return !immunizationChecker.isImmunization(observationStatement);
        }
        return true;
    }

    private boolean isSelfReferral(RCMRMT030101UK04RequestStatement requestStatement) {
        for (CR qualifier : requestStatement.getCode().getQualifier()) {
            if (qualifier.getValue().getCode().equals(SELF_REFERRAL)) {
                return true;
            }
        }
        return false;
    }

    private void addEffective(Observation observation, Object effective) {
        if (effective instanceof DateTimeType) {
            observation.setEffective((DateTimeType) effective);
        } else if (effective instanceof Period) {
            observation.setEffective((Period) effective);
        }
    }

    private void addValue(Observation observation, Quantity valueQuantity, String valueString) {
        if (valueQuantity != null) {
            observation.setValue(valueQuantity);
        } else if (StringUtils.isNotEmpty(valueString)) {
            observation.setValue(new StringType().setValue(valueString));
        }
    }

    private CodeableConcept getCode(CD code) {
        if (code == null) {
            return null;
        }

        var codeableConcept = codeableConceptMapper.mapToCodeableConcept(code);
        DegradedCodeableConcepts.addDegradedEntryIfRequired(codeableConcept, DegradedCodeableConcepts.DEGRADED_OTHER);
        return codeableConcept;
    }

    private String getValueString(Object value) {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof CV cvValue) {
            return cvValue.getOriginalText() != null ? cvValue.getOriginalText() : cvValue.getDisplayName();
        }
        return null;
    }

    private String getComment(List<RCMRMT030101UK04PertinentInformation02> pertinentInformation,
                              RCMRMT030101UK04Subject subject,
                              CD code,
                              Optional<List<CR>> qualifiers) {
        StringJoiner stringJoiner = new StringJoiner(StringUtils.SPACE);

        if (subjectHasOriginalText(subject)) {
            stringJoiner.add(String.format(SUBJECT_COMMENT, subject.getPersonalRelationship().getCode().getOriginalText()));
        } else if (subjectHasDisplayName(subject)) {
            stringJoiner.add(String.format(SUBJECT_COMMENT, subject.getPersonalRelationship().getCode().getDisplayName()));
        }

        Optional<String> minusOneSequenceComment = extractSequenceCommentOfValue(MINUS_ONE, pertinentInformation);
        Optional<String> zeroSequenceComment = extractSequenceCommentOfValue(ZERO, pertinentInformation);
        Optional<String> postFixedSequenceComments = extractAllPostFixedSequenceComments(pertinentInformation);

        if (minusOneSequenceComment.isPresent()) {
            stringJoiner.add(minusOneSequenceComment.orElseThrow());

            if (code.hasOriginalText()) {
                stringJoiner.add(code.getOriginalText());
            }
        }

        zeroSequenceComment.ifPresent(stringJoiner::add);
        postFixedSequenceComments.ifPresent(stringJoiner::add);

        // Append episodicity to the comment.
        qualifiers.ifPresent(q -> appendEpisodicity(q, stringJoiner));

        return stringJoiner.toString();
    }

    /**
     * Append episodicity to comment separating from existing comments with <br> tag.
     * @param qualifiers
     * @param stringJoiner
     */
    private void appendEpisodicity(List<CR> qualifiers, StringJoiner stringJoiner) {
        qualifiers.stream()
                .map(this::buildEpisodicityText)
                .filter(Objects::nonNull)
                .forEach(et -> {
                    if (stringJoiner.length() > 0) {
                        stringJoiner.add("<br>");
                    }
                    stringJoiner.add(EPISODICITY_COMMENT.formatted(et));
                });
    }

    /**
     * Build out the episodicity text in the same style as AllergyIntolerance.
     * @param qualifier
     * @return
     */
    private String buildEpisodicityText(CR qualifier) {
        var qualifierName = qualifier.getName();

        if (qualifierName == null) {
            return null;
        }

        var text = "code=" + qualifierName.getCode()
                + ", displayName=" + qualifierName.getDisplayName();

        if (qualifierName.hasOriginalText()) {
            return text + ", originalText=" + qualifierName.getOriginalText();
        }

        return text;
    }

    private Optional<String> extractSequenceCommentOfValue(BigInteger value,
        List<RCMRMT030101UK04PertinentInformation02> pertinentInformation) {
        return pertinentInformation.stream()
            .filter(this::pertinentInformationHasOriginalText)
            .filter(pertinentInfo -> !pertinentInfo.getSequenceNumber().hasNullFlavor())
            .filter(pertinentInfo -> pertinentInfo.getSequenceNumber().getValue().equals(value))
            .map(RCMRMT030101UK04PertinentInformation02::getPertinentAnnotation)
            .map(RCMRMT030101UK04Annotation::getText)
            .findFirst();
    }

    private Optional<String> extractAllPostFixedSequenceComments(List<RCMRMT030101UK04PertinentInformation02> pertinentInformation) {
        String postFixedSequenceComments = pertinentInformation.stream()
            .filter(this::pertinentInformationHasOriginalText)
            .filter(pertinentInfo -> pertinentInfo.getSequenceNumber().hasNullFlavor()
                || pertinentInfo.getSequenceNumber().getValue().equals(ONE))
            .map(RCMRMT030101UK04PertinentInformation02::getPertinentAnnotation)
            .map(RCMRMT030101UK04Annotation::getText)
            .collect(Collectors.joining(StringUtils.SPACE));

        if (StringUtils.isEmpty(postFixedSequenceComments)) {
            return Optional.empty();
        }

        return Optional.of(postFixedSequenceComments);
    }

    private boolean pertinentInformationHasOriginalText(RCMRMT030101UK04PertinentInformation02 pertinentInformation) {
        return pertinentInformation != null && pertinentInformation.getPertinentAnnotation() != null
            && pertinentInformation.getPertinentAnnotation().getText() != null;
    }

    private boolean subjectHasOriginalText(RCMRMT030101UK04Subject subject) {
        return subject != null && subject.getPersonalRelationship() != null
            && subject.getPersonalRelationship().getCode() != null && subject.getPersonalRelationship().getCode().getOriginalText() != null;
    }

    private boolean subjectHasDisplayName(RCMRMT030101UK04Subject subject) {
        return subject != null && subject.getPersonalRelationship() != null
            && subject.getPersonalRelationship().getCode() != null && subject.getPersonalRelationship().getCode().getDisplayName() != null;
    }
    private List<Observation.ObservationComponentComponent> createComponentList(RCMRMT030101UK04RequestStatement
                                                                                        requestStatement) {
        List<Observation.ObservationComponentComponent> componentList = new ArrayList<>();

        Observation.ObservationComponentComponent urgency =
                new Observation.ObservationComponentComponent(
                        new CodeableConcept().setTextElement(new StringType(URGENCY)));
        urgency.setProperty("value[x]", new StringType(requestStatement.getPriorityCode().getOriginalText()));
        componentList.add(urgency);

        Observation.ObservationComponentComponent text =
                new Observation.ObservationComponentComponent(
                        new CodeableConcept().setTextElement(new StringType(TEXT)));
        text.setProperty("value[x]", new StringType(requestStatement.getText()));
        componentList.add(text);

        return componentList;
    }
}
