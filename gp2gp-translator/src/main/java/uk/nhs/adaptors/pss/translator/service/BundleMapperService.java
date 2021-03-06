package uk.nhs.adaptors.pss.translator.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.ListResource;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
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
import uk.nhs.adaptors.pss.translator.exception.BundleMappingException;
import uk.nhs.adaptors.pss.translator.generator.BundleGenerator;
import uk.nhs.adaptors.pss.translator.mapper.AgentDirectoryMapper;
import uk.nhs.adaptors.pss.translator.mapper.AllergyIntoleranceMapper;
import uk.nhs.adaptors.pss.translator.mapper.BloodPressureMapper;
import uk.nhs.adaptors.pss.translator.mapper.ConditionMapper;
import uk.nhs.adaptors.pss.translator.mapper.DocumentReferenceMapper;
import uk.nhs.adaptors.pss.translator.mapper.diagnosticreport.DiagnosticReportMapper;
import uk.nhs.adaptors.pss.translator.mapper.EncounterMapper;
import uk.nhs.adaptors.pss.translator.mapper.ImmunizationMapper;
import uk.nhs.adaptors.pss.translator.mapper.LocationMapper;
import uk.nhs.adaptors.pss.translator.mapper.ObservationCommentMapper;
import uk.nhs.adaptors.pss.translator.mapper.ObservationMapper;
import uk.nhs.adaptors.pss.translator.mapper.OrganizationMapper;
import uk.nhs.adaptors.pss.translator.mapper.PatientMapper;
import uk.nhs.adaptors.pss.translator.mapper.ProcedureRequestMapper;
import uk.nhs.adaptors.pss.translator.mapper.ReferralRequestMapper;
import uk.nhs.adaptors.pss.translator.mapper.diagnosticreport.SpecimenCompoundsMapper;
import uk.nhs.adaptors.pss.translator.mapper.diagnosticreport.SpecimenMapper;
import uk.nhs.adaptors.pss.translator.mapper.TemplateMapper;
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
    private final DocumentReferenceMapper documentReferenceMapper;
    private final TemplateMapper templateMapper;
    private final OrganizationMapper organizationMapper;
    private final AllergyIntoleranceMapper allergyIntoleranceMapper;
    private final DiagnosticReportMapper diagnosticReportMapper;
    private final SpecimenMapper specimenMapper;
    private final SpecimenCompoundsMapper specimenCompoundsMapper;

    public Bundle mapToBundle(RCMRIN030000UK06Message xmlMessage, String losingPracticeOdsCode) throws BundleMappingException {

        try {

            Bundle bundle = generator.generateBundle();
            final RCMRMT030101UK04EhrExtract ehrExtract = getEhrExtract(xmlMessage);
            final RCMRMT030101UK04EhrFolder ehrFolder = getEhrFolder(xmlMessage);

            var agents = mapAgentDirectories(ehrFolder);
            var patient = mapPatient(getEhrExtract(xmlMessage), getPatientOrganization(agents));
            addEntry(bundle, patient);

            Organization authorOrg = organizationMapper.mapAuthorOrganization(losingPracticeOdsCode);
            addEntry(bundle, authorOrg);

            addEntries(bundle, agents);

            var mappedEncounterEhrCompositions = mapEncounters(ehrExtract, patient, losingPracticeOdsCode);
            var encounters = handleMappedEncounterResources(mappedEncounterEhrCompositions, bundle);

            var locations = mapLocations(ehrFolder, losingPracticeOdsCode);
            addEntries(bundle, locations);

            var procedureRequests = procedureRequestMapper.mapResources(ehrExtract, patient, encounters, losingPracticeOdsCode);
            addEntries(bundle, procedureRequests);

            var referralRequests = referralRequestMapper.mapResources(ehrExtract, patient, encounters, losingPracticeOdsCode);
            addEntries(bundle, referralRequests);

            var medicationResources = medicationRequestMapper.mapResources(ehrExtract, patient, encounters, losingPracticeOdsCode);
            addEntries(bundle, medicationResources);

            var bloodPressures = bloodPressureMapper.mapResources(ehrExtract, patient, encounters, losingPracticeOdsCode);
            addEntries(bundle, bloodPressures);

            var observations = observationMapper.mapResources(ehrExtract, patient, encounters, losingPracticeOdsCode);
            addEntries(bundle, observations);

            var immunizations = immunizationMapper.mapResources(ehrExtract, patient, encounters, losingPracticeOdsCode);
            addEntries(bundle, immunizations);

            var conditions = conditionMapper.mapResources(ehrExtract, patient, encounters, losingPracticeOdsCode);
            addEntries(bundle, conditions);

            var observationComments = observationCommentMapper.mapResources(ehrExtract, patient, encounters, losingPracticeOdsCode);
            addEntries(bundle, observationComments);

            var documentReferences = documentReferenceMapper.mapResources(ehrExtract, patient, encounters, authorOrg);
            addEntries(bundle, documentReferences);

            var templates = templateMapper.mapResources(ehrExtract, patient, encounters, losingPracticeOdsCode);
            addEntries(bundle, templates);

            var allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, patient, encounters, losingPracticeOdsCode);
            addEntries(bundle, allergyIntolerances);

            mapDiagnosticReports(bundle, ehrExtract, patient, encounters, observations, observationComments, losingPracticeOdsCode);

            conditionMapper.addReferences(bundle, conditions, ehrExtract);
            unknownPractitionerHandler.updateUnknownPractitionersRefs(bundle);

            LOGGER.debug("Mapped Bundle with [{}] entries", bundle.getEntry().size());

            return bundle;
        } catch (Exception e) {
            throw new BundleMappingException(e.getMessage());
        }
    }

    private void mapDiagnosticReports(Bundle bundle, RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        List<Observation> observations, List<Observation> observationComments, String practiceCode) {
        var diagnosticReports = diagnosticReportMapper.mapResources(ehrExtract, patient, encounters, practiceCode);
        diagnosticReportMapper.handleChildObservationComments(ehrExtract, observationComments);
        var specimen = specimenMapper.mapSpecimen(ehrExtract, diagnosticReports, patient, practiceCode);
        addEntries(bundle, diagnosticReports);
        addEntries(bundle, specimen);
        var batteryObservations = specimenCompoundsMapper.handleSpecimenChildComponents(ehrExtract, observations, observationComments,
            diagnosticReports, patient, encounters, practiceCode);
        addEntries(bundle, batteryObservations);
    }

    private List<Encounter> handleMappedEncounterResources(Map<String, List<? extends DomainResource>> mappedEncounterEhrCompositions,
        Bundle bundle) {
        var encounters = (List<Encounter>) mappedEncounterEhrCompositions.get(ENCOUNTER_KEY);
        var consultations = (List<ListResource>) mappedEncounterEhrCompositions.get(CONSULTATION_KEY);
        var topics = (List<ListResource>) mappedEncounterEhrCompositions.get(TOPIC_KEY);
        var categories = (List<ListResource>) mappedEncounterEhrCompositions.get(CATEGORY_KEY);

        addEntries(bundle, encounters);
        addEntries(bundle, consultations);
        addEntries(bundle, topics);
        addEntries(bundle, categories);

        return encounters;
    }

    private Map<String, List<? extends DomainResource>> mapEncounters(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient,
        String losingPracticeOdsCode) {
        return encounterMapper.mapEncounters(ehrExtract, patient, losingPracticeOdsCode);
    }

    private List<? extends DomainResource> mapAgentDirectories(RCMRMT030101UK04EhrFolder ehrFolder) {
        return agentDirectoryMapper.mapAgentDirectory(ehrFolder.getResponsibleParty().getAgentDirectory());
    }

    private List<Location> mapLocations(RCMRMT030101UK04EhrFolder ehrFolder, String losingPracticeOdsCode) {

        return ehrFolder.getComponent().stream()
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .filter(ehrComposition -> ehrComposition.getLocation() != null)
            .map(ehrComposition -> locationMapper.mapToLocation(ehrComposition.getLocation(), ehrComposition.getId().getRoot(),
                losingPracticeOdsCode)).toList();
    }

    private Patient mapPatient(RCMRMT030101UK04EhrExtract ehrExtract, Organization organization) {
        RCMRMT030101UK04Patient xmlPatient = ehrExtract.getRecordTarget().getPatient();
        return patientMapper.mapToPatient(xmlPatient, organization);
    }

    private Organization getPatientOrganization(List<? extends DomainResource> agents) {
        return agents.stream()
            .filter(agent -> ResourceType.Organization.equals(agent.getResourceType()))
            .map(Organization.class::cast)
            .findFirst()
            .orElse(null);
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