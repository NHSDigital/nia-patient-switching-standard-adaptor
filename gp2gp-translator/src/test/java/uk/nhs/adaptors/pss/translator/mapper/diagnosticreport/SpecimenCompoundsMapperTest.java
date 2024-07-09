package uk.nhs.adaptors.pss.translator.mapper.diagnosticreport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.RCMRMT030101UKEhrExtract;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
public class SpecimenCompoundsMapperTest {

    private static final String XML_RESOURCES_BASE = "xml/SpecimenComponents/";

    private static final String OBSERVATION_STATEMENT_ID = "OBSERVATION_STATEMENT_ID";
    private static final String NARRATIVE_STATEMENT_ID = "NARRATIVE_STATEMENT_ID";
    private static final String NARRATIVE_STATEMENT_ID_1 = "NARRATIVE_STATEMENT_ID_1";
    private static final String BATTERY_OBSERVATION_STATEMENT_ID = "BATTERY_DIRECT_CHILD_OBSERVATION_STATEMENT";
    private static final String TEST_PRACTISE_CODE = "TEST_PRACTISE_CODE";
    private static final String DIAGNOSTIC_REPORT_ID = "DR_TEST_ID";
    private static final String SPECIMEN_ID = "TEST_SPECIMEN_ID";
    private static final String TEST_COMMENT_LINE = "First comment Line";
    private static final String TEST_COMMENT_LINE_1 = "Test Comment";

    private static final Patient PATIENT = (Patient) new Patient().setId("TEST_PATIENT_ID");

    private List<Observation> observations;
    private List<Observation> observationComments;
    private List<DiagnosticReport> diagnosticReports;

    @Mock
    private SpecimenBatteryMapper specimenBatteryMapper;

    @InjectMocks
    private SpecimenCompoundsMapper specimenCompoundsMapper;

    @BeforeEach
    public void setup() {
        observations = createObservations();
        observationComments = createObservationComments();
        diagnosticReports = createDiagnosticReports();
    }

    @Test
    public void testHandlingFirstLevelObservationStatement() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("specimen_observation_statement.xml");
        specimenCompoundsMapper.handleSpecimenChildComponents(
            ehrExtract, observations, observationComments, diagnosticReports, PATIENT, List.of(), TEST_PRACTISE_CODE
        );

        assertParentSpecimenIsReferenced(observations.get(0));
        assertThat(observations.get(0).getIssuedElement().asStringValue()).isEqualTo("2010-02-25T15:41:00.000+00:00");
        assertThat(diagnosticReports.get(0).getResult()).isNotEmpty();

