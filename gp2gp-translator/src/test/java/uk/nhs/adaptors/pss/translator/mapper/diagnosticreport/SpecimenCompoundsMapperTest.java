package uk.nhs.adaptors.pss.translator.mapper.diagnosticreport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.nhs.adaptors.pss.translator.MetaFactory.MetaType.META_WITH_SECURITY;
import static uk.nhs.adaptors.pss.translator.mapper.diagnosticreport.SpecimenBatteryMapper.SpecimenBatteryParameters;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CV;
import org.hl7.v3.RCMRMT030101UKCompoundStatement;
import org.hl7.v3.RCMRMT030101UKEhrComposition;
import org.hl7.v3.RCMRMT030101UKEhrExtract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import org.mockito.stubbing.Answer1;
import uk.nhs.adaptors.pss.translator.MetaFactory;
import uk.nhs.adaptors.pss.translator.TestUtility;
import uk.nhs.adaptors.pss.translator.service.ConfidentialityService;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
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
    private static final String META_PROFILE = "Observation-1";
    private static final Meta META = MetaFactory.getMetaFor(META_WITH_SECURITY, META_PROFILE);
    private static final CV NOPAT_CV = TestUtility.createCv(
        "NOPAT",
        "http://hl7.org/fhir/v3/ActCode",
        "no disclosure to patient, family or caregivers without attending provider's authorization");

    private static final Patient PATIENT = (Patient) new Patient().setId("TEST_PATIENT_ID");

    private List<Observation> observations;
    private List<Observation> observationComments;
    private List<DiagnosticReport> diagnosticReports;

    @Mock
    private SpecimenBatteryMapper specimenBatteryMapper;

    @Mock
    private ConfidentialityService confidentialityService;

    @InjectMocks
    private SpecimenCompoundsMapper specimenCompoundsMapper;

    @BeforeEach
    public void setup() {
        observations = createObservations();
        observationComments = createObservationComments();
        diagnosticReports = createDiagnosticReports();
    }

    @Test
    public void testHandlingNoPatCompoundStatementWithObservationStatement() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("specimen_observation_statement_with_nopat_conf_code.xml");
        final var compoundStatement = getCompoundStatement(ehrExtract);
        when(confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            META_PROFILE,
            Optional.empty(),
            compoundStatement.getConfidentialityCode()
        )).thenReturn(META);

        specimenCompoundsMapper.handleSpecimenChildComponents(
            ehrExtract, observations, observationComments, diagnosticReports, PATIENT, List.of(), TEST_PRACTISE_CODE
        );

        assertParentSpecimenIsReferenced(observations.get(0));
        assertThat(observations.get(0).getIssuedElement().asStringValue()).isEqualTo("2010-02-25T15:41:00.000+00:00");
        assertThat(diagnosticReports.get(0).getResult()).isNotEmpty();

        final Reference result = diagnosticReports.get(0).getResult().get(0);

        assertMetaSecurityIsPresent(observations.get(0).getMeta());
        assertMetaSecurityIsPresent((Meta) result.getResource().getMeta());
    }

    @Test
    public void testHandlingNoPatEhrCompositionWithObservationStatement() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("specimen_ehr_composition_with_nopat_conf_code.xml");
        final var ehrComposition = getEhrComposition(ehrExtract);
        final var compoundStatement = getCompoundStatement(ehrExtract);
        when(confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            META_PROFILE,
            ehrComposition.getConfidentialityCode(),
            compoundStatement.getConfidentialityCode()
        )).thenReturn(META);

        specimenCompoundsMapper.handleSpecimenChildComponents(
            ehrExtract, observations, observationComments, diagnosticReports, PATIENT, List.of(), TEST_PRACTISE_CODE);

        assertParentSpecimenIsReferenced(observations.get(0));
        assertThat(observations.get(0).getIssuedElement().asStringValue()).isEqualTo("2010-02-25T15:41:00.000+00:00");
        assertThat(diagnosticReports.get(0).getResult()).isNotEmpty();

        final Reference result = diagnosticReports.get(0).getResult().get(0);

        assertMetaSecurityIsPresent(observations.get(0).getMeta());
        assertMetaSecurityIsPresent((Meta) result.getResource().getMeta());
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
        assertThat(observationComment.getRelated().get(0).getTarget().getResource()).isNotNull();
        assertThat(observationComment.getRelated().get(0).getTarget().getResource().getIdElement().getValue())
                .isEqualTo(observation.getId());
        assertThat(diagnosticReports.get(0).getResult().size()).isOne();
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
        assertThat(observationComments.get(0).getRelated()).isNotEmpty();
        assertThat(observationComments.get(0).getRelated().get(0).getTarget().getResource()).isNotNull();
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
        assertThat(observationComments.size()).isOne();
        assertThat(observations.get(0).getComment()).isEqualTo(TEST_COMMENT_LINE + "\n" + TEST_COMMENT_LINE_1);
    }

    @Test public void testHandlingObservationStatementWithUnkAvailabilityTime() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("specimen_cluster_compound_statement_availability_time_unk.xml");

        specimenCompoundsMapper.handleSpecimenChildComponents(
            ehrExtract, observations, observationComments, diagnosticReports, PATIENT, List.of(), TEST_PRACTISE_CODE
        );

        final Observation observation = observations.get(0);

        assertThat(observation.getIssuedElement().asStringValue()).isNull();
    }

    @Test public void testOrderingIsPreservedForDiagnosticReportResults() {
        final RCMRMT030101UKEhrExtract ehrExtract =
            unmarshallEhrExtract("specimen_with_three_test_group_headers.xml");

        final var testObservations = List.of(
            (Observation) new Observation().setId("TEST-GROUP-HEADER-1"),
            (Observation) new Observation().setId("OBSERVATION-STATEMENT-ID-1"),
            (Observation) new Observation().setId("TEST-GROUP-HEADER-2"),
            (Observation) new Observation().setId("OBSERVATION-STATEMENT-ID-2"),
            (Observation) new Observation().setId("TEST-GROUP-HEADER-3"),
            (Observation) new Observation().setId("OBSERVATION-STATEMENT-ID-3")
        );

        doAnswer(
            answer(
                (Answer1<Observation, SpecimenBatteryParameters>) batteryParameters -> {
                    var id = batteryParameters.getBatteryCompoundStatement().getId().get(0).getRoot();
                    var observation = new Observation();
                    observation.setId(id);

                    batteryParameters.getDiagnosticReport().addResult(
                        new Reference(new IdType(ResourceType.Observation.name(), id)));
                    return observation;
                })
        ).when(specimenBatteryMapper).mapBatteryObservation(any(SpecimenBatteryMapper.SpecimenBatteryParameters.class));

        specimenCompoundsMapper.handleSpecimenChildComponents(
            ehrExtract,
            testObservations,
            observationComments,
            diagnosticReports,
            PATIENT,
            List.of(),
            TEST_PRACTISE_CODE
        );

        var diagnosticReport = diagnosticReports.get(0);

        assertAll(
            () -> assertThat(getReferenceId(diagnosticReport.getResult().get(0)))
                .isEqualTo("Observation/TEST-GROUP-HEADER-1"),
            () -> assertThat(getReferenceId(diagnosticReport.getResult().get(1)))
                .isEqualTo("OBSERVATION-STATEMENT-ID-2"),
            () -> assertThat(getReferenceId(diagnosticReport.getResult().get(2)))
                .isEqualTo("OBSERVATION-STATEMENT-ID-3")
        );
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

    private String getReferenceId(Reference reference) {
        return reference.hasReference()
            ? reference.getReference()
            : reference.getResource().getIdElement().getValue();
    }

    private void assertMetaSecurityIsPresent(final Meta meta) {
        final List<Coding> metaSecurity = meta.getSecurity();
        final int metaSecuritySize = metaSecurity.size();
        final Coding metaSecurityCoding = metaSecurity.get(0);
        final UriType metaProfile = meta.getProfile().get(0);

        assertAll(
            () -> assertThat(metaSecuritySize).isEqualTo(1),
            () -> assertThat(metaProfile.getValue()).isEqualTo(META_PROFILE),
            () -> assertThat(metaSecurityCoding.getCode()).isEqualTo(NOPAT_CV.getCode()),
            () -> assertThat(metaSecurityCoding.getDisplay()).isEqualTo(NOPAT_CV.getDisplayName()),
            () -> assertThat(metaSecurityCoding.getSystem()).isEqualTo(NOPAT_CV.getCodeSystem())
        );
    }


    @SneakyThrows
    private RCMRMT030101UKEhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UKEhrExtract.class);
    }

    private RCMRMT030101UKEhrComposition getEhrComposition(RCMRMT030101UKEhrExtract ehrExtract) {

        return ehrExtract.getComponent().get(0)
            .getEhrFolder().getComponent().get(0)
            .getEhrComposition();
    }

    private RCMRMT030101UKCompoundStatement getCompoundStatement(RCMRMT030101UKEhrExtract ehrExtract) {
        return ehrExtract.getComponent().get(0)
            .getEhrFolder().getComponent().get(0)
            .getEhrComposition().getComponent().get(0)
            .getCompoundStatement().getComponent().get(0)
            .getCompoundStatement();
    }

}
