package uk.nhs.adaptors.pss.translator.mapper;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.v3.RCMRMT030101UK04AgentDirectory;

public class AgentMapper {

    public List mapToAgent(RCMRMT030101UK04AgentDirectory agentDirectory) {
        var agentList = new ArrayList<>();

        return agentList;
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


