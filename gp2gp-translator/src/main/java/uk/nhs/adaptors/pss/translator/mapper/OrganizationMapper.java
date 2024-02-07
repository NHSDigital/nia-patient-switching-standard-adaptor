package uk.nhs.adaptors.pss.translator.mapper;

import static uk.nhs.adaptors.pss.translator.util.OrganizationUtil.getOdsCode;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.service.IdGeneratorService;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrganizationMapper {
    private static final String ORG_META_PROFILE = "Organization-1";
    private static final String ORG_IDENTIFIER_SYSTEM = "https://fhir.nhs.uk/Id/ods-organization-code";

    private final IdGeneratorService idGenerator;

    public Organization mapAuthorOrganization(String odsCode, List<Resource> agents) {

        Map<String, Organization> organisations = getMappedOrganisationsByOdsCode(agents);

        if (organisations.containsKey(odsCode)) {
            return organisations.get(odsCode);
        }

        Organization organization = new Organization();
        organization.setId(idGenerator.generateUuid());
        organization.addIdentifier(new Identifier()
            .setSystem(ORG_IDENTIFIER_SYSTEM)
            .setValue(odsCode));
        organization.setMeta(generateMeta(ORG_META_PROFILE));

        return organization;
    }

    private Map<String, Organization> getMappedOrganisationsByOdsCode(List<Resource> agents) {
        return agents.stream()
            .filter(resource -> resource.getResourceType().equals(ResourceType.Organization))
            .map(Organization.class::cast)
            .filter(Organization::hasIdentifier)
            .filter(organization -> organization.getIdentifier()
                .stream()
                .anyMatch(identifier -> identifier.getSystem().equals(ORG_IDENTIFIER_SYSTEM) && identifier.hasValue())
            ).collect(Collectors.toMap(
                organization -> getOdsCode(organization).orElseThrow(),
                organization -> organization
            ));
    }
}
