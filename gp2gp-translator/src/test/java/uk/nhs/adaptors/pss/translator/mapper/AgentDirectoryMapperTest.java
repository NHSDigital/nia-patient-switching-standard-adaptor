package uk.nhs.adaptors.pss.translator.mapper;

import lombok.SneakyThrows;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.Address.AddressUse;
import org.hl7.fhir.dstu3.model.Address.AddressType;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.dstu3.model.HumanName.NameUse;
import org.hl7.v3.RCMRMT030101UK04AgentDirectory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

public class AgentDirectoryMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/AgentDirectory/";
    private static final String PRACT_META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Practitioner-1";
    private static final String ORG_META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Organization-1";
    private static final String PRACT_ROLE_META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC"
        + "-PractitionerRole-1";
    private final AgentDirectoryMapper agentDirectoryMapper = new AgentDirectoryMapper();
    private static final String ORG_IDENTIFIER_SYSTEM = "https://fhir.nhs.uk/Id/ods-organization-code";
    private static final int TELECOM_RANK = 1;
    private static final int THREE_RESOURCES_MAPPED = 3;
    private static final int FIVE_RESOURCES_MAPPED = 5;

    @Test
    public void mapAgentDirectoryWithMultipleAgents() {
        var agentDirectory = unmarshallAgentDirectoryElement("multiple_agents_example.xml");

        List mappedAgents = agentDirectoryMapper.mapAgentDirectory(agentDirectory);

        // asserts that for the 3 varying Agents in the input, that there are correctly 5 mapped resources output
        assertThat(mappedAgents.size()).isEqualTo(FIVE_RESOURCES_MAPPED);
    }

    @Test
    public void mapAgentDirectoryWithAgentPersonAndRepresentedOrganizationNoCode() {
        var agentDirectory = unmarshallAgentDirectoryElement("agent_person_and_represented_org_example.xml");

        List mappedAgents = agentDirectoryMapper.mapAgentDirectory(agentDirectory);

        assertThat(mappedAgents.size()).isEqualTo(THREE_RESOURCES_MAPPED);

        var practitioner = (Practitioner) mappedAgents.get(0);
        assertThat(practitioner.getId()).isEqualTo("94F00D99-0601-4A8E-AD1D-1B564307B0A6");
        assertThat(practitioner.getMeta().getProfile().get(0).getValue()).isEqualTo(PRACT_META_PROFILE);
        assertThat(practitioner.getNameFirstRep().getUse()).isEqualTo(NameUse.OFFICIAL);
        assertThat(practitioner.getNameFirstRep().getFamily()).isEqualTo("Test");

        var organization = (Organization) mappedAgents.get(1);
        assertThat(organization.getId()).isEqualTo("94F00D99-0601-4A8E-AD1D-1B564307B0A6-ORG");
        assertThat(organization.getMeta().getProfile().get(0).getValue()).isEqualTo(ORG_META_PROFILE);
        assertThat(organization.getName()).isEqualTo("TEMPLE SOWERBY MEDICAL PRACTICE");
        assertThat(organization.getTypeFirstRep().getText()).isNull();

        var practitionerRole = (PractitionerRole) mappedAgents.get(2);
        assertThat(practitionerRole.getId()).isEqualTo("94F00D99-0601-4A8E-AD1D-1B564307B0A6-PR");
        assertThat(practitionerRole.getMeta().getProfile().get(0).getValue()).isEqualTo(PRACT_ROLE_META_PROFILE);
        assertThat(practitionerRole.getPractitioner().getReference()).isEqualTo("Practitioner/94F00D99-0601-4A8E-AD1D-1B564307B0A6");
        assertThat(practitionerRole.getOrganization().getReference()).isEqualTo("Organization/94F00D99-0601-4A8E-AD1D-1B564307B0A6-ORG");
        assertThat(practitionerRole.getCodeFirstRep()).isNull();
    }

    @Test
    public void mapAgentDirectoryWithAgentPersonAndRepresentedOrganizationWithOriginalText() {
        var agentDirectory = unmarshallAgentDirectoryElement("agent_person_and_represented_org_with_original_text_example.xml");

        List mappedAgents = agentDirectoryMapper.mapAgentDirectory(agentDirectory);

        assertThat(mappedAgents.size()).isEqualTo(THREE_RESOURCES_MAPPED);

        var practitioner = (Practitioner) mappedAgents.get(0);
        assertThat(practitioner.getId()).isEqualTo("94F00D99-0601-4A8E-AD1D-1B564307B0A6");
        assertThat(practitioner.getMeta().getProfile().get(0).getValue()).isEqualTo(PRACT_META_PROFILE);
        assertThat(practitioner.getNameFirstRep().getUse()).isEqualTo(NameUse.OFFICIAL);
        assertThat(practitioner.getNameFirstRep().getFamily()).isEqualTo("Test");

        var organization = (Organization) mappedAgents.get(1);
        assertThat(organization.getId()).isEqualTo("94F00D99-0601-4A8E-AD1D-1B564307B0A6-ORG");
        assertThat(organization.getMeta().getProfile().get(0).getValue()).isEqualTo(ORG_META_PROFILE);
        assertThat(organization.getName()).isEqualTo("TEMPLE SOWERBY MEDICAL PRACTICE");
        assertThat(organization.getTypeFirstRep().getText()).isNull();

        var practitionerRole = (PractitionerRole) mappedAgents.get(2);
        assertThat(practitionerRole.getId()).isEqualTo("94F00D99-0601-4A8E-AD1D-1B564307B0A6-PR");
        assertThat(practitionerRole.getMeta().getProfile().get(0).getValue()).isEqualTo(PRACT_ROLE_META_PROFILE);
        assertThat(practitionerRole.getPractitioner().getReference()).isEqualTo("Practitioner/94F00D99-0601-4A8E-AD1D-1B564307B0A6");
        assertThat(practitionerRole.getOrganization().getReference()).isEqualTo("Organization/94F00D99-0601-4A8E-AD1D-1B564307B0A6-ORG");
        assertThat(practitionerRole.getCodeFirstRep().getText()).isEqualTo("Clerical Worker");
    }

    @Test
    public void mapAgentDirectoryWithAgentPersonAndRepresentedOrganizationWithDisplayName() {
        var agentDirectory = unmarshallAgentDirectoryElement("agent_person_and_represented_org_with_display_name_example.xml");

        List mappedAgents = agentDirectoryMapper.mapAgentDirectory(agentDirectory);

        assertThat(mappedAgents.size()).isEqualTo(THREE_RESOURCES_MAPPED);

        var practitioner = (Practitioner) mappedAgents.get(0);
        assertThat(practitioner.getId()).isEqualTo("94F00D99-0601-4A8E-AD1D-1B564307B0A6");
        assertThat(practitioner.getMeta().getProfile().get(0).getValue()).isEqualTo(PRACT_META_PROFILE);
        assertThat(practitioner.getNameFirstRep().getUse()).isEqualTo(NameUse.OFFICIAL);
        assertThat(practitioner.getNameFirstRep().getFamily()).isEqualTo("Test");

        var organization = (Organization) mappedAgents.get(1);
        assertThat(organization.getId()).isEqualTo("94F00D99-0601-4A8E-AD1D-1B564307B0A6-ORG");
        assertThat(organization.getMeta().getProfile().get(0).getValue()).isEqualTo(ORG_META_PROFILE);
        assertThat(organization.getName()).isEqualTo("TEMPLE SOWERBY MEDICAL PRACTICE");
        assertThat(organization.getTypeFirstRep().getText()).isNull();

        var practitionerRole = (PractitionerRole) mappedAgents.get(2);
        assertThat(practitionerRole.getId()).isEqualTo("94F00D99-0601-4A8E-AD1D-1B564307B0A6-PR");
        assertThat(practitionerRole.getMeta().getProfile().get(0).getValue()).isEqualTo(PRACT_ROLE_META_PROFILE);
        assertThat(practitionerRole.getPractitioner().getReference()).isEqualTo("Practitioner/94F00D99-0601-4A8E-AD1D-1B564307B0A6");
        assertThat(practitionerRole.getOrganization().getReference()).isEqualTo("Organization/94F00D99-0601-4A8E-AD1D-1B564307B0A6-ORG");
        assertThat(practitionerRole.getCodeFirstRep().getText()).isEqualTo("General practice");
    }

    @Test
    public void mapAgentDirectoryOnlyAgentPersonNoOptionalFields() {
        var agentDirectory = unmarshallAgentDirectoryElement("agent_person_only_example.xml");

        List mappedAgents = agentDirectoryMapper.mapAgentDirectory(agentDirectory);

        assertThat(mappedAgents.size()).isEqualTo(1);

        var practitioner = (Practitioner) mappedAgents.get(0);
        assertThat(practitioner.getId()).isEqualTo("95D00D99-0601-4A8E-AD1D-1B564307B0A6");
        assertThat(practitioner.getMeta().getProfile().get(0).getValue()).isEqualTo(PRACT_META_PROFILE);
        assertThat(practitioner.getNameFirstRep().getUse()).isEqualTo(NameUse.OFFICIAL);
        assertThat(practitioner.getNameFirstRep().getFamily()).isEqualTo("Test");
        assertThat(practitioner.getNameFirstRep().getGiven().get(0)).isNull();
        assertThat(practitioner.getNameFirstRep().getPrefix().get(0)).isNull();
    }

    @Test
    public void mapAgentDirectoryOnlyAgentPersonUnknownName() {
        var agentDirectory = unmarshallAgentDirectoryElement("agent_person_only_no_name_example.xml");

        List mappedAgents = agentDirectoryMapper.mapAgentDirectory(agentDirectory);

        assertThat(mappedAgents.size()).isEqualTo(1);

        var practitioner = (Practitioner) mappedAgents.get(0);
        assertThat(practitioner.getId()).isEqualTo("95D00D99-0601-4A8E-AD1D-1B564307B0A6");
        assertThat(practitioner.getMeta().getProfile().get(0).getValue()).isEqualTo(PRACT_META_PROFILE);
        assertThat(practitioner.getNameFirstRep().getUse()).isEqualTo(NameUse.OFFICIAL);
        assertThat(practitioner.getNameFirstRep().getFamily()).isEqualTo("Unknown");
        assertThat(practitioner.getNameFirstRep().getGiven().get(0)).isNull();
        assertThat(practitioner.getNameFirstRep().getPrefix().get(0)).isNull();
    }

    @Test
    public void mapAgentDirectoryOnlyAgentPersonFullName() {
        var agentDirectory = unmarshallAgentDirectoryElement("agent_person_only_full_name_example.xml");

        List mappedAgents = agentDirectoryMapper.mapAgentDirectory(agentDirectory);

        assertThat(mappedAgents.size()).isEqualTo(1);

        var practitioner = (Practitioner) mappedAgents.get(0);
        assertThat(practitioner.getId()).isEqualTo("95D00D99-0601-4A8E-AD1D-1B564307B0A6");
        assertThat(practitioner.getMeta().getProfile().get(0).getValue()).isEqualTo(PRACT_META_PROFILE);
        assertThat(practitioner.getNameFirstRep().getUse()).isEqualTo(NameUse.OFFICIAL);
        assertThat(practitioner.getNameFirstRep().getFamily()).isEqualTo("Test");
        assertThat(practitioner.getNameFirstRep().getGiven().get(0).getValue()).isEqualTo("NHS");
        assertThat(practitioner.getNameFirstRep().getPrefix().get(0).getValue()).isEqualTo("Mr");
    }

    @Test
    public void mapAgentDirectoryOnlyAgentOrganizationNoOptionalFields() {
        var agentDirectory = unmarshallAgentDirectoryElement("agent_org_only_example.xml");

        List mappedAgents = agentDirectoryMapper.mapAgentDirectory(agentDirectory);

        assertThat(mappedAgents.size()).isEqualTo(1);

        var organization = (Organization) mappedAgents.get(0);
        assertThat(organization.getId()).isEqualTo("1D9BDC28-50AB-440D-B421-0E5E049526FA");
        assertThat(organization.getMeta().getProfile().get(0).getValue()).isEqualTo(ORG_META_PROFILE);
        assertThat(organization.getName()).isEqualTo("The Health Centre");
        assertThat(organization.getIdentifierFirstRep()).isNull();
        assertThat(organization.getTelecomFirstRep()).isNull();
        assertThat(organization.getAddressFirstRep()).isNull();
    }

    @Test
    public void mapAgentDirectoryOnlyAgentOrganizationUnknownName() {
        var agentDirectory = unmarshallAgentDirectoryElement("agent_org_no_name_example.xml");

        List mappedAgents = agentDirectoryMapper.mapAgentDirectory(agentDirectory);

        assertThat(mappedAgents.size()).isEqualTo(1);

        var organization = (Organization) mappedAgents.get(0);
        assertThat(organization.getId()).isEqualTo("1D9BDC28-50AB-440D-B421-0E5E049526FA");
        assertThat(organization.getMeta().getProfile().get(0).getValue()).isEqualTo(ORG_META_PROFILE);
        assertThat(organization.getName()).isEqualTo("Unknown");
        assertThat(organization.getIdentifierFirstRep()).isNull();
        assertThat(organization.getTelecomFirstRep()).isNull();
        assertThat(organization.getAddressFirstRep()).isNull();
    }

    @Test
    public void mapAgentDirectoryOnlyAgentOrganizationWithValidIdentifier() {
        var agentDirectory = unmarshallAgentDirectoryElement("agent_org_valid_identifier_example.xml");

        List mappedAgents = agentDirectoryMapper.mapAgentDirectory(agentDirectory);

        assertThat(mappedAgents.size()).isEqualTo(1);

        var organization = (Organization) mappedAgents.get(0);
        assertThat(organization.getId()).isEqualTo("1D9BDC28-50AB-440D-B421-0E5E049526FA");
        assertThat(organization.getMeta().getProfile().get(0).getValue()).isEqualTo(ORG_META_PROFILE);
        assertThat(organization.getName()).isEqualTo("The Health Centre");
        assertThat(organization.getIdentifierFirstRep().getSystem()).isEqualTo(ORG_IDENTIFIER_SYSTEM);
        assertThat(organization.getIdentifierFirstRep().getValue()).isEqualTo("A81001");
        assertThat(organization.getTelecomFirstRep()).isNull();
        assertThat(organization.getAddressFirstRep()).isNull();
    }

    @Test
    public void mapAgentDirectoryOnlyAgentOrganizationWithInvalidIdentifier() {
        var agentDirectory = unmarshallAgentDirectoryElement("agent_org_invalid_identifier_example.xml");

        List mappedAgents = agentDirectoryMapper.mapAgentDirectory(agentDirectory);

        assertThat(mappedAgents.size()).isEqualTo(1);

        var organization = (Organization) mappedAgents.get(0);
        assertThat(organization.getId()).isEqualTo("1D9BDC28-50AB-440D-B421-0E5E049526FA");
        assertThat(organization.getMeta().getProfile().get(0).getValue()).isEqualTo(ORG_META_PROFILE);
        assertThat(organization.getName()).isEqualTo("The Health Centre");
        assertThat(organization.getIdentifierFirstRep()).isNull();
        assertThat(organization.getTelecomFirstRep()).isNull();
        assertThat(organization.getAddressFirstRep()).isNull();
    }

    @Test
    public void mapAgentDirectoryOnlyAgentOrganizationWithNonWPAddressAndTelecom() {
        var agentDirectory = unmarshallAgentDirectoryElement("agent_org_non_wp_address_and_telecom_example.xml");

        List mappedAgents = agentDirectoryMapper.mapAgentDirectory(agentDirectory);

        assertThat(mappedAgents.size()).isEqualTo(1);

        var organization = (Organization) mappedAgents.get(0);
        assertThat(organization.getId()).isEqualTo("1D9BDC28-50AB-440D-B421-0E5E049526FA");
        assertThat(organization.getMeta().getProfile().get(0).getValue()).isEqualTo(ORG_META_PROFILE);
        assertThat(organization.getName()).isEqualTo("The Health Centre");
        assertThat(organization.getIdentifierFirstRep()).isNull();
        assertTelecom(organization.getTelecomFirstRep(), "01234567890");
        assertThat(organization.getAddressFirstRep()).isNull();
    }

    @Test
    public void mapAgentDirectoryOnlyAgentOrganizationWithWPTelecom() {
        var agentDirectory = unmarshallAgentDirectoryElement("agent_org_wp_telecom_example.xml");

        List mappedAgents = agentDirectoryMapper.mapAgentDirectory(agentDirectory);

        assertThat(mappedAgents.size()).isEqualTo(1);

        var organization = (Organization) mappedAgents.get(0);
        assertThat(organization.getId()).isEqualTo("1D9BDC28-50AB-440D-B421-0E5E049526FA");
        assertThat(organization.getMeta().getProfile().get(0).getValue()).isEqualTo(ORG_META_PROFILE);
        assertThat(organization.getName()).isEqualTo("The Health Centre");
        assertThat(organization.getIdentifierFirstRep()).isNull();
        assertTelecom(organization.getTelecomFirstRep(), "01234567890");
        assertThat(organization.getAddressFirstRep()).isNull();
    }

    @Test
    public void mapAgentDirectoryOnlyAgentOrganizationWithWPAddress() {
        var agentDirectory = unmarshallAgentDirectoryElement("agent_org_wp_address_example.xml");

        List mappedAgents = agentDirectoryMapper.mapAgentDirectory(agentDirectory);

        assertThat(mappedAgents.size()).isEqualTo(1);

        var organization = (Organization) mappedAgents.get(0);
        assertThat(organization.getId()).isEqualTo("1D9BDC28-50AB-440D-B421-0E5E049526FA");
        assertThat(organization.getMeta().getProfile().get(0).getValue()).isEqualTo(ORG_META_PROFILE);
        assertThat(organization.getName()).isEqualTo("The Health Centre");
        assertThat(organization.getIdentifierFirstRep()).isNull();
        assertThat(organization.getTelecomFirstRep()).isNull();
        assertAddress(organization.getAddressFirstRep());
    }

    private void assertAddress(Address address) {
        assertThat(address.getUse()).isEqualTo(AddressUse.WORK);
        assertThat(address.getType()).isEqualTo(AddressType.PHYSICAL);
        assertThat(address.getLine().get(0).getValue()).isEqualTo("234 ASHTREE ROAD");
        assertThat(address.getLine().get(1).getValue()).isEqualTo("LEEDS");
        assertThat(address.getLine().get(2).getValue()).isEqualTo("YORKSHIRE");
        assertThat(address.getPostalCode()).isEqualTo("LS12 3RT");
    }
    private void assertTelecom(ContactPoint telecom, String value) {
        assertThat(telecom.getSystem()).isEqualTo(ContactPointSystem.PHONE);
        assertThat(telecom.getUse()).isEqualTo(ContactPointUse.WORK);
        assertThat(telecom.getRank()).isEqualTo(TELECOM_RANK);
        assertThat(telecom.getValue()).isEqualTo(value);
    }

    @SneakyThrows
    private RCMRMT030101UK04AgentDirectory unmarshallAgentDirectoryElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04AgentDirectory.class);
    }
}
