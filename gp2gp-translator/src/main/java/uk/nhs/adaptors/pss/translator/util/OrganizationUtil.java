package uk.nhs.adaptors.pss.translator.util;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.ResourceType;

public class OrganizationUtil {

    private static final String ORG_IDENTIFIER_SYSTEM = "https://fhir.nhs.uk/Id/ods-organization-code";

    public static Optional<Organization> findDuplicateOrganisation(Organization newOrganisation, List<? extends DomainResource> agentResources) {
        var newOdsCode = getOdsCode(newOrganisation);

        if (newOdsCode.isEmpty()) {
            return Optional.empty();
        }

        return agentResources.stream()
            .filter(domainResource -> domainResource.getResourceType().equals(ResourceType.Organization))
            .map(Organization.class::cast)
            .filter(organization -> {
                var odsCode = getOdsCode(organization);
                return odsCode.isPresent() && odsCode.orElseThrow().equals(newOdsCode.orElseThrow());
            }).findFirst();
    }

    public static boolean organisationIsNotDuplicate(Organization organization, List<? extends DomainResource> agentResources) {
        return findDuplicateOrganisation(organization, agentResources).isEmpty();
    }

    public static Optional<String> getOdsCode(Organization organization) {
        if (!organization.hasIdentifier()) {
            return Optional.empty();
        }

        return organization.getIdentifier().stream()
            .filter(Identifier::hasSystem)
            .filter(identifier -> identifier.getSystem().equals(ORG_IDENTIFIER_SYSTEM))
            .filter(Identifier::hasValue)
            .map(Identifier::getValue)
            .findFirst();
    }
}
