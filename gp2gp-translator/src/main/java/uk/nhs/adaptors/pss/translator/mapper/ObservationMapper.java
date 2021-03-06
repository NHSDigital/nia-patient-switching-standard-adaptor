package uk.nhs.adaptors.pss.translator.mapper;

import static org.hl7.fhir.dstu3.model.Observation.ObservationStatus.FINAL;

import static uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors.extractAllObservationStatementsWithoutAllergies;
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
import java.util.Objects;
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
import uk.nhs.adaptors.pss.translator.util.BloodPressureValidatorUtil;
import uk.nhs.adaptors.pss.translator.util.DatabaseImmunizationChecker;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ObservationMapper extends AbstractMapper<Observation> {
    private static final String META_PROFILE = "Observation-1";
    private static final String SUBJECT_COMMENT = "Subject: %s ";

    private final CodeableConceptMapper codeableConceptMapper;
    private final DatabaseImmunizationChecker immunizationChecker;

    public List<Observation> mapResources(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        String practiseCode) {
        return mapEhrExtractToFhirResource(ehrExtract, (extract, composition, component) ->
            extractAllObservationStatementsWithoutAllergies(component)
                .filter(Objects::nonNull)
                .filter(this::isNotBloodPressure)
                .filter(this::isNotImmunization)
                .map(observationStatement
                    -> mapObservation(extract, composition, observationStatement, patient, encounters, practiseCode)))
            .toList();
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
            .setComment(getComment(observationStatement.getPertinentInformation(), observationStatement.getSubject()))
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

    private boolean isNotBloodPressure(RCMRMT030101UK04ObservationStatement observationStatement) {
        if (observationStatement.hasCode() && observationStatement.getCode().hasCode()) {
            return !BloodPressureValidatorUtil.isSystolicBloodPressure(observationStatement.getCode().getCode())
                && !BloodPressureValidatorUtil.isDiastolicBloodPressure(observationStatement.getCode().getCode());
        }
        return true;
    }

    private boolean isNotImmunization(RCMRMT030101UK04ObservationStatement observationStatement) {
        if (observationStatement.hasCode() && observationStatement.getCode().hasCode()) {
            return !immunizationChecker.isImmunization(observationStatement);
        }
        return true;
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
        return code != null ? codeableConceptMapper.mapToCodeableConcept(code) : null;
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
}
