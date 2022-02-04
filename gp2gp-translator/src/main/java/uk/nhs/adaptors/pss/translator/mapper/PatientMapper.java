package uk.nhs.adaptors.pss.translator.mapper;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.RCMRMT030101UK04Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.service.FhirIdGeneratorService;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PatientMapper {

    private final FhirIdGeneratorService idGenerator;

    private static final String NHS_NUMBER_SYSTEM_URL = "https://fhir.nhs.uk/Id/nhs-number";
    private static final String META_PROFILE_URL = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Patient-1";
    private static final String META_VERSION_ID = "1521806400000";

    public Patient mapToPatient(RCMRMT030101UK04Patient patient) {
        var identifier = createIdentifier(patient.getId().getExtension());
        return createPatient(identifier, null);
    }

    public Patient mapToPatient(RCMRMT030101UK04Patient patient, Organization organization) {
        var identifier = createIdentifier(patient.getId().getExtension());
        var managingOrganization = createManagingOrganizationReference(organization);
        return createPatient(identifier, managingOrganization);
    }

    private Identifier createIdentifier(String nhsNumber) {
        return new Identifier()
            .setSystem(NHS_NUMBER_SYSTEM_URL)
            .setValue(nhsNumber);
    }

    private Meta createMeta() {
        return new Meta()
            .setVersionId(META_VERSION_ID)
            .addProfile(META_PROFILE_URL);
    }

    private Reference createManagingOrganizationReference(Organization organization) {
        return new Reference(organization.getIdElement());
    }

    private Patient createPatient(Identifier identifier, Reference managingOrganizationReference) {
        return (Patient) new Patient()
            .addIdentifier(identifier)
            .setManagingOrganization(managingOrganizationReference)
            .setId(idGenerator.generateUuid())
            .setMeta(createMeta());
    }
}
