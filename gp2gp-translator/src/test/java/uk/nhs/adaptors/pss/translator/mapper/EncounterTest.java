package uk.nhs.adaptors.pss.translator.mapper;

import lombok.SneakyThrows;

import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

@ExtendWith(MockitoExtension.class)
public class EncounterTest {
    private static final String XML_RESOURCES_BASE = "xml/Encounter/";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-ProcedureRequest-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";

    @InjectMocks
    private EncounterMapper encounterMapper;

    @Test
    public void mapEncounter() {
        var ehrComposition = unmarshallCodeElement("test.xml");

        Encounter encounter = encounterMapper.mapToEncounter(ehrComposition);

        assertThat(encounter).isNotNull();
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrComposition unmarshallCodeElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrComposition.class);
    }
}


