package uk.nhs.adaptors.pss.translator.mapper;

import lombok.SneakyThrows;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class EncounterTest {
    private static final String XML_RESOURCES_BASE = "xml/Encounter/";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-ProcedureRequest-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String CODING_DISPLAY = "Ischaemic heart disease";

    @InjectMocks
    private EncounterMapper encounterMapper;

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    private Patient patient;

    @BeforeEach
    public void setup() {
        patient = new Patient();
        patient.setId("0E6F45F0-8D7B-11EC-B1E5-0800200C9A66");
        setUpCodeableConceptMock();
    }

    @Test
    public void mapEncounterWithFullData() {
        var ehrExtract = unmarshallEhrExtractElement("test.xml");

        Map<String, List<Object>> mappedResources = encounterMapper.mapAllEncounters(ehrExtract, patient);

        var encounterList = mappedResources.get("encounters");

        assertThat(encounterList.size()).isEqualTo(1);
    }

    private void setUpCodeableConceptMock() {
        var codeableConcept = new CodeableConcept();
        var coding = new Coding();
        coding.setDisplay(CODING_DISPLAY);
        codeableConcept.addCoding(coding);
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(codeableConcept);
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtractElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}


