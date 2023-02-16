package uk.nhs.adaptors.pss.translator.mapper.diagnosticreport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hl7.fhir.dstu3.model.Observation.ObservationStatus;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToDateTimeType;
import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToInstantType;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationRelationshipType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.mapper.CodeableConceptMapper;
import uk.nhs.adaptors.pss.translator.mapper.diagnosticreport.SpecimenBatteryMapper.SpecimenBatteryParameters;
import uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors;

@ExtendWith(MockitoExtension.class)
public class SpecimenBatteryMapperTest {

    private static final String RESOURCES_BASE = "xml/SpecimenBattery/";

    private static final String BATTERY_CLASSCODE = "BATTERY";
    private static final String PRACTISE_CODE = "TEST_PRACTISE_CODE";
    private static final String OBSERVATION_ID = "SPECIMEN_CHILD_BATTERY_COMPOUND_STATEMENT_ID_1";
    private static final String OBSERVATION_STATEMENT_ID_1 = "BATTERY_DIRECT_CHILD_OBSERVATION_STATEMENT";
    private static final String OBSERVATION_STATEMENT_ID_2 = "OBSERVATION_STATEMENT_ID";
    private static final String DIAGNOSTIC_REPORT_ID = "DIAGNOSTIC_REPORT_ID";
    private static final String ENCOUNTER_ID = "ENCOUNTER_ID";
    private static final String PATIENT_ID = "TEST_PATIENT_ID";
    private static final String SPECIMEN_ID = "TEST_SPECIMEN_ID_1";
    private static final String META_PROFILE_SUFFIX = "Observation-1";
    private static final String EXPECTED_COMMENT = "Looks like Covid";
    private static final Patient PATIENT = (Patient) new Patient().setId(PATIENT_ID);
    private static final DiagnosticReport DIAGNOSTIC_REPORT = (DiagnosticReport) new DiagnosticReport().setId(DIAGNOSTIC_REPORT_ID);
    private static final InstantType OBSERVATION_ISSUED = parseToInstantType("202203021160700");
    private static final DateTimeType OBSERVATION_EFFECTIVE = parseToDateTimeType("20100223000000");

    private final List<Encounter> encounters = generateEncounters();

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @InjectMocks
    private SpecimenBatteryMapper specimenBatteryMapper;

    @Test
    public void testMappingObservationFromBatteryCompoundStatement() {
        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("specimen_battery_compound_statement.xml");
        var batteryCompoundStatement = getBatteryCompoundStatements(ehrExtract);

        final List<Observation> observations = getObservations();
        final List<Observation> observationComments = getObservationComments();

        var batteryParameters = SpecimenBatteryParameters.builder()
            .ehrExtract(ehrExtract)
            .batteryCompoundStatement(batteryCompoundStatement)
            .specimenCompoundStatement(getSpecimenCompoundStatement(ehrExtract))
            .ehrComposition(getEhrComposition(ehrExtract))
            .diagnosticReport(DIAGNOSTIC_REPORT)
            .patient(PATIENT)
            .encounters(encounters)
            .practiseCode(PRACTISE_CODE)
            .observations(observations)
            .observationComments(observationComments)
            .build();

        final Observation observation = specimenBatteryMapper.mapBatteryObservation(batteryParameters);

        assertThat(observation.getId()).isEqualTo(OBSERVATION_ID);
        assertThat(observation.getIdentifierFirstRep().getSystem()).contains(PRACTISE_CODE);
        assertThat(observation.getEffectiveDateTimeType().getValueAsString()).isEqualTo(OBSERVATION_EFFECTIVE.getValueAsString());
        assertThat(observation.getIssuedElement().getValueAsString()).isEqualTo(OBSERVATION_ISSUED.getValueAsString());
        assertThat(observation.getSpecimen().hasReference()).isTrue();
        assertThat(observation.getSpecimen().getReference()).contains(SPECIMEN_ID);
        assertThat(observation.getStatus()).isEqualTo(ObservationStatus.FINAL);
        assertThat(observation.getMeta().getProfile().get(0).getValue()).contains(META_PROFILE_SUFFIX);
        assertThat(observation.getComment()).isEqualTo(EXPECTED_COMMENT);
        assertThat(observation.getContext().hasReference()).isTrue();
        assertThat(observation.getContext().getReference()).contains(ENCOUNTER_ID);

        assertThat(observations.get(0).getRelated()).isNotEmpty();
        assertThat(observations.get(0).getRelatedFirstRep().getType()).isEqualTo(ObservationRelationshipType.DERIVEDFROM);

        assertSubject(observation);
        assertRelated(observation);

        assertThat(observationComments.size()).isEqualTo(2);

        var observationCommentIds = observationComments.stream()
            .map(Observation::getId)
            .toList();

        assertThat(observationCommentIds.contains("BATTERY_DIRECT_CHILD_NARRATIVE_STATEMENT_ID")).isFalse();
    }

