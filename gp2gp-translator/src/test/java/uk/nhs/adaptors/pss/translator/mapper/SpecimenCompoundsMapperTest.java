package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
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

    private List<Observation> observations;
    private List<Observation> observationComments;
    private List<DiagnosticReport> diagnosticReports;

    @Mock
    private DateTimeMapper dateTimeMapper;

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
            ehrExtract, observations, observationComments, diagnosticReports
        );

        assertParentSpecimenIsReferenced(observations.get(0));
        assertThat(diagnosticReports.get(0).getResult()).isNotEmpty();

        final Reference result = diagnosticReports.get(0).getResult().get(0);
        assertThat(result.getResource()).isNotNull();
        assertThat(result.getResource().getIdElement().getValue()).isEqualTo("OBSERVATION_STATEMENT_ID");
    }

    @Test
    public void testHandlingSpecimenChildClusterCompoundStatement() {
        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("specimen_cluster_compound_statement.xml");
        specimenCompoundsMapper.handleSpecimenChildComponents(
            ehrExtract, observations, observationComments, diagnosticReports
        );

        final Observation observation = observations.get(0);
        final Observation observationComment = observationComments.get(0);
        assertParentSpecimenIsReferenced(observation);

        assertThat(observationComment.getComment()).isEqualTo("Test Comment");
        assertThat(observationComment.getRelated()).isNotEmpty();
        assertThat(observationComment.getRelatedFirstRep().getTarget().getResource()).isNotNull();
        assertThat(observationComment.getRelatedFirstRep().getTarget().getResource().getIdElement().getValue())
            .isEqualTo("OBSERVATION_STATEMENT_ID");

        assertThat(observation.getRelated()).isNotEmpty();
        assertThat(observation.getRelatedFirstRep().getTarget().getResource()).isNotNull();
        assertThat(observation.getRelatedFirstRep().getTarget().getResource().getIdElement().getValue())
            .isEqualTo("NARRATIVE_STATEMENT_ID");
    }

    @Test
    public void testHandlingSpecimenChildBatteryCompoundStatement() {
        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("specimen_battery_compound_statement.xml");
        specimenCompoundsMapper.handleSpecimenChildComponents(
            ehrExtract, observations, observationComments, diagnosticReports
        );

        assertParentSpecimenIsReferenced(observations.get(0));
        assertParentSpecimenIsReferenced(observations.get(1));
        assertThat(observationComments.get(0).getComment()).isEqualTo("Test Comment");
    }

    @Test
    public void testHandlingUserNarrativeStatement() {
        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("specimen_user_narrative_statement.xml");
        specimenCompoundsMapper.handleSpecimenChildComponents(
            ehrExtract, observations, observationComments, diagnosticReports
        );

        assertParentSpecimenIsReferenced(observations.get(0));
        assertThat(observations.get(0).getRelated()).isNotEmpty();
        assertThat(observations.get(0).getComment()).isEqualTo("First comment line");
    }

    @Test
    public void testHandlingNonUserNarrativeStatement() {
        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("specimen_non_user_narrative_statement.xml");
        specimenCompoundsMapper.handleSpecimenChildComponents(
            ehrExtract, observations, observationComments, diagnosticReports
        );

        assertParentSpecimenIsReferenced(observations.get(0));
        assertThat(observations.get(0).getComment()).isEqualTo("First comment line\n" + "Test Comment");
    }

    private void assertParentSpecimenIsReferenced(Observation observation) {
        assertThat(observation.hasSpecimen()).isTrue();
        assertThat(observation.getSpecimen().hasReference()).isTrue();
        assertThat(observation.getSpecimen().getReference()).contains("TEST_SPECIMEN_ID");
    }

    private List<DiagnosticReport> createDiagnosticReports() {
        DiagnosticReport diagnosticReport = (DiagnosticReport) new DiagnosticReport().setId("DR_TEST_ID");
        return List.of(diagnosticReport);
    }

    private List<Observation> createObservations() {
        Observation observation = (Observation) new Observation()
            .setComment("First comment line")
            .setId("OBSERVATION_STATEMENT_ID");
        Observation observation1 = (Observation) new Observation().setId("BATTERY_DIRECT_CHILD_OBSERVATION_STATEMENT");
        return List.of(observation, observation1);
    }

    private List<Observation> createObservationComments() {
        Observation observationCommentUser = (Observation) new Observation()
            .setComment("""
                CommentType:USER COMMENT
                CommentDate:20100223000000

                Test Comment""")
            .setId("NARRATIVE_STATEMENT_ID");

        Observation observationCommentNonUser = (Observation) new Observation()
            .setComment("""
                CommentType:LAB COMMENT
                CommentDate:20100223000000

                Test Comment""")
            .setId("NARRATIVE_STATEMENT_ID_1");
        return List.of(observationCommentUser, observationCommentNonUser);
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }

}
