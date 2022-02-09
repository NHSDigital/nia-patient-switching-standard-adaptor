package uk.nhs.adaptors.pss.translator.service;

import java.util.Collection;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.ReferralRequest;
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
import uk.nhs.adaptors.pss.translator.mapper.ConditionMapper;
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
    private final ConditionMapper conditionMapper;

    public Bundle mapToBundle(RCMRIN030000UK06Message xmlMessage) {
        Bundle bundle = generator.generateBundle();
        final RCMRMT030101UK04EhrExtract ehrExtract = getEhrExtract(xmlMessage);
        final RCMRMT030101UK04EhrFolder ehrFolder = getEhrFolder(xmlMessage);

        var agents = mapAgentDirectories(ehrFolder);
        var patient = mapPatient(getEhrExtract(xmlMessage), getPatientOrganization(agents));
        bundle.addEntry(new BundleEntryComponent().setResource(patient));
        agents.forEach(agent -> bundle.addEntry(new BundleEntryComponent().setResource(agent)));

        var locations = mapLocations(ehrFolder);
        locations.forEach(location -> bundle.addEntry(new BundleEntryComponent().setResource(location)));

        var procedureRequests = mapProcedureRequests(ehrExtract);
        procedureRequests.forEach(procedureRequest -> bundle.addEntry(new BundleEntryComponent().setResource(procedureRequest)));

        var referralRequests = mapReferralRequests(ehrFolder);
        referralRequests.forEach(referralRequest -> bundle.addEntry(new BundleEntryComponent().setResource(referralRequest)));

        var conditions = mapConditions();
        conditions.forEach(condition -> bundle.addEntry(new BundleEntryComponent().setResource(condition)));

        LOGGER.debug("Mapped Bundle with [{}] entries", bundle.getEntry().size());
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

    private Patient mapPatient(RCMRMT030101UK04EhrExtract ehrExtract, Organization organization) {
        RCMRMT030101UK04Patient xmlPatient = ehrExtract.getRecordTarget().getPatient();
        return patientMapper.mapToPatient(xmlPatient, organization);
    }

    private List<ReferralRequest> mapReferralRequests(RCMRMT030101UK04EhrFolder ehrFolder) {
        return ehrFolder.getComponent()
            .stream()
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .flatMap(e -> e.getComponent().stream()
                .filter(f -> f.getRequestStatement() != null)
                .map(RCMRMT030101UK04Component4::getRequestStatement)
                .map(g -> referralRequestMapper.mapToReferralRequest(e, g)))
            .toList();
    }

    private List<ProcedureRequest> mapProcedureRequests(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent().get(0).getEhrFolder().getComponent()
            .stream()
            .map(e -> e.getEhrComposition().getComponent())
            .flatMap(Collection::stream)
            .filter(e -> e.getPlanStatement() != null)
            .map(RCMRMT030101UK04Component4::getPlanStatement)
            .map(e -> procedureRequestMapper.mapToProcedureRequest(ehrExtract, e))
            .toList();
    }

    private List<Condition> mapConditions(RCMRMT030101UK04EhrFolder ehrFolder, Patient patient, Practitioner practitioner) {
        var ehrCompositions = ehrFolder.getComponent()
            .stream()
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .toList();

       // ehrCompositions.stream().map(ehrComposition -> );
        return List.of();
    }

    private Organization getPatientOrganization(List<? extends DomainResource> agents) {
        return agents.stream()
            .filter(e -> ResourceType.Organization.equals(e.getResourceType()))
            .map(Organization.class::cast)
            .findFirst().get();
    }

    private RCMRMT030101UK04EhrFolder getEhrFolder(RCMRIN030000UK06Message xmlMessage) {
        return xmlMessage.getControlActEvent().getSubject().getEhrExtract().getComponent().get(0).getEhrFolder();
    }

    private RCMRMT030101UK04EhrExtract getEhrExtract(RCMRIN030000UK06Message xmlMessage) {
        return xmlMessage.getControlActEvent().getSubject().getEhrExtract();
    }
}
