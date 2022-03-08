package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToDateTimeType;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
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
public class SpecimenMapperTest {

    private static final String XML_RESOURCES_BASE = "xml/Specimen/";
    private static final String SPECIMEN_META_PROFILE_SUFFIX = "Specimen-1";
    private static final String SPECIMEN_PREFIX = "Specimen/";
    private static final String TEST_SPECIMEN_ID = "COMPOUND_STATEMENT_CHILD_ID_1";
    private static final String PRACTICE_CODE = "TEST_PRACTICE_CODE";
    private static final String ACCESSION_IDENTIFIER_VALUE = "SPECIMEN_ROLE_ID_EXTENSION";
    private static final String SPECIMEN_TYPE_TEXT = "EINE KLEINE";
    private static final Patient PATIENT = (Patient) new Patient().setId("PATIENT_TEST_ID");
    private static final DateTimeType SPECIMEN_COLLECTED_DATETIME = parseToDateTimeType("20100223000000");

    @Mock
    private DateTimeMapper dateTimeMapper;

    @InjectMocks
    private SpecimenMapper specimenMapper;

    @Test
    public void testSpecimenIsMapped() {
        when(dateTimeMapper.mapDateTime(any())).thenCallRealMethod();
        RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("specimen_valid.xml");
        List<Specimen> specimenList = specimenMapper.mapSpecimen(
            ehrExtract, generateDiagnosticReportWithSpecimenReference(), PATIENT, PRACTICE_CODE
        );

        assertThat(specimenList).isNotEmpty();

        final Specimen specimen = specimenList.get(0);
        assertThat(specimen.getId()).isEqualTo(TEST_SPECIMEN_ID);
        assertThat(specimen.getMeta().getProfile().get(0).getValue()).contains(SPECIMEN_META_PROFILE_SUFFIX);
        assertThat(specimen.getIdentifierFirstRep().getSystem()).contains(PRACTICE_CODE);
        assertThat(specimen.getSubject().getResource().getIdElement().getValue()).isEqualTo(PATIENT.getId());
        assertThat(specimen.getNote().size()).isEqualTo(0);
        assertThat(specimen.getAccessionIdentifier().getValue()).isEqualTo(ACCESSION_IDENTIFIER_VALUE);
        assertThat(specimen.getType().getText()).isEqualTo(SPECIMEN_TYPE_TEXT);
        assertThat(specimen.getCollection().getCollected().toString()).isEqualTo(SPECIMEN_COLLECTED_DATETIME.toString());
    }

    @Test
    public void testSpecimenIsMappedWithNoOptionalFields() {
        RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("specimen_no_optional_fields.xml");
        List<Specimen> specimenList = specimenMapper.mapSpecimen(
            ehrExtract, generateDiagnosticReportWithSpecimenReference(), PATIENT, PRACTICE_CODE
        );

        assertThat(specimenList.get(0).getCollection().getCollected()).isNull();
        assertThat(specimenList.get(0).hasAccessionIdentifier()).isFalse();
        assertThat(specimenList.get(0).getType().getText()).isNullOrEmpty();
    }

    @Test
    public void testInvalidSpecimenIsNotMapped() {
        RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("specimen_invalid.xml");
        List<Specimen> specimenList = specimenMapper.mapSpecimen(
            ehrExtract, generateDiagnosticReportWithNoSpecimenReference(), PATIENT, PRACTICE_CODE
        );

        assertThat(specimenList).isEmpty();
    }

    private List<DiagnosticReport> generateDiagnosticReportWithSpecimenReference() {
        DiagnosticReport diagnosticReport = (DiagnosticReport) new DiagnosticReport().setId("DIAGNOSTIC_REPORT_ID");
        List<Reference> specimen = List.of(new Reference(SPECIMEN_PREFIX + TEST_SPECIMEN_ID));
        diagnosticReport.setSpecimen(specimen);

        return List.of(diagnosticReport);
    }

    private List<DiagnosticReport> generateDiagnosticReportWithNoSpecimenReference() {
        DiagnosticReport diagnosticReport = (DiagnosticReport) new DiagnosticReport().setId("DIAGNOSTIC_REPORT_ID");
        return List.of(diagnosticReport);
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }

}
