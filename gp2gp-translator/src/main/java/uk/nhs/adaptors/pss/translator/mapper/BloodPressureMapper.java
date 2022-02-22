package uk.nhs.adaptors.pss.translator.mapper;

import static uk.nhs.adaptors.pss.translator.util.BloodPressureValidatorUtil.isDiastolicBloodPressure;
import static uk.nhs.adaptors.pss.translator.util.BloodPressureValidatorUtil.isSystolicBloodPressure;
import static uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil.extractEhrCompositionForCompoundStatement;
import static uk.nhs.adaptors.pss.translator.util.EncounterReferenceUtil.getEncounterReference;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getEffective;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getInterpretation;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getIssued;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getReferenceRange;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getValueQuantity;
import static uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil.getParticipantReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.dstu3.model.Observation.ObservationStatus;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CD;
import org.hl7.v3.II;
import org.hl7.v3.RCMRMT030101UK04Annotation;
import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04NarrativeStatement;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.hl7.v3.RCMRMT030101UK04PertinentInformation02;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.BloodPressureValidatorUtil;

@Service
@AllArgsConstructor
public class BloodPressureMapper {
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Observation-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String BATTERY_VALUE = "BATTERY";
    private static final String SYSTOLIC_NOTE = "Systolic Note: ";
    private static final String DIASTOLIC_NOTE = "Diastolic Note: ";
    private static final String BP_NOTE = "BP Note: ";

    private CodeableConceptMapper codeableConceptMapper;

    public List<Observation> mapBloodPressure(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters) {
        /**
         * TODO: Known future implementations to this mapper
         * - performer: fallback to a default 'Unknown User' Practitioner if none are present in performer (NIAD-2026)
         * - concatenate source practice org id to identifier URL (NIAD-2021)
         *
         * - add functionality for mapping non conformant blood pressures
         *
         */

        // TODO: id - done
        // TODO: meta - done
        // TODO: identifier - done
        // TODO: status - done
        // TODO: code - done
        // TODO: subject - done
        // TODO: context - done
        // TODO: effective - done
        // TODO: issued - done
        // TODO: performer - done
        // TODO: comment - done
        // TODO: referenceRange - N/A
        // TODO: component - done

        // TODO: Add functionality for mapping non conformant blood pressures
        var compositionsList = getCompositionsContainingCompoundStatementBattery(ehrExtract);

        return compositionsList.stream()
            .flatMap(ehrComposition -> ehrComposition.getComponent().stream())
            .map(RCMRMT030101UK04Component4::getCompoundStatement)
            .filter(Objects::nonNull)
            .filter(compoundStatement -> {
                var observationStatements = getObservationStatementsFromCompoundStatement(compoundStatement);

                if (observationStatements.size() == 2 && observationStatements.get(0) != null && observationStatements.get(1) != null) {
                    return BloodPressureValidatorUtil.validateBloodPressureTriple(compoundStatement.getCode().getCode(),
                        observationStatements.get(0).getCode().getCode(), observationStatements.get(1).getCode().getCode());
                }

                return false;
            })
            .map(compoundStatement -> {
                var observationStatements = getObservationStatementsFromCompoundStatement(compoundStatement);
                var id = compoundStatement.getId().get(0).getRoot();

                Observation observation = new Observation()
                    .addIdentifier(getIdentifier(id))
                    .setStatus(ObservationStatus.FINAL)
                    .setCode(getCode(compoundStatement.getCode()))
                    .setComponent(getComponent(observationStatements))
                    .setComment(getComment(observationStatements,
                        getNarrativeStatementsFromCompoundStatement(compoundStatement)))
                    .setSubject(new Reference(patient))
                    .setIssuedElement(getIssued(ehrExtract, compoundStatement.getId().get(0)))
                    .addPerformer(getParticipantReference(
                        compoundStatement.getParticipant(),
                        extractEhrCompositionForCompoundStatement(ehrExtract, compoundStatement.getId().get(0))));

                observation.setId(id);
                observation.getMeta().getProfile().add(new UriType(META_PROFILE));

                addEffective(observation, getEffective(compoundStatement.getEffectiveTime(), compoundStatement.getAvailabilityTime()));
                addContext(observation, getEncounterReference(compositionsList, encounters,
                    getEhrCompositionId(compositionsList, compoundStatement).getRoot()));

                return observation;
            }).toList();
    }

