package uk.nhs.adaptors.pss.translator.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.ListResource;
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
import uk.nhs.adaptors.pss.translator.mapper.EncounterMapper;
import uk.nhs.adaptors.pss.translator.mapper.ImmunizationMapper;
import uk.nhs.adaptors.pss.translator.mapper.LocationMapper;
import uk.nhs.adaptors.pss.translator.mapper.ObservationCommentMapper;

import uk.nhs.adaptors.pss.translator.mapper.ObservationMapper;

import uk.nhs.adaptors.pss.translator.mapper.PatientMapper;
import uk.nhs.adaptors.pss.translator.mapper.ProcedureRequestMapper;
import uk.nhs.adaptors.pss.translator.mapper.ReferralRequestMapper;
import uk.nhs.adaptors.pss.translator.mapper.medication.MedicationRequestMapper;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BundleMapperService {
    private static final String ENCOUNTER_KEY = "encounters";
    private static final String CONSULTATION_KEY = "consultations";
    private static final String TOPIC_KEY = "topics";
    private static final String CATEGORY_KEY = "categories";

    private final BundleGenerator generator;

    private final PatientMapper patientMapper;
    private final AgentDirectoryMapper agentDirectoryMapper;
    private final EncounterMapper encounterMapper;
    private final LocationMapper locationMapper;
    private final ProcedureRequestMapper procedureRequestMapper;
    private final ReferralRequestMapper referralRequestMapper;
    private final MedicationRequestMapper medicationRequestMapper;
    private final ObservationCommentMapper observationCommentMapper;
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

        var mappedEncounterEhrCompositions = mapEncounters(ehrExtract, patient);
        var encounters = (List<Encounter>) mappedEncounterEhrCompositions.get(ENCOUNTER_KEY);
        var consultations = (List<ListResource>) mappedEncounterEhrCompositions.get(CONSULTATION_KEY);
        var topics = (List<ListResource>) mappedEncounterEhrCompositions.get(TOPIC_KEY);
        var categories = (List<ListResource>) mappedEncounterEhrCompositions.get(CATEGORY_KEY);
        addEntries(bundle, encounters);
        addEntries(bundle, consultations);

        var locations = mapLocations(ehrFolder);
        addEntries(bundle, locations);

        var procedureRequests = mapProcedureRequests(ehrExtract, patient);
        addEntries(bundle, procedureRequests);

        var referralRequests = mapReferralRequests(ehrFolder, patient);
        addEntries(bundle, referralRequests);

        var medicationResources = medicationRequestMapper.mapResources(ehrExtract, encounters, patient);
        addEntries(bundle, medicationResources);

        var observations = mapObservations(ehrExtract, patient, encounters);
        addEntries(bundle, observations);

        var immunizations = mapImmunizations(ehrExtract, patient, encounters);
        addEntries(bundle, immunizations);

        var conditions = mapConditions(ehrExtract, patient, encounters);
        addEntries(bundle, conditions);

        var observationComments =
            mapObservationComments(ehrExtract, patient, encounters);
        addEntries(bundle, observationComments);

        // TODO: Add references to mapped resources in their appropriate lists (NIAD-2051)
        addEntries(bundle, topics);
        addEntries(bundle, categories);

        LOGGER.debug("Mapped Bundle with [{}] entries", bundle.getEntry().size());

        conditionMapper.addReferences(bundle, conditions, ehrExtract);

        return bundle;
    }

    private Map<String, List<? extends DomainResource>> mapEncounters(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient) {
        return encounterMapper.mapEncounters(ehrExtract, patient);
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

    private List<Observation> mapObservationComments(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters) {
        return observationCommentMapper.mapObservations(ehrExtract, patient, encounters);
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