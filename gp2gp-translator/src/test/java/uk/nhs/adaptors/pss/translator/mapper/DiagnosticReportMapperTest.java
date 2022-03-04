package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.Date;
import java.util.List;

import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
public class DiagnosticReportMapperTest {

    private static final String XML_RESOURCES_BASE = "xml/DiagnosticReport/";
    private static final String PRACTISE_CODE = "TEST_PRACTISE_CODE";

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @Mock
    private ObservationCommentMapper observationCommentMapper;

    @Mock
    private SpecimenMapper specimenMapper;

    @Mock
    private ObservationMapper observationMapper;

    @Mock
    private SpecimenCompoundsMapper specimenCompoundsMapper;

    @InjectMocks
    private DiagnosticReportMapper diagnosticReportMapper;

    private Patient patient;

    @BeforeEach
    public void setup() {
        patient = (Patient) new Patient().setId("PATIENT_TEST_ID");
    }

    @Test
    public void testIt() {
        RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("diagnostic_report.xml");
        var diagnosticReports = diagnosticReportMapper.mapDiagnosticReports(ehrExtract, patient, List.of());
        assertThat(diagnosticReports).isNotEmpty();
    }

    @Test
    public void testRelatedObservationsMapping() {
        RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("diagnostic_report_with_observations_and_specimen.xml");
        var diagnosticReports = diagnosticReportMapper.mapDiagnosticReports(ehrExtract, patient, List.of());
        var observations = diagnosticReportMapper.mapChildrenObservationComments(ehrExtract, patient, List.of(), PRACTISE_CODE);
        var specimen = diagnosticReportMapper.mapSpecimen(ehrExtract, diagnosticReports, patient);
        assertThat(diagnosticReports).isNotEmpty();
    }

    @Test
    public void testNestedCompoundStatementsMapping() {
        RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("diagnostic_report_nested_compound.xml");
        var diagnosticReports = diagnosticReportMapper.mapDiagnosticReports(ehrExtract, patient, List.of());
        assertThat(diagnosticReports).isNotEmpty();
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}
