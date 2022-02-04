package uk.nhs.adaptors.pss.translator.mapper;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.Address.AddressUse;
import org.hl7.fhir.dstu3.model.Address.AddressType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.HumanName.NameUse;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.AD;
import org.hl7.v3.CV;
import org.hl7.v3.CsTelecommunicationAddressUse;
import org.hl7.v3.II;
import org.hl7.v3.PN;
import org.hl7.v3.RCCTMT120101UK01Agent;
import org.hl7.v3.RCCTMT120101UK01Organization;
import org.hl7.v3.RCCTMT120101UK01Person;
import org.hl7.v3.RCMRMT030101UK04AgentDirectory;
import org.hl7.v3.RCMRMT030101UK04Part;
import org.hl7.v3.TEL;
import org.springframework.util.CollectionUtils;

import io.micrometer.core.instrument.util.StringUtils;

public class AgentDirectoryMapper {
    private static final String PRACT_META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Practitioner-1";
    private static final String ORG_META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Organization-1";
    private static final String PRACT_ROLE_META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC"
        + "-PractitionerRole-1";
    private static final String ORG_IDENTIFIER_SYSTEM = "https://fhir.nhs.uk/Id/ods-organization-code";
    private static final String ORG_PREFIX = "Organization/";
    private static final String PRACT_PREFIX = "Practitioner/";
    private static final String ORG_ID_SUFFIX = "-ORG";
    private static final String PRACT_ROLE_SUFFIX = "-PR";
    private static final String ORG_ROOT = "2.16.840.1.113883.2.1.4.3";
    private static final String UNKNOWN = "Unknown";
    private static final String WORK_PLACE = "WP";
    private static final int TELECOM_RANK = 1;
    private static final int TEL_PREFIX_INT = 4;

    public List mapAgentDirectory(RCMRMT030101UK04AgentDirectory agentDirectory) {
        var partList = agentDirectory.getPart();
        if (!CollectionUtils.isEmpty(partList)) {
            return partList.stream()
                .map(RCMRMT030101UK04Part::getAgent)
                .map(this::mapAgent)
                .flatMap(List::stream)
                .toList();
        }

        return null;
    }

    private List mapAgent(RCCTMT120101UK01Agent agent) {
        var agentResourceList = new ArrayList<>();
        var agentPerson = agent.getAgentPerson();
        var agentOrganization = agent.getAgentOrganization();
        var representedOrganization = agent.getRepresentedOrganization();
        var resourceId = agent.getId().get(0).getRoot();

        if (agentPerson != null && representedOrganization != null) {
            agentResourceList.add(createPractitioner(agentPerson, resourceId));
            agentResourceList.add(createRepresentedOrganization(representedOrganization, resourceId));
            agentResourceList.add(createPractitionerRole(resourceId, agent.getCode()));
        } else if (agentPerson != null && agentOrganization == null) {
            agentResourceList.add(createPractitioner(agentPerson, resourceId));
        } else if (agentPerson == null && agentOrganization != null) {
            agentResourceList.add(createAgentOrganization(agentOrganization, resourceId, agent.getCode()));
        }

        return agentResourceList;
    }

    private Practitioner createPractitioner(RCCTMT120101UK01Person agentPerson, String id) {
        var practitioner = new Practitioner();

        practitioner.setId(id);
        practitioner.getMeta().getProfile().add(new UriType(PRACT_META_PROFILE));
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
        return StringUtils.isNotEmpty(family) ? family : UNKNOWN;
    }

    private StringType getPractitionerGiven(String given) {
        return StringUtils.isNotEmpty(given) ? new StringType(given) : null;
    }

    private StringType getPractitionerPrefix(String prefix) {
        return StringUtils.isNotEmpty(prefix) ? new StringType(prefix) : null;
    }

    private Organization createRepresentedOrganization(RCCTMT120101UK01Organization representedOrg, String id) {
        var organization = new Organization();

        organization
            .setName(getOrganizationName(representedOrg.getName()))
            .setId(id + ORG_ID_SUFFIX);
        organization.getMeta().getProfile().add(new UriType(ORG_META_PROFILE));
        organization.getIdentifier().add(getOrganizationIdentifier(representedOrg.getId()));
        organization.getTelecom().add(getOrganizationTelecom(representedOrg.getTelecom()));
        organization.getAddress().add(getOrganizationAddress(representedOrg.getAddr()));

        return organization;
    }

