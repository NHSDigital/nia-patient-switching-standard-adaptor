package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToInstantType;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Specimen;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
public class DiagnosticReportMapperTest {

    private static final String XML_RESOURCES_BASE = "xml/DiagnosticReport/";
    private static final String DIAGNOSTIC_REPORT_META_SUFFIX = "DiagnosticReport-1";
    private static final String PRACTISE_CODE = "TEST_PRACTISE_CODE";
    private static final String DIAGNOSTIC_REPORT_ID = "DIAGNOSTIC_REPORT_ID";
    private static final String NARRATIVE_STATEMENT_ID = "NARRATIVE_STATEMENT_ID_1";
    private static final String NARRATIVE_STATEMENT_TEXT = "TEXT_OF_DIRECT_COMPOUND_STATEMENT_CHILD_NARRATIVE_STATEMENT";
    private static final String COMPOUND_STATEMENT_CHILD_ID = "COMPOUND_STATEMENT_CHILD_ID";
    private static final InstantType ISSUED_ELEMENT = parseToInstantType("20220308163805");
    private static final Patient PATIENT = (Patient) new Patient().setId("PATIENT_TEST_ID");

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @InjectMocks
    private DiagnosticReportMapper diagnosticReportMapper;

    @Test
    public void testDiagnosticReportIsMappedWithNoReferences() {
        RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("diagnostic_report_no_references.xml");
        List<DiagnosticReport> diagnosticReports = diagnosticReportMapper.mapDiagnosticReports(
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
        assertThat(diagnosticReport.getSpecimen()).isEmpty();
        assertThat(diagnosticReport.getResult()).isEmpty();
        assertThat(diagnosticReport.getResultFirstRep().getReference()).contains(NARRATIVE_STATEMENT_ID);
    }

    @Test
    public void testDiagnosticReportWithSpecimenReferencesMapping() {
        RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("diagnostic_report_specimen.xml");
        var diagnosticReports = diagnosticReportMapper.mapDiagnosticReports(ehrExtract, PATIENT, List.of(), PRACTISE_CODE);
        assertThat(diagnosticReports).isNotEmpty();
        assertThat(diagnosticReports.get(0).getSpecimen()).isNotEmpty();
        assertThat(diagnosticReports.get(0).getSpecimenFirstRep().getReference()).contains(COMPOUND_STATEMENT_CHILD_ID);
    }

    @Test
    public void testDiagnosticReportWithObservationReferencesMapping() {
        RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("diagnostic_report_observations.xml");
        var diagnosticReports = diagnosticReportMapper.mapDiagnosticReports(ehrExtract, PATIENT, List.of(), PRACTISE_CODE);
        assertThat(diagnosticReports).isNotEmpty();
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}
