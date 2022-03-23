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
    private static final String SPECIMEN_META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Specimen-1";
    private static final String SPECIMEN_PREFIX = "Specimen/";
    private static final String TEST_SPECIMEN_ID = "COMPOUND_STATEMENT_CHILD_ID_1";
    private static final String PRACTICE_CODE = "TEST_PRACTICE_CODE";
    private static final String ACCESSION_IDENTIFIER_VALUE = "SPECIMEN_ROLE_ID_EXTENSION";
    private static final String SPECIMEN_TYPE_TEXT = "EINE KLEINE";
    private static final Patient PATIENT = (Patient) new Patient().setId("PATIENT_TEST_ID");
    private static final DateTimeType SPECIMEN_COLLECTED_DATETIME = parseToDateTimeType("20100223000000");
    private static final String NOTE_TEXT = "Received Date: 2002-03-30 09:21";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/TEST_PRACTICE_CODE";
    private static final String DR_ID = "DIAGNOSTIC_REPORT_ID";
    private static final DiagnosticReport DIAGNOSTIC_REPORT_WITH_SPECIMEN = generateDiagnosticReportWithSpecimenReference();
    private static final DiagnosticReport DIAGNOSTIC_REPORT_WITHOUT_SPECIMEN = generateDiagnosticReportWithNoSpecimenReference();

    @Mock
    private DateTimeMapper dateTimeMapper;

    @InjectMocks
    private SpecimenMapper specimenMapper;

    @Test
    public void testSpecimenIsMapped() {
        when(dateTimeMapper.mapDateTime(any())).thenCallRealMethod();
        RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("specimen_valid.xml");
        List<Specimen> specimenList = specimenMapper.mapSpecimen(
            ehrExtract, List.of(DIAGNOSTIC_REPORT_WITH_SPECIMEN), PATIENT, PRACTICE_CODE
        );

        assertThat(specimenList).isNotEmpty();

        final Specimen specimen = specimenList.get(0);
        checkFixedValues(specimen);
        assertThat(specimen.getNote().get(0).getText()).isEqualTo(NOTE_TEXT);
        assertThat(specimen.getAccessionIdentifier().getValue()).isEqualTo(ACCESSION_IDENTIFIER_VALUE);
        assertThat(specimen.getType().getText()).isEqualTo(SPECIMEN_TYPE_TEXT);
        assertThat(specimen.getCollection().getCollected().toString()).isEqualTo(SPECIMEN_COLLECTED_DATETIME.toString());
    }

    @Test
    public void testSpecimenIsMappedWithNoOptionalFields() {
        RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("specimen_no_optional_fields.xml");
        List<Specimen> specimenList = specimenMapper.mapSpecimen(
            ehrExtract, List.of(DIAGNOSTIC_REPORT_WITH_SPECIMEN), PATIENT, PRACTICE_CODE
        );

        Specimen specimen = specimenList.get(0);
        assertThat(specimen.getCollection().getCollected()).isNull();
        assertThat(specimen.hasAccessionIdentifier()).isFalse();
        assertThat(specimen.getType().getText()).isNullOrEmpty();
        checkFixedValues(specimen);
    }

    private void checkFixedValues(Specimen specimen) {
        assertThat(specimen.getId()).isEqualTo(TEST_SPECIMEN_ID);
        assertThat(specimen.getMeta().getProfile().get(0).getValue()).isEqualTo(SPECIMEN_META_PROFILE);
        assertThat(specimen.getIdentifierFirstRep().getSystem()).isEqualTo(IDENTIFIER_SYSTEM);
        assertThat(specimen.getIdentifierFirstRep().getValue()).isEqualTo(TEST_SPECIMEN_ID);
        assertThat(specimen.getSubject().getResource().getIdElement().getValue()).isEqualTo(PATIENT.getId());
    }

    @Test
    public void testInvalidSpecimenIsNotMapped() {
        RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("specimen_invalid.xml");
        List<Specimen> specimenList = specimenMapper.mapSpecimen(
            ehrExtract, List.of(DIAGNOSTIC_REPORT_WITHOUT_SPECIMEN), PATIENT, PRACTICE_CODE
        );

        assertThat(specimenList).isEmpty();
    }

    private static DiagnosticReport generateDiagnosticReportWithSpecimenReference() {
        DiagnosticReport diagnosticReport = (DiagnosticReport) new DiagnosticReport().setId(DR_ID);
        List<Reference> specimen = List.of(new Reference(SPECIMEN_PREFIX + TEST_SPECIMEN_ID));
        diagnosticReport.setSpecimen(specimen);

        return diagnosticReport;
    }

    private static DiagnosticReport generateDiagnosticReportWithNoSpecimenReference() {
        return (DiagnosticReport) new DiagnosticReport().setId(DR_ID);

    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}
