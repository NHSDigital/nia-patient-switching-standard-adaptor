package uk.nhs.adaptors.pss.translator.mapper.diagnosticreport;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToInstantType;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.mapper.CodeableConceptMapper;

@ExtendWith(MockitoExtension.class)
public class DiagnosticReportMapperTest {

    private static final String XML_RESOURCES_BASE = "xml/DiagnosticReport/";
    private static final String DIAGNOSTIC_REPORT_META_SUFFIX = "DiagnosticReport-1";
    private static final String PRACTISE_CODE = "TEST_PRACTISE_CODE";
    private static final String DIAGNOSTIC_REPORT_ID = "DIAGNOSTIC_REPORT_ID";
    private static final String NARRATIVE_STATEMENT_ID = "NARRATIVE_STATEMENT_ID_1";
    private static final String NARRATIVE_STATEMENT_TEXT = "TEXT_OF_DIRECT_COMPOUND_STATEMENT_CHILD_NARRATIVE_STATEMENT";
    private static final String COMPOUND_STATEMENT_CHILD_ID = "COMPOUND_STATEMENT_CHILD_ID";
    private static final String ENCOUNTER_ID = "EHR_COMPOSITION_ID_1";
    private static final InstantType ISSUED_ELEMENT = parseToInstantType("20100225154100");
    private static final Patient PATIENT = (Patient) new Patient().setId("PATIENT_TEST_ID");
    private static final String CONCLUSION_FIELD_TEXT = "TEXT_OF_DIRECT_COMPOUND_STATEMENT_CHILD_NARRATIVE_STATEMENT_1\n"
        + "TEXT_OF_DIRECT_COMPOUND_STATEMENT_CHILD_NARRATIVE_STATEMENT_2";

    @SuppressWarnings("RegexpSingleline")
    private static final String NARRATIVE_STATEMENT_COMMENT_BLOCK = """
        CommentType:LABORATORY RESULT COMMENT(E141)
        CommentDate:20220308170025
        
        TEXT_OF_DIRECT_COMPOUND_STATEMENT_CHILD_NARRATIVE_STATEMENT""";

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @InjectMocks
    private DiagnosticReportMapper diagnosticReportMapper;

    @Test
    public void testDiagnosticReportIsMappedWithNoReferences() {
        RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("diagnostic_report_no_references.xml");
        List<DiagnosticReport> diagnosticReports = diagnosticReportMapper.mapResources(
            ehrExtract, PATIENT, List.of(), PRACTISE_CODE
        );
        assertThat(diagnosticReports).isNotEmpty();

        final DiagnosticReport diagnosticReport = diagnosticReports.get(0);
        assertThat(diagnosticReport.getId()).isEqualTo(DIAGNOSTIC_REPORT_ID);
        assertThat(diagnosticReport.getMeta().getProfile().get(0).getValue()).contains(DIAGNOSTIC_REPORT_META_SUFFIX);
        assertThat(diagnosticReport.getIdentifierFirstRep().getSystem()).contains(PRACTISE_CODE);
        assertThat(diagnosticReport.getIdentifierFirstRep().getValue()).isEqualTo(DIAGNOSTIC_REPORT_ID);
        assertThat(diagnosticReport.getSubject().getResource().getIdElement().getValue()).isEqualTo(PATIENT.getId());
        assertThat(diagnosticReport.getIssuedElement().getValueAsString()).isEqualTo(ISSUED_ELEMENT.getValueAsString());
        assertThat(diagnosticReport.hasContext()).isFalse();
        assertThat(diagnosticReport.getSpecimen()).isEmpty();
        assertThat(diagnosticReport.getResult()).isEmpty();
    }

    @Test
    public void testDiagnosticReportWithSpecimenReferencesMapping() {
        RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("diagnostic_report_specimen.xml");
        List<DiagnosticReport> diagnosticReports = diagnosticReportMapper.mapResources(
            ehrExtract, PATIENT, List.of(), PRACTISE_CODE
        );
        assertThat(diagnosticReports).isNotEmpty();
        assertThat(diagnosticReports.get(0).getSpecimen()).isNotEmpty();
        assertThat(diagnosticReports.get(0).getSpecimenFirstRep().getReference()).contains(COMPOUND_STATEMENT_CHILD_ID);
    }

    @Test
    public void testDiagnosticReportWithObservationReferencesMapping() {
        RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("diagnostic_report_observations.xml");
        List<DiagnosticReport> diagnosticReports = diagnosticReportMapper.mapResources(
            ehrExtract, PATIENT, List.of(), PRACTISE_CODE
        );
        assertThat(diagnosticReports).isNotEmpty();
        assertThat(diagnosticReports.get(0).getResult().size()).isEqualTo(2);
        assertThat(diagnosticReports.get(0).getResultFirstRep().getReference()).contains(NARRATIVE_STATEMENT_ID);
    }

    @Test
    public void testMappingChildObservationComments() {
        RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("diagnostic_report_observations.xml");
        List<Observation> observationComments = createObservationCommentList();
        diagnosticReportMapper.handleChildObservationComments(ehrExtract, observationComments);
        assertThat(observationComments.get(0).hasEffective()).isFalse();
        assertThat(observationComments.get(0).getComment()).isEqualTo(NARRATIVE_STATEMENT_TEXT);
    }

    @Test
    public void testMappingEncountersToContext() {
        RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("diagnostic_report_specimen.xml");
        List<DiagnosticReport> diagnosticReports = diagnosticReportMapper.mapResources(
            ehrExtract, PATIENT, createEncounterList(), PRACTISE_CODE
        );

        assertThat(diagnosticReports.get(0).hasContext()).isTrue();
        assertThat(diagnosticReports.get(0).getContext().getResource()).isNotNull();
        assertThat(diagnosticReports.get(0).getContext().getResource().getIdElement().getValue()).isEqualTo(ENCOUNTER_ID);
    }

    @Test
    public void testMappingNarrativeStatementToConclusion() {
        RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("diagnostic_report_observations.xml");
        List<DiagnosticReport> diagnosticReports = diagnosticReportMapper.mapResources(
            ehrExtract, PATIENT, createEncounterList(), PRACTISE_CODE
        );

        assertThat(diagnosticReports.get(0).hasConclusion()).isTrue();
        assertThat(diagnosticReports.get(0).getConclusion()).isEqualTo(CONCLUSION_FIELD_TEXT);
    }

    private List<Observation> createObservationCommentList() {
        Observation observationComment1 = (Observation) new Observation()
            .setEffective(new DateTimeType())
            .setComment(NARRATIVE_STATEMENT_COMMENT_BLOCK)
            .setId("NARRATIVE_STATEMENT_ID_1");
        return List.of(observationComment1);
    }

    private List<Encounter> createEncounterList() {
        return List.of((Encounter) new Encounter().setId(ENCOUNTER_ID));
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}
