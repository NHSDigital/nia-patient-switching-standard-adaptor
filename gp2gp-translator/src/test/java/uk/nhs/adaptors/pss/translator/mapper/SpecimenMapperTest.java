package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
public class SpecimenMapperTest {

    private static final String XML_RESOURCES_BASE = "xml/Specimen/";
    private static final Patient PATIENT = (Patient) new Patient().setId("PATIENT_TEST_ID");
    private static final String SPECIMEN_PREFIX = "Specimen/";
    private static final String TEST_SPECIMEN_ID = "COMPOUND_STATEMENT_CHILD_ID_1";

    @Mock
    private DateTimeMapper dateTimeMapper;

    @InjectMocks
    private SpecimenMapper specimenMapper;

    @Test
    public void testSpecimenIsMapped() {
        RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("compound_statement_with_specimen.xml");
        var specimenList = specimenMapper.mapSpecimen(ehrExtract, generateDiagnosticReportWithSpecimenReference(), PATIENT);
        assertThat(specimenList).isNotEmpty();
        assertThat(specimenList.get(0).getId()).isEqualTo(TEST_SPECIMEN_ID);
    }

    private List<DiagnosticReport> generateDiagnosticReportWithSpecimenReference() {
        DiagnosticReport diagnosticReport = (DiagnosticReport) new DiagnosticReport().setId("DIAGNOSTIC_REPORT_ID");
        List<Reference> specimen = List.of(new Reference().setReference(SPECIMEN_PREFIX + TEST_SPECIMEN_ID));
        diagnosticReport.setSpecimen(specimen);

        return List.of(diagnosticReport);
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }

}
