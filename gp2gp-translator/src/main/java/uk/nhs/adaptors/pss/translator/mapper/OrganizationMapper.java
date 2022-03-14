package uk.nhs.adaptors.pss.translator.mapper;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Organization;
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

    public Organization mapAuthorOrganization(String odsCode) {
        Organization organization = new Organization();
        organization.setId(idGenerator.generateUuid());
        organization.addIdentifier(new Identifier()
            .setSystem(ORG_IDENTIFIER_SYSTEM)
            .setValue(odsCode));
        organization.setMeta(generateMeta(ORG_META_PROFILE));

        return organization;
    }
}
