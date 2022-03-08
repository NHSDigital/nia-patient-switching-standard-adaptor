package uk.nhs.adaptors.pss.translator.mapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
public class TemplateMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/Template/";
    private static final String PRACTISE_CODE = "TESTPRACTISECODE";
    private static final String PATIENT_ID = "9A5D5A78-1F63-434C-9637-1D7E7843341B";
    private static final String CODING_DISPLAY_MOCK = "Test Display";

    private static final CodeableConcept CODEABLE_CONCEPT = new CodeableConcept()
        .addCoding(new Coding().setDisplay(CODING_DISPLAY_MOCK));

    private static final List<Encounter> ENCOUNTER_LIST = List.of(
        (Encounter) new Encounter().setId("TEST_ID_MATCHING_ENCOUNTER")
    );

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @InjectMocks
    private TemplateMapper templateMapper;

    @Test
    public void mapObservationWithValidData() {
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(CODEABLE_CONCEPT);

        var ehrExtract = unmarshallEhrExtractElement("full_valid_template.xml");
        var template = templateMapper.mapTemplates(ehrExtract, getPatient(), ENCOUNTER_LIST, PRACTISE_CODE);

        var x = 1;
    }

    private Patient getPatient() {
        var patient = new Patient();
        patient.setId(PATIENT_ID);
        return patient;
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtractElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}
