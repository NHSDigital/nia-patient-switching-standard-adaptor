package uk.nhs.adaptors.pss.translator.mapper;

import static org.hl7.fhir.dstu3.model.Observation.ObservationStatus.FINAL;

import static uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil.extractEhrCompositionForObservationStatement;
import static uk.nhs.adaptors.pss.translator.util.EncounterReferenceUtil.getEncounterReference;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getEffective;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getInterpretation;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getIssued;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getReferenceRange;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getValueQuantity;
import static uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil.getParticipantReference;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CD;
import org.hl7.v3.CV;
import org.hl7.v3.II;
import org.hl7.v3.RCMRMT030101UK04Annotation;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.hl7.v3.RCMRMT030101UK04PertinentInformation02;
import org.hl7.v3.RCMRMT030101UK04Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ObservationMapper {
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Observation-1";
    private static final String SUBJECT_COMMENT = "Subject: %s ";

    private final CodeableConceptMapper codeableConceptMapper;
    private final QuantityMapper quantityMapper;

    public List<Observation> mapObservations(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters) {
        /**
         * TODO: Known future implementations to this mapper
         * - concatenate source practice org id to identifier URL (NIAD-2021)
         */

        var compositionsList = getCompositionsContainingObservationStatement(ehrExtract);

        return compositionsList
            .stream()
            .flatMap(ehrComposition -> ehrComposition.getComponent().stream())
            .map(RCMRMT030101UK04Component4::getObservationStatement)
            .filter(Objects::nonNull)
            .map(observationStatement -> {
                var id = observationStatement.getId().getRoot();

                Observation observation = new Observation()
                    .setStatus(FINAL)
                    .addIdentifier(getIdentifier(id))
                    .setCode(getCode(observationStatement.getCode()))
                    .setIssuedElement(getIssued(ehrExtract,
                        extractEhrCompositionForObservationStatement(ehrExtract, observationStatement.getId())))
                    .addPerformer(getParticipantReference(
                        observationStatement.getParticipant(),
                        extractEhrCompositionForObservationStatement(ehrExtract, observationStatement.getId())))
                    .setInterpretation(getInterpretation(observationStatement.getInterpretationCode()))
                    .setComment(getComment(observationStatement.getPertinentInformation(), observationStatement.getSubject()))
                    .setReferenceRange(getReferenceRange(observationStatement.getReferenceRange()))
                    .setSubject(new Reference(patient));

                observation.setId(id);
                observation.getMeta().getProfile().add(new UriType(META_PROFILE));

                addContext(observation, getEncounterReference(compositionsList, encounters,
                    getEhrCompositionId(compositionsList, observationStatement).getRoot()));
                addValue(observation, getValueQuantity(observationStatement.getValue(), observationStatement.getUncertaintyCode()),
                    getValueString(observationStatement.getValue()));
                addEffective(observation,
                    getEffective(observationStatement.getEffectiveTime(), observationStatement.getAvailabilityTime()));

                return observation;
            }).toList();
    }

    private II getEhrCompositionId(List<RCMRMT030101UK04EhrComposition> ehrCompositions,
        RCMRMT030101UK04ObservationStatement observationStatement) {
        return ehrCompositions
            .stream()
            .filter(e -> e.getComponent()
                .stream()
                .anyMatch(f -> observationStatement.equals(f.getObservationStatement()))
            ).findFirst()
            .map(RCMRMT030101UK04EhrComposition::getId)
            .orElse(null);
    }

    private void addContext(Observation observation, Reference context) {
        if (context != null) {
            observation.setContext(context);
        }
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

    private Identifier getIdentifier(String id) {
        return new Identifier()
            .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL (NIAD-2021)
            .setValue(id);
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

    private List<RCMRMT030101UK04EhrComposition> getCompositionsContainingObservationStatement(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent().stream()
            .flatMap(component -> component.getEhrFolder().getComponent().stream())
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .filter(ehrComposition -> ehrComposition.getComponent()
                .stream()
                .map(RCMRMT030101UK04Component4::getObservationStatement)
                .anyMatch(Objects::nonNull))
            .toList();
    }
}
