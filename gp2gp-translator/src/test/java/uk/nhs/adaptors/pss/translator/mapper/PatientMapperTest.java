package uk.nhs.adaptors.pss.translator.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.testutil.XmlUnmarshallUtil.unmarshallFile;


import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04Patient;
import org.junit.jupiter.api.Test;

import lombok.SneakyThrows;

public class PatientMapperTest {

    private static final String XML_RESOURCES_BASE = "xml/Patient/";


    private final PatientMapper patientMapper = new PatientMapper();

    @Test
    public void mapPatientWithNhsNumber() {
        RCMRMT030101UK04Patient patientXml = unmarshallCodeElement("patient_example.xml").getRecordTarget().getPatient();

        Patient patient = patientMapper.mapToPatient(patientXml);

        assertTrue(patient.hasIdentifier());
    }


    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallCodeElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}
