package uk.nhs.adaptors.pss.translator.service;

import java.util.Collection;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;
import org.hl7.v3.RCMRMT030101UK04Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.pss.translator.generator.BundleGenerator;
import uk.nhs.adaptors.pss.translator.mapper.AgentDirectoryMapper;
import uk.nhs.adaptors.pss.translator.mapper.CodeableConceptMapper;
import uk.nhs.adaptors.pss.translator.mapper.ConditionMapper;
import uk.nhs.adaptors.pss.translator.mapper.DateTimeMapper;
import uk.nhs.adaptors.pss.translator.mapper.LocationMapper;
import uk.nhs.adaptors.pss.translator.mapper.PatientMapper;
import uk.nhs.adaptors.pss.translator.mapper.ProcedureRequestMapper;
import uk.nhs.adaptors.pss.translator.mapper.ReferralRequestMapper;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BundleMapperService {

    private final BundleGenerator generator;

    private final PatientMapper patientMapper;
    private final AgentDirectoryMapper agentDirectoryMapper;
    private final LocationMapper locationMapper;
    private final ProcedureRequestMapper procedureRequestMapper;
    private final ReferralRequestMapper referralRequestMapper;

    public Bundle mapToBundle(RCMRIN030000UK06Message xmlMessage) {
        LOGGER.info("Mapping the Bundle");
        Bundle bundle = generator.generateBundle();
        final RCMRMT030101UK04EhrFolder ehrFolder = getEhrFolder(xmlMessage);

     //   var agents = mapAgentDirectories(ehrFolder);
     //   var patient = mapPatient(getEhrExtract(xmlMessage), findPatientOrganization(agents));

     //   bundle.addEntry(patient);
    //    agents.forEach(agent -> bundle.addEntry(new Bundle.BundleEntryComponent().setResource(agent)));

        return bundle;
    }

    private List<? extends DomainResource> mapAgentDirectories(RCMRMT030101UK04EhrFolder ehrFolder) {
        return agentDirectoryMapper.mapAgentDirectory(ehrFolder.getResponsibleParty().getAgentDirectory());
    }

    private List<Location> mapLocations(RCMRMT030101UK04EhrFolder ehrFolder) {
        return ehrFolder.getComponent().stream()
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .filter(ehrComposition -> ehrComposition.getLocation() != null)
            .map(e -> locationMapper.mapToLocation(e.getLocation(), e.getId().toString()))
            .toList();
    }

    private Organization findPatientOrganization(List<? extends DomainResource> agents) {
        return agents.stream()
            .filter(agent -> ResourceType.Organization.equals(agent.getResourceType()))
            .map(Organization.class::cast)
            .findFirst().get();
    }

    private Patient mapPatient(RCMRMT030101UK04EhrExtract ehrExtract, Organization organization) {
        RCMRMT030101UK04Patient xmlPatient = ehrExtract.getRecordTarget().getPatient();
        return patientMapper.mapToPatient(xmlPatient, organization);
    }

    private List<RCMRMT030101UK04Component4> mapComponents(RCMRMT030101UK04EhrFolder ehrFolder) {
        return ehrFolder.getComponent()
            .stream()
            .map(component3 -> component3.getEhrComposition().getComponent())
            .flatMap(Collection::stream)
            .toList();
    }

    private RCMRMT030101UK04EhrFolder getEhrFolder(RCMRIN030000UK06Message xmlMessage) {
        return xmlMessage.getControlActEvent().getSubject().getEhrExtract().getComponent().get(0).getEhrFolder();
    }

    private RCMRMT030101UK04EhrExtract getEhrExtract(RCMRIN030000UK06Message xmlMessage) {
        return xmlMessage.getControlActEvent().getSubject().getEhrExtract();
    }
}
