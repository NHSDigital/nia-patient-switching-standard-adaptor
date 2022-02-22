package uk.nhs.adaptors.pss.translator.service;

import java.util.Collection;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.hl7.v3.RCMRMT030101UK04Component3;
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
import uk.nhs.adaptors.pss.translator.mapper.ImmunizationMapper;
import uk.nhs.adaptors.pss.translator.mapper.LocationMapper;
import uk.nhs.adaptors.pss.translator.mapper.ObservationMapper;
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
    private final ObservationMapper observationMapper;
    private final ConditionMapper conditionMapper;
    private final ImmunizationMapper immunizationMapper;

    public Bundle mapToBundle(RCMRIN030000UK06Message xmlMessage) {
        Bundle bundle = generator.generateBundle();
        final RCMRMT030101UK04EhrExtract ehrExtract = getEhrExtract(xmlMessage);
        final RCMRMT030101UK04EhrFolder ehrFolder = getEhrFolder(xmlMessage);

        var agents = mapAgentDirectories(ehrFolder);
        var patient = mapPatient(getEhrExtract(xmlMessage), getPatientOrganization(agents));
        addEntry(bundle, patient);
        addEntries(bundle, agents);

        var locations = mapLocations(ehrFolder);
        addEntries(bundle, locations);

        var procedureRequests = mapProcedureRequests(ehrExtract, patient);
        addEntries(bundle, procedureRequests);

        var referralRequests = mapReferralRequests(ehrFolder, patient);
        addEntries(bundle, referralRequests);

        var observations = mapObservations(ehrExtract, patient, List.of()); //TODO: Provide list of encounters
        addEntries(bundle, observations);

        var immunizations = mapImmunizations(ehrExtract, patient, List.of()); // TODO: Insert encounter list (NIAD-1961)
        addEntries(bundle, immunizations);

        var conditions = mapConditions(ehrExtract, patient, List.of()); //TODO: Provide list of encounters
        addEntries(bundle, conditions);

        LOGGER.debug("Mapped Bundle with [{}] entries", bundle.getEntry().size());

        conditionMapper.addReferences(bundle, conditions, ehrExtract); //add after mapping all resources

        return bundle;
    }

    private List<Immunization> mapImmunizations(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounterList) {
        return immunizationMapper.mapToImmunization(ehrExtract, patient, encounterList);
    }

    private List<? extends DomainResource> mapAgentDirectories(RCMRMT030101UK04EhrFolder ehrFolder) {
        return agentDirectoryMapper.mapAgentDirectory(ehrFolder.getResponsibleParty().getAgentDirectory());
    }

    private List<Location> mapLocations(RCMRMT030101UK04EhrFolder ehrFolder) {
        return ehrFolder.getComponent().stream()
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .filter(ehrComposition -> ehrComposition.getLocation() != null)
            .map(ehrComposition -> locationMapper.mapToLocation(ehrComposition.getLocation(), ehrComposition.getId().getRoot()))
            .toList();
    }

    private Patient mapPatient(RCMRMT030101UK04EhrExtract ehrExtract, Organization organization) {
        RCMRMT030101UK04Patient xmlPatient = ehrExtract.getRecordTarget().getPatient();
        return patientMapper.mapToPatient(xmlPatient, organization);
    }

    private List<ReferralRequest> mapReferralRequests(RCMRMT030101UK04EhrFolder ehrFolder, Patient patient) {
        return ehrFolder.getComponent()
            .stream()
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .flatMap(ehrComposition -> ehrComposition.getComponent().stream()
                .filter(component4 -> component4.getRequestStatement() != null)
                .map(component4 -> referralRequestMapper.mapToReferralRequest(ehrComposition, component4.getRequestStatement(), patient)))
            .toList();
    }

    private List<ProcedureRequest> mapProcedureRequests(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient) {
        return ehrExtract.getComponent().get(0).getEhrFolder().getComponent()
            .stream()
            .flatMap(component3 -> component3.getEhrComposition().getComponent().stream())
            .filter(component4 -> component4.getPlanStatement() != null)
            .map(component4 -> procedureRequestMapper.mapToProcedureRequest(ehrExtract, component4.getPlanStatement(), patient))
            .toList();
    }

    private List<Observation> mapObservations(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters) {
        return observationMapper.mapObservations(ehrExtract, patient, encounters);
    }

    private List<Condition> mapConditions(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters) {
        return conditionMapper.mapConditions(ehrExtract, patient, encounters);
    }

    private Organization getPatientOrganization(List<? extends DomainResource> agents) {
        return agents.stream()
            .filter(agent -> ResourceType.Organization.equals(agent.getResourceType()))
            .map(Organization.class::cast)
            .findFirst().get();
    }

    private RCMRMT030101UK04EhrFolder getEhrFolder(RCMRIN030000UK06Message xmlMessage) {
        return xmlMessage.getControlActEvent().getSubject().getEhrExtract().getComponent().get(0).getEhrFolder();
    }

    private RCMRMT030101UK04EhrExtract getEhrExtract(RCMRIN030000UK06Message xmlMessage) {
        return xmlMessage.getControlActEvent().getSubject().getEhrExtract();
    }

    private <T extends DomainResource> void addEntries(Bundle bundle, Collection<T> resources) {
        resources.forEach(it -> addEntry(bundle, it));
    }

    private <T extends DomainResource> void addEntry(Bundle bundle, T resource) {
        bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }
}
