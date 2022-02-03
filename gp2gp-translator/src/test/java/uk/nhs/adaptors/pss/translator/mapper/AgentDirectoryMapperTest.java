package uk.nhs.adaptors.pss.translator.mapper;

import lombok.SneakyThrows;

import org.hl7.v3.RCMRMT030101UK04AgentDirectory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

public class AgentDirectoryMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/Agent/";

    private final AgentDirectoryMapper agentDirectoryMapper = new AgentDirectoryMapper();

    @Test
    public void mapAgentDirectory() {
        var agentDirectory = unmarshallCodeElement("test.xml");

        List mappedAgents = agentDirectoryMapper.mapAgentDirectory(agentDirectory);

        assertThat(mappedAgents).isNotNull();
    }

    @Test
    public void mapAgentDirectoryWithAgentPersonAndRepresentedOrganization() {
        var agentDirectory = unmarshallCodeElement("test.xml");

        List mappedAgents = agentDirectoryMapper.mapAgentDirectory(agentDirectory);

        assertThat(mappedAgents).isNotNull();
    }

    @Test
    public void mapAgentDirectoryOnlyAgentPerson() {
        var agentDirectory = unmarshallCodeElement("test.xml");

        List mappedAgents = agentDirectoryMapper.mapAgentDirectory(agentDirectory);

        assertThat(mappedAgents).isNotNull();
    }

    @Test
    public void mapAgentDirectoryOnlyAgentOrganization() {
        var agentDirectory = unmarshallCodeElement("test.xml");

        List mappedAgents = agentDirectoryMapper.mapAgentDirectory(agentDirectory);

        assertThat(mappedAgents).isNotNull();
    }

    @SneakyThrows
    private RCMRMT030101UK04AgentDirectory unmarshallCodeElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04AgentDirectory.class);
    }
}