    private Organization createAgentOrganization(RCCTMT120101UK01Organization agentOrg, String id, CV code) {
        var organization = new Organization();
        organization
            .setName(getOrganizationName(agentOrg.getName()))
            .setId(id);
        organization.getMeta().getProfile().add(new UriType(ORG_META_PROFILE));
        organization.getIdentifier().add(getOrganizationIdentifier(agentOrg.getId()));
        organization.getType().add(getText(code));
        organization.getTelecom().add(getOrganizationTelecom(agentOrg.getTelecom()));
        organization.getAddress().add(getOrganizationAddress(agentOrg.getAddr()));

        return organization;
    }

    private String getOrganizationName(String name) {
        return name != null ? name : UNKNOWN;
    }

    private ContactPoint getOrganizationTelecom(List<TEL> telecomList) {
        var telecom = telecomList.stream().findFirst();
        if (telecom.isPresent()) {
            var contactPoint = new ContactPoint();
            return contactPoint
                .setValue(getTelecomValue(telecom.get()))
                .setSystem(ContactPoint.ContactPointSystem.PHONE)
                .setUse(ContactPoint.ContactPointUse.WORK)
                .setRank(TELECOM_RANK);
        }

        return null;
    }

    private String getTelecomValue(TEL telecom) {
        var value = telecom.getValue();
        if (value != null) {
            return isWorkPlaceValue(telecom.getUse()) ? stripTelPrefix(value) : value;
        }

        return null;
    }

    private boolean isWorkPlaceValue(List<CsTelecommunicationAddressUse> use) {
        return use.stream()
            .anyMatch(addressUse -> WORK_PLACE.equals(addressUse.value()));
    }

    private String stripTelPrefix(String value) {
        return value.substring(TEL_PREFIX_INT); // strips the 'tel:' prefix on phone numbers
    }

    private Address getOrganizationAddress(List<AD> addr) {
        var address = addr.stream()
            .filter(this::isValidAddress)
            .findFirst();
        if (address.isPresent()) {
            var mappedAddress = new Address();
            var validAddress = address.get();

            if (validAddress.getStreetAddressLine() != null) {
                validAddress.getStreetAddressLine()
                    .forEach(addressLine -> mappedAddress.addLine(addressLine));
            }
            if (StringUtils.isNotEmpty(validAddress.getPostalCode())) {
                mappedAddress.setPostalCode(validAddress.getPostalCode());
            }

            return mappedAddress
                .setUse(AddressUse.WORK)
                .setType(AddressType.PHYSICAL);
        }
        return null;
    }

    private boolean isValidAddress(AD address) {
        if (address != null && address.getUse() != null) {
            var use = address.getUse().stream().findFirst();
            return use.isPresent() && use.stream().anyMatch(addressUse -> WORK_PLACE.equals(addressUse.value()));
        }

        return false;
    }

    private CodeableConcept getText(CV code) {
        var codeableConcept = new CodeableConcept();
        if (code != null) {
            if (StringUtils.isNotEmpty(code.getOriginalText())) {
                return codeableConcept.setText(code.getOriginalText());
            } else if (StringUtils.isNotEmpty(code.getDisplayName())) {
                return codeableConcept.setText(code.getDisplayName());
            }
        }

        return null;
    }

    private Identifier getOrganizationIdentifier(II id) {
        var identifier = new Identifier();
        if (isValidIdentifier(id)) {
            return identifier
                .setSystem(ORG_IDENTIFIER_SYSTEM)
                .setValue(id.getExtension());
        }

        return null;
    }

    private boolean isValidIdentifier(II id) {
        return id != null && id.getRoot() != null && id.getExtension() != null && ORG_ROOT.equals(id.getRoot());
    }

    private PractitionerRole createPractitionerRole(String id, CV code) {
        var practitionerRole = new PractitionerRole();

        practitionerRole.setId(id + PRACT_ROLE_SUFFIX);
        practitionerRole.getMeta().getProfile().add(new UriType(PRACT_ROLE_META_PROFILE));
        practitionerRole.setPractitioner(new Reference(PRACT_PREFIX + id));
        practitionerRole.setOrganization(new Reference(ORG_PREFIX + id + ORG_ID_SUFFIX));
        practitionerRole.getCode().add(getText(code));

        return practitionerRole;
    }
}
