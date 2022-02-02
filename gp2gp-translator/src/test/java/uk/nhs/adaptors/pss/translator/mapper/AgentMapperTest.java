package uk.nhs.adaptors.pss.translator.mapper;

import lombok.SneakyThrows;

import org.hl7.v3.RCMRMT030101UK04AgentDirectory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.testutil.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

public class AgentMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/Agent/";

    private final AgentMapper agentMapper = new AgentMapper();

    @Test
    public void mapAgent() {
        var agentDirectory = unmarshallCodeElement("test.xml");

        List agent = agentMapper.mapAgentDirectory(agentDirectory);

        assertThat(agent).isNull();
    }

    @SneakyThrows
    private RCMRMT030101UK04AgentDirectory unmarshallCodeElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04AgentDirectory.class);
    }
}


