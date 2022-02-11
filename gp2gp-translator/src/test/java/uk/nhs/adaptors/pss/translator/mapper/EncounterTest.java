package uk.nhs.adaptors.pss.translator.mapper;

import lombok.SneakyThrows;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
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

    @BeforeEach
    public void setup() {
        setUpCodeableConceptMock();
    }

    @Test
    public void mapEncounterWithFullData() {
        var ehrComposition = unmarshallEhrCompositionElement("test.xml");

        Encounter encounter = encounterMapper.mapToEncounter(ehrComposition, "BA6EA7CB-3E2F-46FA-918C-C0B5178C1D4E");

        assertThat(encounter).isNotNull();
    }

    private void setUpCodeableConceptMock() {
        var codeableConcept = new CodeableConcept();
        var coding = new Coding();
        coding.setDisplay(CODING_DISPLAY);
        codeableConcept.addCoding(coding);
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(codeableConcept);
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrComposition unmarshallEhrCompositionElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrComposition.class);
    }
}


