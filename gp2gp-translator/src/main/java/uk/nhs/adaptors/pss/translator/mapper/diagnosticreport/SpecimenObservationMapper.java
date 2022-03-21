package uk.nhs.adaptors.pss.translator.mapper.diagnosticreport;

import static org.hl7.fhir.dstu3.model.Observation.ObservationStatus.FINAL;

import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getEffective;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getInterpretation;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getIssued;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getReferenceRange;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getValueQuantity;
import static uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil.getParticipantReference;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.addContextToObservation;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.hl7.v3.CV;
import org.hl7.v3.RCMRMT030101UK04Annotation;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.hl7.v3.RCMRMT030101UK04PertinentInformation02;
import org.hl7.v3.RCMRMT030101UK04Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.mapper.CodeableConceptMapper;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SpecimenObservationMapper {

    private static final String SUBJECT_COMMENT = "Subject: %s ";
    private static final String META_PROFILE_SUFFIX = "Observation-1";

    private final CodeableConceptMapper codeableConceptMapper;

    public Observation mapSpecimenObservation(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04EhrComposition ehrComposition,
        RCMRMT030101UK04ObservationStatement observationStatement, List<Encounter> encounters, Patient patient, String practiseCode) {
        var id = observationStatement.getId().getRoot();

        final Observation observation = new Observation();
        observation.setId(id);
        observation.setStatus(FINAL);
        observation.addIdentifier(buildIdentifier(id, practiseCode));
        observation.setIssuedElement(getIssued(ehrExtract, ehrComposition));
        observation.addPerformer(getParticipantReference(observationStatement.getParticipant(), ehrComposition));
        observation.setInterpretation(getInterpretation(observationStatement.getInterpretationCode()));
        observation.setComment(getComment(observationStatement.getPertinentInformation(), observationStatement.getSubject()));
        observation.setReferenceRange(getReferenceRange(observationStatement.getReferenceRange()));
        observation.setSubject(new Reference(patient));

        observation.setMeta(generateMeta(META_PROFILE_SUFFIX));

        getCode(observationStatement.getCode()).ifPresent(observation::setCode);
        addContextToObservation(observation, encounters, ehrComposition);

        addValue(observation, getValueQuantity(observationStatement.getValue(), observationStatement.getUncertaintyCode()),
            getValueString(observationStatement.getValue()));
        addEffective(observation,
            getEffective(observationStatement.getEffectiveTime(), observationStatement.getAvailabilityTime()));

        return observation;
    }

    private Optional<CodeableConcept> getCode(CD code) {
        return code != null ? Optional.of(codeableConceptMapper.mapToCodeableConcept(code)) : Optional.empty();
    }

    private void addValue(Observation observation, Quantity valueQuantity, String valueString) {
        if (valueQuantity != null) {
            observation.setValue(valueQuantity);
        } else if (StringUtils.isNotEmpty(valueString)) {
            observation.setValue(new StringType().setValue(valueString));
        }
    }

    private void addEffective(Observation observation, Object effective) {
        if (effective instanceof DateTimeType) {
            observation.setEffective((DateTimeType) effective);
        } else if (effective instanceof Period) {
            observation.setEffective((Period) effective);
        }
    }

    private String getValueString(Object value) {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof CV cvValue) {
            return cvValue.getOriginalText() != null ? cvValue.getOriginalText() : cvValue.getDisplayName();
        }
        return null;
    }

    private String getComment(List<RCMRMT030101UK04PertinentInformation02> pertinentInformation, RCMRMT030101UK04Subject subject) {
        StringBuilder stringBuilder = new StringBuilder();

        if (subjectHasOriginalText(subject)) {
            stringBuilder.append(String.format(SUBJECT_COMMENT, subject.getPersonalRelationship().getCode().getOriginalText()));
        } else if (subjectHasDisplayName(subject)) {
            stringBuilder.append(String.format(SUBJECT_COMMENT, subject.getPersonalRelationship().getCode().getDisplayName()));
        }

        stringBuilder.append(pertinentInformation.stream()
            .filter(this::pertinentInformationHasOriginalText)
            .map(RCMRMT030101UK04PertinentInformation02.class::cast)
            .map(RCMRMT030101UK04PertinentInformation02::getPertinentAnnotation)
            .map(RCMRMT030101UK04Annotation::getText)
            .collect(Collectors.joining(StringUtils.SPACE)));

        return stringBuilder.toString();
    }

    private boolean subjectHasOriginalText(RCMRMT030101UK04Subject subject) {
        return subject != null && subject.getPersonalRelationship() != null
            && subject.getPersonalRelationship().getCode() != null && subject.getPersonalRelationship().getCode().getOriginalText() != null;
    }

    private boolean subjectHasDisplayName(RCMRMT030101UK04Subject subject) {
        return subject != null && subject.getPersonalRelationship() != null
            && subject.getPersonalRelationship().getCode() != null && subject.getPersonalRelationship().getCode().getDisplayName() != null;
    }

    private boolean pertinentInformationHasOriginalText(RCMRMT030101UK04PertinentInformation02 pertinentInformation) {
        return pertinentInformation != null && pertinentInformation.getPertinentAnnotation() != null
            && pertinentInformation.getPertinentAnnotation().getText() != null;
    }
}