        final Reference result = diagnosticReports.get(0).getResult().get(0);
        assertNotNull(result.getResource());
        assertThat(result.getResource().getIdElement().getValue()).isEqualTo(OBSERVATION_STATEMENT_ID);
    }

    @Test
    public void testHandlingSpecimenChildClusterAndBatteryCompoundStatement() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("specimen_with_cluster_and_battery_compound_statements.xml");

        specimenCompoundsMapper.handleSpecimenChildComponents(
            ehrExtract, observations, observationComments, diagnosticReports, PATIENT, List.of(), TEST_PRACTISE_CODE
        );

        assertNotNull(diagnosticReports);
        assertThat(diagnosticReports.get(0).getResult()).hasSize(1);
    }

    @Test
    public void testHandlingSpecimenChildClusterCompoundStatement() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("specimen_cluster_compound_statement.xml");
        specimenCompoundsMapper.handleSpecimenChildComponents(
            ehrExtract, observations, observationComments, diagnosticReports, PATIENT, List.of(), TEST_PRACTISE_CODE
        );

        final Observation observation = observations.get(0);

        final Observation observationComment = observationComments.get(0);

        assertParentSpecimenIsReferenced(observation);
        assertThat(observation.getIssuedElement().asStringValue()).isEqualTo("2010-02-25T15:41:00.000+00:00");

        assertThat(observation.getRelated()).isEmpty();
        assertThat(observationComments).hasSize(2);
        assertThat(observationComment.getComment()).isEqualTo(TEST_COMMENT_LINE_1);
        assertThat(observationComment.getRelated()).isNotEmpty();
        assertNotNull(observationComment.getRelated().get(0).getTarget().getResource());
        assertThat(observationComment.getRelated().get(0).getTarget().getResource().getIdElement().getValue())
                .isEqualTo(observation.getId());
        assertThat(diagnosticReports.get(0).getResult()).hasSize(1);
    }

    @Test
    public void testHandlingSpecimenChildBatteryCompoundStatement() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("specimen_battery_compound_statement.xml");
        specimenCompoundsMapper.handleSpecimenChildComponents(
            ehrExtract, observations, observationComments, diagnosticReports, PATIENT, List.of(), TEST_PRACTISE_CODE
        );

        assertParentSpecimenIsReferenced(observations.get(0));
        assertThat(observations.get(0).getIssuedElement().asStringValue()).isEqualTo("2010-02-25T15:41:00.000+00:00");
        assertParentSpecimenIsReferenced(observations.get(1));
        assertThat(observations.get(0).getIssuedElement().asStringValue()).isEqualTo("2010-02-25T15:41:00.000+00:00");
        assertThat(observationComments).hasSize(2);
        assertThat(observationComments.get(0).getComment()).isEqualTo(TEST_COMMENT_LINE_1);

        assertThat(diagnosticReports.get(0).getResult()).isEmpty();
        verify(specimenBatteryMapper, times(1)).mapBatteryObservation(any());
    }

    @Test
    public void testHandlingUserNarrativeStatement() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("specimen_user_narrative_statement.xml");
        specimenCompoundsMapper.handleSpecimenChildComponents(
            ehrExtract, observations, observationComments, diagnosticReports, PATIENT, List.of(), TEST_PRACTISE_CODE
        );

        assertParentSpecimenIsReferenced(observations.get(0));
        assertThat(observations.get(0).getIssuedElement().asStringValue()).isEqualTo("2022-03-14T18:24:45.000+00:00");
        assertThat(observationComments).hasSize(2);
        assertThat(observationComments.get(0).getRelated()).isNotEmpty();
        assertNotNull(observationComments.get(0).getRelated().get(0).getTarget().getResource());
        assertThat(observationComments.get(0).getRelated().get(0).getTarget().getResource().getIdElement().getValue())
                .isEqualTo(observations.get(0).getId());
        assertThat(observations.get(0).getComment()).isEqualTo(TEST_COMMENT_LINE);
    }

    @Test
    public void testHandlingNonUserNarrativeStatement() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("specimen_non_user_narrative_statement.xml");
        specimenCompoundsMapper.handleSpecimenChildComponents(
            ehrExtract, observations, observationComments, diagnosticReports, PATIENT, List.of(), TEST_PRACTISE_CODE
        );

        assertParentSpecimenIsReferenced(observations.get(0));
        assertThat(observations.get(0).getIssuedElement().asStringValue()).isEqualTo("2022-03-14T18:24:45.000+00:00");
        assertThat(observationComments).hasSize(1);
        assertThat(observations.get(0).getComment()).isEqualTo(TEST_COMMENT_LINE + "\n" + TEST_COMMENT_LINE_1);
    }

    @Test public void testHandlingObservationStatementWithUnkAvailabilityTime() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("specimen_cluster_compound_statement_availability_time_unk.xml");

        specimenCompoundsMapper.handleSpecimenChildComponents(
            ehrExtract, observations, observationComments, diagnosticReports, PATIENT, List.of(), TEST_PRACTISE_CODE
        );

        final Observation observation = observations.get(0);

        assertNull(observation.getIssuedElement().asStringValue());
    }

    private void assertParentSpecimenIsReferenced(Observation observation) {
        assertTrue(observation.hasSpecimen());
        assertTrue(observation.getSpecimen().hasReference());
        assertThat(observation.getSpecimen().getReference()).contains(SPECIMEN_ID);
    }

    private List<DiagnosticReport> createDiagnosticReports() {
        return List.of((DiagnosticReport) new DiagnosticReport().setId(DIAGNOSTIC_REPORT_ID));
    }

    private List<Observation> createObservations() {
        Observation observation = (Observation) new Observation()
            .setComment(TEST_COMMENT_LINE)
            .setId(OBSERVATION_STATEMENT_ID);
        Observation observation1 = (Observation) new Observation().setId(BATTERY_OBSERVATION_STATEMENT_ID);
        return List.of(observation, observation1);
    }

    private List<Observation> createObservationComments() {
        Observation observationCommentUser = (Observation) new Observation()
            .setComment("""
                CommentType:USER COMMENT
                CommentDate:20100223000000

                Test Comment
                """)
            .setId(NARRATIVE_STATEMENT_ID);

        Observation observationCommentNonUser = (Observation) new Observation()
            .setComment("""
                CommentType:LAB COMMENT
                CommentDate:20100223000000

                Test Comment
                """)
            .setId(NARRATIVE_STATEMENT_ID_1);

        List<Observation> observationComments = new ArrayList<>();
        observationComments.add(observationCommentUser);
        observationComments.add(observationCommentNonUser);

        return observationComments;
    }

    @SneakyThrows
    private RCMRMT030101UKEhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UKEhrExtract.class);
    }

}
