package uk.nhs.adaptors.pss.translator.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.HumanName.NameUse;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CV;
import org.hl7.v3.II;
import org.hl7.v3.PN;
import org.hl7.v3.RCCTMT120101UK01Agent;
import org.hl7.v3.RCCTMT120101UK01Organization;
import org.hl7.v3.RCCTMT120101UK01Person;
import org.hl7.v3.RCMRMT030101UK04AgentDirectory;
import org.hl7.v3.RCMRMT030101UK04Part;
import org.springframework.util.CollectionUtils;

import io.micrometer.core.instrument.util.StringUtils;

public class AgentMapper {
    private static final String PRACTITIONER_META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Practitioner-1";
    private static final String ORGANIZATION_META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Organization-1";
    private static final String UNKNOWN_FAMILY = "Unknown";
    private static final String ORG_ID_PREFIX = "-ORG";

    public List mapAgentDirectory(RCMRMT030101UK04AgentDirectory agentDirectory) {
        var partList = agentDirectory.getPart();

        if (!CollectionUtils.isEmpty(partList)) {
            return partList.stream()
                .map(RCMRMT030101UK04Part::getAgent)
                .map(this::generateAppropriateResources)
                .collect(Collectors.toList());
        }

        return null;
    }

    private List generateAppropriateResources(RCCTMT120101UK01Agent agent) {
        var agentResourceList = new ArrayList<>();
        var agentPerson = agent.getAgentPerson();
        var agentOrganization = agent.getAgentOrganization();
        var representedOrganization = agent.getRepresentedOrganization();
        var resourceId = agent.getId().get(0).getRoot();

        if (agentPerson != null && representedOrganization != null) {
            agentResourceList.add(createPractitioner(agentPerson, resourceId));
            agentResourceList.add(createRepresentedOrganization(representedOrganization, resourceId));
            agentResourceList.add(createPractitionerRole());
        } else if (agentPerson != null && agentOrganization == null) {
            agentResourceList.add(createPractitioner(agentPerson, resourceId));
        } else if (agentPerson == null && agentOrganization != null) {
            agentResourceList.add(createAgentOrganization(agentOrganization, resourceId, agent));
        }

        return agentResourceList;
    }

    private Practitioner createPractitioner(RCCTMT120101UK01Person agentPerson, String id) {
        var practitioner = new Practitioner();

        practitioner.setId(id);
        practitioner.getMeta().getProfile().add(new UriType(PRACTITIONER_META_PROFILE));
        practitioner.setName(getPractitionerName(agentPerson.getName()));

        return practitioner;
    }

    private List<HumanName> getPractitionerName(PN name) {
        var nameList = new ArrayList();
        var humanName = new HumanName();

        humanName
            .setUse(NameUse.OFFICIAL)
            .setFamily(getPractitionerFamily(name.getFamily()));
        humanName.getGiven().add(getPractitionerGiven(name.getGiven()));
        humanName.getPrefix().add(getPractitionerPrefix(name.getPrefix()));
        nameList.add(humanName);

        return nameList;
    }

    private String getPractitionerFamily(String family) {
        return StringUtils.isNotEmpty(family) ? family : UNKNOWN_FAMILY;
    }

    private StringType getPractitionerGiven(String given) {
        return StringUtils.isNotEmpty(given) ? new StringType(given) : null;
    }

    private StringType getPractitionerPrefix(String prefix) {
        return StringUtils.isNotEmpty(prefix) ? new StringType(prefix) : null;
    }

    private Organization createRepresentedOrganization(RCCTMT120101UK01Organization representedOrg, String id) {
        var organization = new Organization();

        organization.setId(id + ORG_ID_PREFIX);
        organization.getMeta().getProfile().add(new UriType(ORGANIZATION_META_PROFILE));
        organization.getIdentifier().add(getOrganizationIdentifier(representedOrg.getId())); // fix
        // name
        // telecom
        // address

        return organization;
    }

    private CodeableConcept getText(CV code) {
        var codeableConcept = new CodeableConcept();
        if (code != null) {
            if (StringUtils.isNotEmpty(code.getOriginalText())) {
                return codeableConcept.setText(code.getOriginalText());
            }
            else if (StringUtils.isNotEmpty(code.getDisplayName())) {
                return codeableConcept.setText(code.getDisplayName());
            }
        }

        return null;
    }

    private Organization createAgentOrganization(RCCTMT120101UK01Organization agentOrg, String id,
        RCCTMT120101UK01Agent agent) {
        var organization = new Organization();

        organization.setId(id);
        organization.getMeta().getProfile().add(new UriType(ORGANIZATION_META_PROFILE));
        organization.getType().add(getText(agent.getCode()));
        // identifier?

        return organization;
    }

    private Identifier getOrganizationIdentifier(II id) {
        var identifier = new Identifier();

        return identifier;
    }

    private PractitionerRole createPractitionerRole() {
        var practitionerRole = new PractitionerRole();

        // id
        // meta
        // code.text
        // practitioner
        // organization

        return practitionerRole;
    }
}
