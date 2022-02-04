package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.testutil.XmlUnmarshallUtil.unmarshallFile;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04Patient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
public class PatientMapperTest {

    private static final String SLASH = "/";

    private static final String XML_RESOURCES_BASE = "xml/Patient/";
    private static final String PATIENT_EXAMPLE_XML = "patient_example.xml";

    private static final String TEST_PATIENT_ID = "TEST_PATIENT_ID";
    private static final String ORGANIZATION_CLASS_NAME = "Organization";
    private static final String TEST_ORGANIZATION_ID = "TEST_ORGANIZATION_ID";

    private static final String EXPECTED_NHS_NUMBER_SYSTEM_URL = "https://fhir.nhs.uk/Id/nhs-number";
    private static final String EXPECTED_META_PROFILE_URL = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Patient-1";
    private static final String EXPECTED_NHS_NUMBER = "1234567890";
    private static final String EXPECTED_ORGANIZATION_REFERENCE = ORGANIZATION_CLASS_NAME + SLASH + TEST_ORGANIZATION_ID;
    private static final String EXPECTED_META_VERSION_ID = "1521806400000";


    @Mock
    private Organization organization;

    @InjectMocks
    private PatientMapper patientMapper;

    @Test
    public void testNhsNumberIsAddedToPatient() {
        RCMRMT030101UK04Patient patientXml = unmarshallCodeElement(PATIENT_EXAMPLE_XML).getRecordTarget().getPatient();

        Patient patient = patientMapper.mapToPatient(patientXml);

//        assertThat(patient.getId()).isEqualTo(TEST_PATIENT_ID);
        assertThat(patient.hasIdentifier()).isTrue();
        assertThat(patient.getIdentifier().stream().anyMatch(identifier -> EXPECTED_NHS_NUMBER_SYSTEM_URL.equals(identifier.getSystem()))).isTrue();

        assertThat(patient.getIdentifier().stream()
            .filter(identifier -> identifier.getSystem().equals(EXPECTED_NHS_NUMBER_SYSTEM_URL)).findFirst().get().getValue()
        ).isEqualTo(EXPECTED_NHS_NUMBER);
    }

    @Test
    public void testOrganizationReferenceIsAddedToPatient() {
        when(organization.getIdElement()).thenReturn(new IdType(ORGANIZATION_CLASS_NAME, TEST_ORGANIZATION_ID));
        RCMRMT030101UK04Patient patientXml = unmarshallCodeElement(PATIENT_EXAMPLE_XML).getRecordTarget().getPatient();

        Patient patient = patientMapper.mapToPatient(patientXml, organization);

        assertThat(patient.hasIdentifier()).isTrue();
        assertThat(patient.hasManagingOrganization()).isTrue();
        assertThat(patient.getManagingOrganization().getReference()).isEqualTo(EXPECTED_ORGANIZATION_REFERENCE);
    }

    @Test
    public void testMetaIsAddedToPatient() {
        RCMRMT030101UK04Patient patientXml = unmarshallCodeElement(PATIENT_EXAMPLE_XML).getRecordTarget().getPatient();

        Patient patient = patientMapper.mapToPatient(patientXml);

        assertThat(patient.hasMeta()).isTrue();
        assertThat(patient.getMeta().getVersionId()).isEqualTo(EXPECTED_META_VERSION_ID);
        assertThat(patient.getMeta().getProfile().stream().findFirst().get().getValue()).isEqualTo(EXPECTED_META_PROFILE_URL);
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallCodeElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}
