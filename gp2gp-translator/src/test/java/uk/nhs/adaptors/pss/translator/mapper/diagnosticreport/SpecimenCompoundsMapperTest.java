package uk.nhs.adaptors.pss.translator.mapper.diagnosticreport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.mapper.CodeableConceptMapper;
import uk.nhs.adaptors.pss.translator.mapper.DateTimeMapper;

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
    private DateTimeMapper dateTimeMapper;

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

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
        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("specimen_observation_statement.xml");
        specimenCompoundsMapper.handleSpecimenChildComponents(
            ehrExtract, observations, observationComments, diagnosticReports, PATIENT, List.of(), TEST_PRACTISE_CODE
        );

        assertParentSpecimenIsReferenced(observations.get(0));
        assertThat(diagnosticReports.get(0).getResult()).isNotEmpty();

        final Reference result = diagnosticReports.get(0).getResult().get(0);
        assertThat(result.getResource()).isNotNull();
        assertThat(result.getResource().getIdElement().getValue()).isEqualTo(OBSERVATION_STATEMENT_ID);
    }

    @Test
    public void testHandlingSpecimenChildClusterCompoundStatement() {
        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("specimen_cluster_compound_statement.xml");
        specimenCompoundsMapper.handleSpecimenChildComponents(
            ehrExtract, observations, observationComments, diagnosticReports, PATIENT, List.of(), TEST_PRACTISE_CODE
        );

        final Observation observation = observations.get(0);
        final Observation observationComment = observationComments.get(0);
        assertParentSpecimenIsReferenced(observation);

        assertThat(observationComment.getComment()).isEqualTo(TEST_COMMENT_LINE_1);
        assertThat(observationComment.getRelated()).isNotEmpty();
        assertThat(observationComment.getRelatedFirstRep().getTarget().getResource()).isNotNull();
        assertThat(observationComment.getRelatedFirstRep().getTarget().getResource().getIdElement().getValue())
            .isEqualTo(OBSERVATION_STATEMENT_ID);

        assertThat(observation.getRelated()).isNotEmpty();
        assertThat(observation.getRelatedFirstRep().getTarget().getResource()).isNotNull();
        assertThat(observation.getRelatedFirstRep().getTarget().getResource().getIdElement().getValue())
            .isEqualTo(NARRATIVE_STATEMENT_ID);
    }

    @Test
    public void testHandlingSpecimenChildBatteryCompoundStatement() {
        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("specimen_battery_compound_statement.xml");
        specimenCompoundsMapper.handleSpecimenChildComponents(
            ehrExtract, observations, observationComments, diagnosticReports, PATIENT, List.of(), TEST_PRACTISE_CODE
        );

        assertParentSpecimenIsReferenced(observations.get(0));
        assertParentSpecimenIsReferenced(observations.get(1));
        assertThat(observationComments.get(0).getComment()).isEqualTo(TEST_COMMENT_LINE_1);
    }

    @Test
    public void testHandlingUserNarrativeStatement() {
        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("specimen_user_narrative_statement.xml");
        specimenCompoundsMapper.handleSpecimenChildComponents(
            ehrExtract, observations, observationComments, diagnosticReports, PATIENT, List.of(), TEST_PRACTISE_CODE
        );

        assertParentSpecimenIsReferenced(observations.get(0));
        assertThat(observations.get(0).getRelated()).isNotEmpty();
        assertThat(observations.get(0).getComment()).isEqualTo(TEST_COMMENT_LINE);
    }

    @Test
    public void testHandlingNonUserNarrativeStatement() {
        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("specimen_non_user_narrative_statement.xml");
        specimenCompoundsMapper.handleSpecimenChildComponents(
            ehrExtract, observations, observationComments, diagnosticReports, PATIENT, List.of(), TEST_PRACTISE_CODE
        );

        assertParentSpecimenIsReferenced(observations.get(0));
        assertThat(observations.get(0).getComment()).isEqualTo(TEST_COMMENT_LINE + "\n" + TEST_COMMENT_LINE_1);
    }

    private void assertParentSpecimenIsReferenced(Observation observation) {
        assertThat(observation.hasSpecimen()).isTrue();
        assertThat(observation.getSpecimen().hasReference()).isTrue();
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

                Test Comment""")
            .setId(NARRATIVE_STATEMENT_ID);

        Observation observationCommentNonUser = (Observation) new Observation()
            .setComment("""
                CommentType:LAB COMMENT
                CommentDate:20100223000000

                Test Comment""")
            .setId(NARRATIVE_STATEMENT_ID_1);
        return List.of(observationCommentUser, observationCommentNonUser);
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }

}
