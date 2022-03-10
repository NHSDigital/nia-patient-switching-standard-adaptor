package uk.nhs.adaptors.pss.translator.mapper;

import static uk.nhs.adaptors.pss.translator.util.BloodPressureValidatorUtil.isDiastolicBloodPressure;
import static uk.nhs.adaptors.pss.translator.util.BloodPressureValidatorUtil.isSystolicBloodPressure;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getEffective;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getInterpretation;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getIssued;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getReferenceRange;
import static uk.nhs.adaptors.pss.translator.util.ObservationUtil.getValueQuantity;
import static uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil.getParticipantReference;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.addContextToObservation;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors.extractAllCompoundStatements;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.dstu3.model.Observation.ObservationStatus;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CD;
import org.hl7.v3.RCMRMT030101UK04Annotation;
import org.hl7.v3.RCMRMT030101UK04Component02;
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
public class BloodPressureMapper extends AbstractMapper<Observation> {
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Observation-1";
    private static final String BATTERY_VALUE = "BATTERY";
    private static final String SYSTOLIC_NOTE = "Systolic Note: ";
    private static final String DIASTOLIC_NOTE = "Diastolic Note: ";
    private static final String BP_NOTE = "BP Note: ";

    private CodeableConceptMapper codeableConceptMapper;

    public List<Observation> mapResources(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        String practiseCode) {

        return mapEhrExtractToFhirResource(ehrExtract, (extract, composition, component) ->
            extractAllCompoundStatements(component)
                .filter(Objects::nonNull)
                .filter(compoundStatement -> BATTERY_VALUE.equals(compoundStatement.getClassCode().get(0))
                    && containsValidBloodPressureTriple(compoundStatement))
                .map(compoundStatement -> mapObservation(extract, composition, compoundStatement, patient, encounters, practiseCode)))
            .toList();
    }

    private Observation mapObservation(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04EhrComposition ehrComposition,
        RCMRMT030101UK04CompoundStatement compoundStatement, Patient patient, List<Encounter> encounters, String practiseCode) {
        var observationStatements = getObservationStatementsFromCompoundStatement(compoundStatement);
        var id = compoundStatement.getId().get(0);

        Observation observation = new Observation()
            .addIdentifier(buildIdentifier(id.getRoot(), practiseCode))
            .setStatus(ObservationStatus.FINAL)
            .setCode(getCode(compoundStatement.getCode()))
            .setComponent(getComponent(observationStatements))
            .setComment(
                getComment(observationStatements, getNarrativeStatementsFromCompoundStatement(compoundStatement)))
            .setSubject(new Reference(patient))
            .setIssuedElement(getIssued(
                ehrExtract,
                ehrComposition))
            .addPerformer(getParticipantReference(
                compoundStatement.getParticipant(),
                ehrComposition));

        observation.setId(id.getRoot());
        observation.getMeta().getProfile().add(new UriType(META_PROFILE));

        addEffective(observation, getEffective(compoundStatement.getEffectiveTime(), compoundStatement.getAvailabilityTime()));
        addContextToObservation(observation, encounters, ehrComposition);

        return observation;
    }

    private boolean containsValidBloodPressureTriple(RCMRMT030101UK04CompoundStatement compoundStatement) {
        var observationStatements = getObservationStatementsFromCompoundStatement(compoundStatement);

        if (observationStatements.size() == 2) {
            return BloodPressureValidatorUtil.validateBloodPressureTriple(compoundStatement.getCode().getCode(),
                observationStatements.get(0).getCode().getCode(), observationStatements.get(1).getCode().getCode());
        }

        return false;
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
            var bloodPressureText = observationStatement.getPertinentInformation().stream()
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
                })
                .collect(Collectors.joining());

            if (StringUtils.isNotEmpty(bloodPressureText)) {
                stringBuilder.append(bloodPressureText);
            }
        }

        if (!narrativeStatements.isEmpty()) {
            stringBuilder.append(BP_NOTE);
            for (RCMRMT030101UK04NarrativeStatement narrativeStatement
                : narrativeStatements) {
                stringBuilder.append(narrativeStatement.getText()).append(StringUtils.SPACE);
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
            .map(RCMRMT030101UK04Component02::getObservationStatement)
            .filter(Objects::nonNull)
            .toList();
    }

    private List<RCMRMT030101UK04NarrativeStatement> getNarrativeStatementsFromCompoundStatement(
        RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement.getComponent().stream()
            .map(RCMRMT030101UK04Component02::getNarrativeStatement)
            .filter(Objects::nonNull)
            .toList();
    }

    private void addEffective(Observation observation, Object effective) {
        if (effective instanceof DateTimeType) {
            observation.setEffective((DateTimeType) effective);
        } else if (effective instanceof Period) {
            observation.setEffective((Period) effective);
        }
    }
}