    private List<Observation> getObservationComments() {
        List<Observation> observationComments = new ArrayList<>();
        var batteryObservationComment = new Observation()
            .setComment("""
                           CommentType:SUPER COMMENT
                           CommentDate:20220321163025

                           Looks like Covid
                           """);

        batteryObservationComment.setId("BATTERY_DIRECT_CHILD_NARRATIVE_STATEMENT_ID");

        var otherClusterObservationComment = new Observation()
            .setComment("""
                CommentType:OTHER COMMENT
                CommentDate:20220321162705

                Or maybe not?
                """);

        otherClusterObservationComment.setId("OTHER_COMMENT_NARRATIVE_STATEMENT_ID");

        var userObservationComment = new Observation()
            .setComment("""
                CommentType:USER COMMENT
                CommentDate:20100223000000

                This should not be a part of Battery Observation.comment
                """);

        userObservationComment.setId("USER_COMMENT_NARRATIVE_STATEMENT_ID");

        observationComments.add(batteryObservationComment);
        observationComments.add(otherClusterObservationComment);
        observationComments.add(userObservationComment);

        return observationComments;
    }

    private void assertRelated(Observation observation) {
        assertThat(observation.getRelated()).isNotEmpty();
        assertThat(observation.getRelatedFirstRep().hasTarget()).isTrue();
        assertThat(observation.getRelatedFirstRep().getTarget().getReference()).contains(OBSERVATION_STATEMENT_ID_1);
        assertThat(observation.getRelated().get(1).getTarget().getReference()).contains(OBSERVATION_STATEMENT_ID_2);
    }

    private void assertSubject(Observation observation) {
        assertThat(observation.getSubject()).isNotNull();
        assertThat(observation.getSubject().getResource()).isNotNull();
        assertThat(observation.getSubject().getResource().getIdElement().getValue()).isEqualTo(PATIENT_ID);
    }

    private RCMRMT030101UK04EhrComposition getEhrComposition(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent().get(0).getEhrFolder().getComponent().get(0).getEhrComposition();
    }

    private RCMRMT030101UK04CompoundStatement getSpecimenCompoundStatement(RCMRMT030101UK04EhrExtract ehrExtract) {
        return getEhrComposition(ehrExtract).getComponent().get(0).getCompoundStatement()
            .getComponent().get(0).getCompoundStatement();
    }

    private List<Observation> getObservations() {
        return List.of(
            (Observation) new Observation().setId(OBSERVATION_STATEMENT_ID_1),
            (Observation) new Observation().setId(OBSERVATION_STATEMENT_ID_2)
        );
    }

    private RCMRMT030101UK04CompoundStatement getBatteryCompoundStatements(RCMRMT030101UK04EhrExtract ehrExtract) {
        return getEhrComposition(ehrExtract).getComponent()
            .stream()
            .flatMap(CompoundStatementResourceExtractors::extractAllCompoundStatements)
            .filter(compoundStatement -> BATTERY_CLASSCODE.equals(compoundStatement.getClassCode().get(0)))
            .findFirst().get();
    }

    private List<Encounter> generateEncounters() {
        return List.of((Encounter) new Encounter().setId(ENCOUNTER_ID));
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtract(String filename) {
        return unmarshallFile(getFile("classpath:" + RESOURCES_BASE + filename), RCMRMT030101UK04EhrExtract.class);
    }
}

