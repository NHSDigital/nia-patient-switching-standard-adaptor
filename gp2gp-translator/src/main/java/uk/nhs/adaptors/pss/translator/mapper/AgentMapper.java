package uk.nhs.adaptors.pss.translator.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.v3.RCCTMT120101UK01Agent;
import org.hl7.v3.RCMRMT030101UK04AgentDirectory;
import org.hl7.v3.RCMRMT030101UK04Part;
import org.springframework.util.CollectionUtils;

public class AgentMapper {

    public List mapAgentDirectory(RCMRMT030101UK04AgentDirectory agentDirectory) {
        var agentList = new ArrayList<>();
        var partList = agentDirectory.getPart();

        if (!CollectionUtils.isEmpty(partList)) {
            partList.stream()
                .map(RCMRMT030101UK04Part::getAgent)
                .map(this::generateAppropriateResources)
                .collect(Collectors.toList());
        }

        return agentList;
    }

    private List generateAppropriateResources(RCCTMT120101UK01Agent agent) {
        var agentResourceList = new ArrayList<>();
        var agentPerson = agent.getAgentPerson();
        var agentOrganization = agent.getRepresentedOrganization();

        if (agentPerson != null && agentOrganization != null) {
            agentResourceList.add(createPractitioner());
            agentResourceList.add(createOrganization());
            agentResourceList.add(createPractitionerRole());
        } else if (agentPerson != null && agentOrganization == null) {
            agentResourceList.add(createPractitioner());
        } else if (agentPerson == null && agentOrganization != null) {
            agentResourceList.add(createOrganization());
        }

        return agentResourceList;
    }

    private Practitioner createPractitioner() {
        var practitioner = new Practitioner();

        return practitioner;
    }

    private Organization createOrganization() {
        var organization = new Organization();

        return organization;
    }

    private PractitionerRole createPractitionerRole() {
        var practitionerRole = new PractitionerRole();

        return practitionerRole;
    }
}


