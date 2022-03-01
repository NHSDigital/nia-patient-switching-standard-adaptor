package uk.nhs.adaptors.pss.translator.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.hl7.v3.RCMRMT030101UK04AgentDirectory;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04Location;
import org.hl7.v3.RCMRMT030101UK04Patient;
import org.hl7.v3.RCMRMT030101UK04PlanStatement;
import org.hl7.v3.RCMRMT030101UK04RequestStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
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
import uk.nhs.adaptors.pss.translator.mapper.medication.MedicationRequestMapper;

@ExtendWith(MockitoExtension.class)
public class BundleMapperServiceTest {

    private static final String XML_RESOURCES_BASE = "xml/RCMRIN030000UK06/";
    private static final String STRUCTURED_RECORD_XML = "structuredRecord.xml";
    private static final String ENCOUNTER_KEY = "encounters";
    private static final String CONSULTATION_KEY = "consultations";
    private static final String TOPIC_KEY = "topics";
    private static final String CATEGORY_KEY = "categories";

    @Mock
    private BundleGenerator bundleGenerator;
    @Mock
    private PatientMapper patientMapper;
    @Mock
    private AgentDirectoryMapper agentDirectoryMapper;
    @Mock
    private LocationMapper locationMapper;
    @Mock
    private EncounterMapper encounterMapper;
    @Mock
    private ProcedureRequestMapper procedureRequestMapper;
    @Mock
    private ReferralRequestMapper referralRequestMapper;
    @Mock
    private BloodPressureMapper bloodPressureMapper;
    @Mock
    private ObservationMapper observationMapper;
    @Mock
    private ConditionMapper conditionMapper;
    @Mock
    private ImmunizationMapper immunizationMapper;
    @Mock
    private ObservationCommentMapper observationCommentMapper;
    @Mock
    private MedicationRequestMapper medicationRequestMapper;

    @InjectMocks
    private BundleMapperService bundleMapperService;

    @BeforeEach
    public void setup() {
        when(bundleGenerator.generateBundle()).thenReturn(new Bundle());

        var agentResourceList = new ArrayList<DomainResource>();
        agentResourceList.add(new Organization());
        List mockedList = mock(List.class);

        Map<String, List<? extends DomainResource>> encounterResources = new HashMap<>();
        encounterResources.put(ENCOUNTER_KEY, new ArrayList<>());
        encounterResources.put(CONSULTATION_KEY, new ArrayList<>());
        encounterResources.put(TOPIC_KEY, new ArrayList<>());
        encounterResources.put(CATEGORY_KEY, new ArrayList<>());

        when(agentDirectoryMapper.mapAgentDirectory(any())).thenReturn(mockedList);
        when(mockedList.stream()).thenReturn(agentResourceList.stream());
        when(patientMapper.mapToPatient(any(RCMRMT030101UK04Patient.class), any(Organization.class))).thenReturn(new Patient());
        when(encounterMapper.mapEncounters(any(RCMRMT030101UK04EhrExtract.class), any(Patient.class))).thenReturn(encounterResources);
    }

    @Test
    public void testAllMappersHaveBeenUsed() {
        final RCMRIN030000UK06Message xml = unmarshallCodeElement(STRUCTURED_RECORD_XML);
        bundleMapperService.mapToBundle(xml);

        verify(patientMapper).mapToPatient(any(RCMRMT030101UK04Patient.class), any(Organization.class));
        verify(agentDirectoryMapper).mapAgentDirectory(any(RCMRMT030101UK04AgentDirectory.class));
        verify(locationMapper, atLeast(1)).mapToLocation(any(RCMRMT030101UK04Location.class), any(String.class));
        verify(encounterMapper).mapEncounters(any(RCMRMT030101UK04EhrExtract.class), any(Patient.class));
        verify(procedureRequestMapper).mapToProcedureRequest(
            any(RCMRMT030101UK04EhrExtract.class),
            any(RCMRMT030101UK04PlanStatement.class),
            any(Patient.class)
        );
        verify(referralRequestMapper).mapToReferralRequest(
            any(RCMRMT030101UK04EhrComposition.class),
            any(RCMRMT030101UK04RequestStatement.class),
            any(Patient.class)
        );
        verify(bloodPressureMapper).mapBloodPressure(any(RCMRMT030101UK04EhrExtract.class), any(Patient.class), anyList());
        verify(observationMapper).mapObservations(any(RCMRMT030101UK04EhrExtract.class), any(Patient.class), anyList());
        verify(conditionMapper).mapConditions(any(RCMRMT030101UK04EhrExtract.class), any(Patient.class), anyList());
        verify(immunizationMapper).mapToImmunization(any(RCMRMT030101UK04EhrExtract.class), any(Patient.class), anyList());
        verify(observationCommentMapper).mapObservations(any(RCMRMT030101UK04EhrExtract.class), any(Patient.class), anyList());
        verify(medicationRequestMapper, atLeast(1))
            .mapResources(any(RCMRMT030101UK04EhrExtract.class), anyList(), any(Patient.class));
    }

    @SneakyThrows
    private RCMRIN030000UK06Message unmarshallCodeElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRIN030000UK06Message.class);
    }
}