    private List<RCMRMT030101UK04EhrComposition> getCompositionsContainingCompoundStatementBattery(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent().stream()
            .flatMap(component -> component.getEhrFolder().getComponent().stream())
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .filter(ehrComposition -> ehrComposition.getComponent()
                .stream()
                .map(RCMRMT030101UK04Component4::getCompoundStatement)
                .anyMatch(compoundStatement ->
                    Objects.nonNull(compoundStatement) && BATTERY_VALUE.equals(compoundStatement.getType())))
            .toList();
    }

    private II getEhrCompositionId(List<RCMRMT030101UK04EhrComposition> ehrCompositions,
        RCMRMT030101UK04CompoundStatement compoundStatement) {
        return ehrCompositions
            .stream()
            .filter(e -> e.getComponent()
                .stream()
                .anyMatch(f -> compoundStatement.equals(f.getCompoundStatement()))
            ).findFirst()
            .map(RCMRMT030101UK04EhrComposition::getId)
            .orElse(null);
    }

    private Identifier getIdentifier(String id) {
        return new Identifier()
            .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL (NIAD-2021)
            .setValue(id);
    }

    private CodeableConcept getCode(CD code) {
        return code != null ? codeableConceptMapper.mapToCodeableConcept(code) : null;
    }

    private List<ObservationComponentComponent> getComponent(List<RCMRMT030101UK04ObservationStatement> observationStatements) {
        var components = new ArrayList<ObservationComponentComponent>();

        for (RCMRMT030101UK04ObservationStatement observationStatement
            : observationStatements) {
            components.add(new ObservationComponentComponent()
                .setCode(getCode(observationStatement.getCode()))
                .setValue(getValueQuantity(observationStatement.getValue(),
                    observationStatement.getUncertaintyCode()))
                .setInterpretation(getInterpretation(observationStatement.getInterpretationCode()))
                .setReferenceRange(getReferenceRange(observationStatement.getReferenceRange())));
        }

        return components;
    }

    private String getComment(List<RCMRMT030101UK04ObservationStatement> observationStatements,
        List<RCMRMT030101UK04NarrativeStatement> narrativeStatements) {
        var stringBuilder = new StringBuilder();

        for (RCMRMT030101UK04ObservationStatement observationStatement
            : observationStatements) {
            stringBuilder.append(observationStatement.getPertinentInformation().stream()
                .filter(this::pertinentInformationHasText)
                .map(RCMRMT030101UK04PertinentInformation02.class::cast)
                .map(RCMRMT030101UK04PertinentInformation02::getPertinentAnnotation)
                .map(RCMRMT030101UK04Annotation::getText)

                .map(text -> {
                    if (isSystolicBloodPressure(observationStatement.getCode().getCode())) {
                        return SYSTOLIC_NOTE + text + StringUtils.SPACE;
                    }
                    if (isDiastolicBloodPressure(observationStatement.getCode().getCode())) {
                        return DIASTOLIC_NOTE + text + StringUtils.SPACE;
                    }
                    return StringUtils.EMPTY;
                }));
        }

        if (!narrativeStatements.isEmpty()) {
            stringBuilder.append(BP_NOTE);
            for (RCMRMT030101UK04NarrativeStatement narrativeStatement
                : narrativeStatements) {
                stringBuilder.append(narrativeStatement.getText() + StringUtils.SPACE);
            }
        }

        return stringBuilder.toString().trim();
    }

    private boolean pertinentInformationHasText(RCMRMT030101UK04PertinentInformation02 pertinentInformation) {
        return pertinentInformation != null && pertinentInformation.getPertinentAnnotation() != null
            && StringUtils.isNotEmpty(pertinentInformation.getPertinentAnnotation().getText());
    }

    private List<RCMRMT030101UK04ObservationStatement> getObservationStatementsFromCompoundStatement(
        RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement.getComponent().stream()
            .filter(component -> component.getObservationStatement() != null)
            .map(RCMRMT030101UK04Component02::getObservationStatement)
            .toList();
    }

    private List<RCMRMT030101UK04NarrativeStatement> getNarrativeStatementsFromCompoundStatement(
        RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement.getComponent().stream()
            .filter(component -> component.getNarrativeStatement() != null)
            .map(RCMRMT030101UK04Component02::getNarrativeStatement)
            .toList();
    }

    private void addEffective(Observation observation, Object effective) {
        if (effective instanceof DateTimeType) {
            observation.setEffective((DateTimeType) effective);
        } else if (effective instanceof Period) {
            observation.setEffective((Period) effective);
        }
    }

    private void addContext(Observation observation, Reference context) {
        if (context != null) {
            observation.setContext(context);
        }
    }
}
