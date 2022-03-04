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
import uk.nhs.adaptors.pss.translator.mapper.BloodPressureMapper;
import uk.nhs.adaptors.pss.translator.mapper.ConditionMapper;
import uk.nhs.adaptors.pss.translator.mapper.EncounterMapper;
import uk.nhs.adaptors.pss.translator.mapper.ImmunizationMapper;
import uk.nhs.adaptors.pss.translator.mapper.LocationMapper;
import uk.nhs.adaptors.pss.translator.mapper.ObservationCommentMapper;
import uk.nhs.adaptors.pss.translator.mapper.ObservationMapper;
import uk.nhs.adaptors.pss.translator.mapper.PatientMapper;
import uk.nhs.adaptors.pss.translator.mapper.ProcedureRequestMapper;
import uk.nhs.adaptors.pss.translator.mapper.ReferralRequestMapper;
import uk.nhs.adaptors.pss.translator.mapper.UnknownPractitionerHandler;
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
    private final BloodPressureMapper bloodPressureMapper;
    private final ObservationMapper observationMapper;
    private final ConditionMapper conditionMapper;
    private final ImmunizationMapper immunizationMapper;
    private final UnknownPractitionerHandler unknownPractitionerHandler;

    public Bundle mapToBundle(RCMRIN030000UK06Message xmlMessage) {
        Bundle bundle = generator.generateBundle();
        final RCMRMT030101UK04EhrExtract ehrExtract = getEhrExtract(xmlMessage);
        final RCMRMT030101UK04EhrFolder ehrFolder = getEhrFolder(xmlMessage);

        var practiseCode = getPractiseCode(ehrExtract);

        var agents = mapAgentDirectories(ehrFolder);
        var patient = mapPatient(getEhrExtract(xmlMessage), getPatientOrganization(agents));
        addEntry(bundle, patient);
        addEntries(bundle, agents);

        var mappedEncounterEhrCompositions = mapEncounters(ehrExtract, patient, practiseCode);
        var encounters = (List<Encounter>) mappedEncounterEhrCompositions.get(ENCOUNTER_KEY);
        var consultations = (List<ListResource>) mappedEncounterEhrCompositions.get(CONSULTATION_KEY);
        var topics = (List<ListResource>) mappedEncounterEhrCompositions.get(TOPIC_KEY);
        var categories = (List<ListResource>) mappedEncounterEhrCompositions.get(CATEGORY_KEY);
        addEntries(bundle, encounters);
        addEntries(bundle, consultations);
        addEntries(bundle, topics);
        addEntries(bundle, categories);

        var locations = mapLocations(ehrFolder, practiseCode);
        addEntries(bundle, locations);

        var procedureRequests = mapProcedureRequests(ehrExtract, patient, encounters, practiseCode);
        addEntries(bundle, procedureRequests);

        var referralRequests = mapReferralRequests(ehrExtract, patient, encounters, practiseCode);
        addEntries(bundle, referralRequests);

        var medicationResources = medicationRequestMapper.mapResources(ehrExtract, encounters, patient, practiseCode);
        addEntries(bundle, medicationResources);

        var bloodPressures = mapBloodPressures(ehrExtract, patient, encounters, practiseCode);
        addEntries(bundle, bloodPressures);

        var observations = mapObservations(ehrExtract, patient, encounters, practiseCode);
        addEntries(bundle, observations);

        var immunizations = mapImmunizations(ehrExtract, patient, encounters, practiseCode);
        addEntries(bundle, immunizations);

        var conditions = mapConditions(ehrExtract, patient, encounters, practiseCode);
        addEntries(bundle, conditions);

        var observationComments =
            mapObservationComments(ehrExtract, patient, encounters, practiseCode);
        addEntries(bundle, observationComments);

        LOGGER.debug("Mapped Bundle with [{}] entries", bundle.getEntry().size());

        conditionMapper.addReferences(bundle, conditions, ehrExtract);
        unknownPractitionerHandler.updateUnknownPractitionersRefs(bundle);

        return bundle;
    }

    private Map<String, List<? extends DomainResource>> mapEncounters(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient,
        String practiseCode) {
        return encounterMapper.mapEncounters(ehrExtract, patient, practiseCode);
    }

    private List<Immunization> mapImmunizations(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounterList,
        String practiseCode) {
        return immunizationMapper.mapToImmunization(ehrExtract, patient, encounterList, practiseCode);
    }

    private List<? extends DomainResource> mapAgentDirectories(RCMRMT030101UK04EhrFolder ehrFolder) {
        return agentDirectoryMapper.mapAgentDirectory(ehrFolder.getResponsibleParty().getAgentDirectory());
    }

    private List<Location> mapLocations(RCMRMT030101UK04EhrFolder ehrFolder, String practiseCode) {
        return ehrFolder.getComponent().stream()
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .filter(ehrComposition -> ehrComposition.getLocation() != null)
            .map(ehrComposition -> locationMapper.mapToLocation(ehrComposition.getLocation(), ehrComposition.getId().getRoot(),
                practiseCode))
            .toList();
    }

    private Patient mapPatient(RCMRMT030101UK04EhrExtract ehrExtract, Organization organization) {
        RCMRMT030101UK04Patient xmlPatient = ehrExtract.getRecordTarget().getPatient();
        return patientMapper.mapToPatient(xmlPatient, organization);
    }

    private List<ReferralRequest> mapReferralRequests(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        String practiseCode) {
        return referralRequestMapper.mapReferralRequests(ehrExtract, patient, encounters, practiseCode);
    }

    private List<ProcedureRequest> mapProcedureRequests(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient,
        List<Encounter> encounters, String practiseCode) {
        return procedureRequestMapper.mapProcedureRequests(ehrExtract, patient, encounters, practiseCode);
    }

    private List<Observation> mapObservationComments(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        String practiseCode) {
        return observationCommentMapper.mapObservations(ehrExtract, patient, encounters, practiseCode);
    }

    private List<Observation> mapBloodPressures(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        String practiseCode) {
        return bloodPressureMapper.mapBloodPressure(ehrExtract, patient, encounters, practiseCode);
    }

    private List<Observation> mapObservations(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        String practiseCode) {
        return observationMapper.mapObservations(ehrExtract, patient, encounters, practiseCode);
    }

    private List<Condition> mapConditions(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        String practiseCode) {
        return conditionMapper.mapConditions(ehrExtract, patient, encounters, practiseCode);
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

    private String getPractiseCode(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getAuthor().getAgentOrgSDS().getAgentOrganizationSDS().getId().getExtension();
    }
}