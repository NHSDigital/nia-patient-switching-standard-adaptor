package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
public class DiagnosticReportMapperTest {

    private static final String XML_RESOURCES_BASE = "xml/DiagnosticReport/";

    @InjectMocks
    private DiagnosticReportMapper diagnosticReportMapper;

    private Patient patient;

    @Before
    public void setup() {
        patient = (Patient) new Patient().setId("PATIENT_TEST_ID");
    }

    @Test
    public void testIt() {
        RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("diagnostic_report.xml");
        var diagnosticReports = diagnosticReportMapper.mapDiagnosticReports(ehrExtract, patient, List.of());
        assertThat(diagnosticReports).isNotEmpty();
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}
