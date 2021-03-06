package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.EncounterStatus;
import org.hl7.fhir.dstu3.model.ListResource;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.util.DatabaseImmunizationChecker;
import uk.nhs.adaptors.pss.translator.util.ResourceReferenceUtil;

@ExtendWith(MockitoExtension.class)
public class EncounterMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/Encounter/";
    private static final String ENCOUNTER_ID = "5EB5D070-8FE1-11EC-B1E5-0800200C9A66";
    private static final String ENCOUNTER_ID_2 = "6EB5D070-8FE1-11EC-B1E5-0800200C9A66";
    private static final String ENCOUNTER_KEY = "encounters";
    private static final String CONSULTATION_KEY = "consultations";
    private static final String TOPIC_KEY = "topics";
    private static final String CATEGORY_KEY = "categories";
    private static final String LOCATION_PREFIX = "Location/";
    private static final String LOCATION_SUFFIX = "-LOC";
    private static final String ENCOUNTER_META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Encounter-1";
    private static final String PRACTISE_CODE = "TESTPRACTISECODE";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/TESTPRACTISECODE";
    private static final String PATIENT_ID = "0E6F45F0-8D7B-11EC-B1E5-0800200C9A66";
    private static final String CODING_DISPLAY = "Ischaemic heart disease";
    private static final String FULL_VALID_STRUCTURED_ENCOUNTER_XML = "full_valid_structured_encounter.xml";
    private static final String FULL_VALID_STRUCTURED_ENCOUNTER_WITH_LINKSET_XML = "full_valid_structured_encounter_with_linkset.xml";
    private static final String FULL_VALID_FLAT_ENCOUNTER_XML = "full_valid_flat_encounter.xml";
    private static final String NO_OPTIONAL_FLAT_ENCOUNTER_XML = "no_optional_valid_flat_encounter.xml";
    private static final String INVALID_ENCOUNTER_CODE_1_XML = "invalid_encounter_code_1.xml";
    private static final String INVALID_ENCOUNTER_CODE_2_XML = "invalid_encounter_code_2.xml";
    private static final String SUPPRESSED_COMPOSITION_WITH_EHR_EMPTY_XML = "suppressed_with_ehr_empty.xml";
    private static final String SUPPRESSED_COMPOSITION_WITH_REGISTRATION_STATEMENT_XML = "suppressed_with_ehr_registration.xml";
    private static final String EFFECTIVE_CENTER_ENCOUNTER_PERIOD_XML = "effective_center_encounter_period.xml";
    private static final String EFFECTIVE_LOW_AND_HIGH_ENCOUNTER_PERIOD_XML = "effective_low_and_high_encounter_period.xml";
    private static final String EFFECTIVE_LOW_ENCOUNTER_PERIOD_XML = "effective_low_encounter_period.xml";
    private static final String EFFECTIVE_HIGH_ENCOUNTER_PERIOD_XML = "effective_high_encounter_period.xml";
    private static final String EFFECTIVE_CENTER_UNK_NULL_FLAVOR_ENCOUNTER_PERIOD_XML = "effective_center_null_flavor_encounter_period.xml";
    private static final String AVAILABILITY_TIME_ENCOUNTER_PERIOD_XML = "availability_time_encounter_period.xml";
    private static final String NO_ENCOUNTER_PERIOD_XML = "no_encounter_period.xml";
    private static final String NULL_FLAVOR_EFFECTIVE_TIMES_XML = "null_flavor_effective_times.xml";
    private static final String ENCOUNTER_WITH_MULTIPLE_COMPOUND_STATEMENTS_XML = "encounter_with_multiple_compound_statements.xml";
    private static final String FULL_VALID_STRUCTURED_ENCOUNTER_WITH_RESOURCES_XML = "full_valid_structured_encounter_with_resources.xml";
    private static final String FULL_VALID_FLAT_ENCOUNTER_WITH_RESOURCES_XML = "full_valid_flat_encounter_with_resources.xml";
    private static final int ONE_MAPPED_RESOURCE = 1;
    private static final int TWO_MAPPED_RESOURCES = 2;
    private static final String LINKSET_REFERENCE = "Condition/DCC26FC9-4D1C-11E3-A2DD-010000000161";

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @Mock
    private ConsultationListMapper consultationListMapper;

    @Mock
    private DatabaseImmunizationChecker immunizationChecker;

    @Mock
    private ResourceReferenceUtil resourceReferenceUtil;

    @InjectMocks
    private EncounterMapper encounterMapper;

    private Patient patient;

    @BeforeEach
    public void setup() {
        patient = new Patient();
        patient.setId(PATIENT_ID);
        setUpCodeableConceptMock();
    }

    @Test
    public void testEncountersWithMultipleCompoundStatements() {
        when(consultationListMapper.mapToConsultation(any(RCMRMT030101UK04EhrExtract.class), any(Encounter.class)))
            .thenReturn(getList());
        when(consultationListMapper.mapToTopic(any(ListResource.class), any(RCMRMT030101UK04CompoundStatement.class)))
            .thenReturn(getList());
        when(consultationListMapper.mapToCategory(any(ListResource.class), any(RCMRMT030101UK04CompoundStatement.class)))
            .thenReturn(getList());
        var ehrExtract = unmarshallEhrExtractElement(ENCOUNTER_WITH_MULTIPLE_COMPOUND_STATEMENTS_XML);

        Map<String, List<? extends DomainResource>> mappedResources = encounterMapper.mapEncounters(ehrExtract, patient, PRACTISE_CODE);

        var encounterList = mappedResources.get(ENCOUNTER_KEY);
        assertThat(mappedResources.get(ENCOUNTER_KEY).size()).isOne();
        assertThat(mappedResources.get(CONSULTATION_KEY).size()).isOne();
        assertThat(mappedResources.get(TOPIC_KEY).size()).isEqualTo(TWO_MAPPED_RESOURCES);
        assertThat(mappedResources.get(CATEGORY_KEY).size()).isEqualTo(ONE_MAPPED_RESOURCE);

        assertThat(encounterList.size()).isEqualTo(1);
    }

    @Test
    public void testValidEncounterWithFullDataWithStructuredConsultation() {
        when(consultationListMapper.mapToConsultation(any(RCMRMT030101UK04EhrExtract.class), any(Encounter.class)))
            .thenReturn(getList());
        when(consultationListMapper.mapToTopic(any(ListResource.class), any(RCMRMT030101UK04CompoundStatement.class)))
            .thenReturn(getList());
        when(consultationListMapper.mapToCategory(any(ListResource.class), any(RCMRMT030101UK04CompoundStatement.class)))
            .thenReturn(getList());
        var ehrExtract = unmarshallEhrExtractElement(FULL_VALID_STRUCTURED_ENCOUNTER_XML);

        Map<String, List<? extends DomainResource>> mappedResources = encounterMapper.mapEncounters(ehrExtract, patient, PRACTISE_CODE);

        assertThat(mappedResources.get(ENCOUNTER_KEY).size()).isOne();
        assertThat(mappedResources.get(CONSULTATION_KEY).size()).isOne();
        assertThat(mappedResources.get(TOPIC_KEY).size()).isOne();
        assertThat(mappedResources.get(CATEGORY_KEY).size()).isOne();

        var encounter = (Encounter) mappedResources.get(ENCOUNTER_KEY).get(0);

        assertEncounter(encounter, "2485BC20-90B4-11EC-B1E5-0800200C9A66", true,
            "2010-01-13T15:20:00+00:00", "2010-01-13T15:20:00+00:00");

        var consultation = (ListResource) mappedResources.get(CONSULTATION_KEY).get(0);
        assertThat(consultation.getEncounter().getReference()).isEqualTo(ENCOUNTER_ID);

        var topic = (ListResource) mappedResources.get(TOPIC_KEY).get(0);
        assertThat(topic.getEncounter().getReference()).isEqualTo(ENCOUNTER_ID);
        assertThat(consultation.getEntryFirstRep().getItem().getReference())
            .isEqualTo(ENCOUNTER_ID);

        var category = (ListResource) mappedResources.get(CATEGORY_KEY).get(0);
        assertThat(category.getEncounter().getReference()).isEqualTo(ENCOUNTER_ID);
        assertThat(topic.getEntryFirstRep().getItem().getReference())
            .isEqualTo(ENCOUNTER_ID);
    }

    @Test
    public void testValidEncounterWithLinkSetWithStructuredConsultation() {
        when(consultationListMapper.mapToConsultation(any(RCMRMT030101UK04EhrExtract.class), any(Encounter.class)))
            .thenReturn(getList());
        when(consultationListMapper.mapToTopic(any(ListResource.class), any(RCMRMT030101UK04CompoundStatement.class)))
            .thenReturn(getList());
        when(consultationListMapper.mapToTopic(any(ListResource.class), isNull()))
            .thenReturn(getList());
        when(consultationListMapper.mapToCategory(any(ListResource.class), any(RCMRMT030101UK04CompoundStatement.class)))
            .thenReturn(getList());
        var ehrExtract = unmarshallEhrExtractElement(FULL_VALID_STRUCTURED_ENCOUNTER_WITH_LINKSET_XML);

        Map<String, List<? extends DomainResource>> mappedResources = encounterMapper.mapEncounters(ehrExtract, patient, PRACTISE_CODE);

        assertThat(mappedResources.get(ENCOUNTER_KEY).size()).isOne();
        assertThat(mappedResources.get(CONSULTATION_KEY).size()).isOne();
        assertThat(mappedResources.get(TOPIC_KEY).size()).isEqualTo(TWO_MAPPED_RESOURCES);
        assertThat(mappedResources.get(CATEGORY_KEY).size()).isOne();

        var encounter = (Encounter) mappedResources.get(ENCOUNTER_KEY).get(0);

        assertEncounter(encounter, "2485BC20-90B4-11EC-B1E5-0800200C9A66", true, "2010-01-13T15:20:00+00:00", "2010-01-13T15:20:00+00:00");

        var consultation = (ListResource) mappedResources.get(CONSULTATION_KEY).get(0);
        assertThat(consultation.getEncounter().getReference()).isEqualTo(ENCOUNTER_ID);

        var linkSetTopic = (ListResource) mappedResources.get(TOPIC_KEY).get(0);
        assertThat(linkSetTopic.getEncounter().getReference()).isEqualTo(ENCOUNTER_ID);
        assertThat(consultation.getEntry().get(1).getItem().getReference())
            .isEqualTo(ENCOUNTER_ID_2);
        assertThat(linkSetTopic.getEntry().get(2).getItem().getReference())
            .isEqualTo(LINKSET_REFERENCE);

        var topic = (ListResource) mappedResources.get(TOPIC_KEY).get(1);
        assertThat(topic.getEncounter().getReference()).isEqualTo(ENCOUNTER_ID);
        assertThat(consultation.getEntry().get(0).getItem().getReference())
            .isEqualTo(ENCOUNTER_ID);

        var category = (ListResource) mappedResources.get(CATEGORY_KEY).get(0);
        assertThat(category.getEncounter().getReference()).isEqualTo(ENCOUNTER_ID);
        assertThat(topic.getEntryFirstRep().getItem().getReference())
            .isEqualTo(ENCOUNTER_ID);
    }

    @Test
    public void testValidEncounterWithFullDataWithFlatConsultation() {
        when(consultationListMapper.mapToConsultation(any(RCMRMT030101UK04EhrExtract.class), any(Encounter.class)))
            .thenReturn(getList());
        when(consultationListMapper.mapToTopic(any(ListResource.class), isNull()))
            .thenReturn(getList());
        var ehrExtract = unmarshallEhrExtractElement(FULL_VALID_FLAT_ENCOUNTER_XML);

        Map<String, List<? extends DomainResource>> mappedResources = encounterMapper.mapEncounters(ehrExtract, patient, PRACTISE_CODE);

        assertThat(mappedResources.get(ENCOUNTER_KEY).size()).isOne();
        assertThat(mappedResources.get(CONSULTATION_KEY).size()).isOne();
        assertThat(mappedResources.get(TOPIC_KEY).size()).isOne();
        assertThat(mappedResources.get(CATEGORY_KEY).size()).isZero();

        var encounter = (Encounter) mappedResources.get(ENCOUNTER_KEY).get(0);

        assertEncounter(encounter, "5EB5D070-8FE1-11EC-B1E5-0800200C9A66", true,
            "2010-01-13T15:20:00+00:00", "2010-01-13T15:20:00+00:00");

        var consultation = (ListResource) mappedResources.get(CONSULTATION_KEY).get(0);
        assertThat(consultation.getEncounter().getReference()).isEqualTo(ENCOUNTER_ID);

        var topic = (ListResource) mappedResources.get(TOPIC_KEY).get(0);
        assertThat(topic.getEncounter().getReference()).isEqualTo(ENCOUNTER_ID);
        assertThat(consultation.getEntryFirstRep().getItem().getReference()).isEqualTo(ENCOUNTER_ID);
    }

    @Test
    public void testValidEncounterWithNoOptionalDataWithFlatConsultation() {
        when(consultationListMapper.mapToConsultation(any(RCMRMT030101UK04EhrExtract.class), any(Encounter.class)))
            .thenReturn(getList());
        when(consultationListMapper.mapToTopic(any(ListResource.class), isNull()))
            .thenReturn(getList());
        var ehrExtract = unmarshallEhrExtractElement(NO_OPTIONAL_FLAT_ENCOUNTER_XML);

        Map<String, List<? extends DomainResource>> mappedResources = encounterMapper.mapEncounters(ehrExtract, patient, PRACTISE_CODE);

        assertThat(mappedResources.get(ENCOUNTER_KEY).size()).isOne();
        assertThat(mappedResources.get(CONSULTATION_KEY).size()).isOne();
        assertThat(mappedResources.get(TOPIC_KEY).size()).isOne();
        assertThat(mappedResources.get(CATEGORY_KEY).size()).isZero();

        var encounter = (Encounter) mappedResources.get(ENCOUNTER_KEY).get(0);

        assertEncounter(encounter, "5EB5D070-8FE1-11EC-B1E5-0800200C9A66", false, null, null);

        var consultation = (ListResource) mappedResources.get(CONSULTATION_KEY).get(0);
        assertThat(consultation.getEncounter().getReference()).isEqualTo(ENCOUNTER_ID);

        var topic = (ListResource) mappedResources.get(TOPIC_KEY).get(0);
        assertThat(topic.getEncounter().getReference()).isEqualTo(ENCOUNTER_ID);
        assertThat(consultation.getEntryFirstRep().getItem().getReference()).isEqualTo(ENCOUNTER_ID);
    }

    @Test
    public void testEncounterWithMappedResourcesWithStructuredConsultation() {
        when(consultationListMapper.mapToConsultation(any(RCMRMT030101UK04EhrExtract.class), any(Encounter.class)))
            .thenReturn(getList());
        when(consultationListMapper.mapToTopic(any(ListResource.class), any(RCMRMT030101UK04CompoundStatement.class)))
            .thenReturn(getList());
        when(consultationListMapper.mapToCategory(any(ListResource.class), any(RCMRMT030101UK04CompoundStatement.class)))
            .thenReturn(getList());
        var ehrExtract = unmarshallEhrExtractElement(FULL_VALID_STRUCTURED_ENCOUNTER_WITH_RESOURCES_XML);

        Map<String, List<? extends DomainResource>> mappedResources = encounterMapper.mapEncounters(ehrExtract, patient, PRACTISE_CODE);

        assertThat(mappedResources.get(ENCOUNTER_KEY).size()).isOne();
        assertThat(mappedResources.get(CONSULTATION_KEY).size()).isOne();
        assertThat(mappedResources.get(TOPIC_KEY).size()).isOne();
        assertThat(mappedResources.get(CATEGORY_KEY).size()).isOne();

        var encounter = (Encounter) mappedResources.get(ENCOUNTER_KEY).get(0);

        assertEncounter(encounter, "2485BC20-90B4-11EC-B1E5-0800200C9A66", true,
            "2010-01-13T15:20:00+00:00", "2010-01-13T15:20:00+00:00");

        var consultation = (ListResource) mappedResources.get(CONSULTATION_KEY).get(0);
        assertThat(consultation.getEncounter().getReference()).isEqualTo(ENCOUNTER_ID);

        var topic = (ListResource) mappedResources.get(TOPIC_KEY).get(0);
        assertThat(topic.getEncounter().getReference()).isEqualTo(ENCOUNTER_ID);
        assertThat(consultation.getEntryFirstRep().getItem().getReference())
            .isEqualTo(ENCOUNTER_ID);

        var category = (ListResource) mappedResources.get(CATEGORY_KEY).get(0);
        assertThat(category.getEncounter().getReference()).isEqualTo(ENCOUNTER_ID);
        assertThat(topic.getEntryFirstRep().getItem().getReference())
            .isEqualTo(ENCOUNTER_ID);

        verify(resourceReferenceUtil, atLeast(1)).extractChildReferencesFromCompoundStatement(any(), any());
    }

    @Test
    public void testEncounterWithMappedResourcesWithFlatConsultation() {
        when(consultationListMapper.mapToConsultation(any(RCMRMT030101UK04EhrExtract.class), any(Encounter.class)))
            .thenReturn(getList());
        when(consultationListMapper.mapToTopic(any(ListResource.class), isNull()))
            .thenReturn(getList());
        var ehrExtract = unmarshallEhrExtractElement(FULL_VALID_FLAT_ENCOUNTER_WITH_RESOURCES_XML);

        Map<String, List<? extends DomainResource>> mappedResources = encounterMapper.mapEncounters(ehrExtract, patient, PRACTISE_CODE);

        assertThat(mappedResources.get(ENCOUNTER_KEY).size()).isOne();
        assertThat(mappedResources.get(CONSULTATION_KEY).size()).isOne();
        assertThat(mappedResources.get(TOPIC_KEY).size()).isOne();
        assertThat(mappedResources.get(CATEGORY_KEY).size()).isZero();

        var encounter = (Encounter) mappedResources.get(ENCOUNTER_KEY).get(0);

        assertEncounter(encounter, "5EB5D070-8FE1-11EC-B1E5-0800200C9A66", true,
            "2010-01-13T15:20:00+00:00", "2010-01-13T15:20:00+00:00");

        var consultation = (ListResource) mappedResources.get(CONSULTATION_KEY).get(0);
        assertThat(consultation.getEncounter().getReference()).isEqualTo(ENCOUNTER_ID);

        var topic = (ListResource) mappedResources.get(TOPIC_KEY).get(0);
        assertThat(topic.getEncounter().getReference()).isEqualTo(ENCOUNTER_ID);
        assertThat(consultation.getEntryFirstRep().getItem().getReference()).isEqualTo(ENCOUNTER_ID);

        verify(resourceReferenceUtil, atLeast(1)).extractChildReferencesFromEhrComposition(any(), any());
    }

    @ParameterizedTest
    @MethodSource("encounterPeriodTestFiles")
    public void testEncounterPeriod(String inputXML, String startDate, String endDate) {
        when(consultationListMapper.mapToConsultation(any(RCMRMT030101UK04EhrExtract.class), any(Encounter.class)))
            .thenReturn(getList());
        when(consultationListMapper.mapToTopic(any(ListResource.class), isNull()))
            .thenReturn(getList());
        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtractElement(inputXML);

        Map<String, List<? extends DomainResource>> mappedResources = encounterMapper.mapEncounters(ehrExtract, patient, PRACTISE_CODE);

        var encounterList = mappedResources.get(ENCOUNTER_KEY);
        assertThat(encounterList.size()).isOne();

        var encounter = (Encounter) mappedResources.get(ENCOUNTER_KEY).get(0);
        assertEncounter(encounter, "5EB5D070-8FE1-11EC-B1E5-0800200C9A66", false, startDate, endDate);
    }

    private static Stream<Arguments> encounterPeriodTestFiles() {
        return Stream.of(
            Arguments.of(EFFECTIVE_CENTER_ENCOUNTER_PERIOD_XML, "2010-01-13T15:20:00+00:00", null),
            Arguments.of(EFFECTIVE_LOW_AND_HIGH_ENCOUNTER_PERIOD_XML, "2010-01-13T15:20:00+00:00", "2015-01-13T15:20:00+00:00"),
            Arguments.of(EFFECTIVE_LOW_ENCOUNTER_PERIOD_XML, "2010-01-13T15:20:00+00:00", null),
            Arguments.of(EFFECTIVE_HIGH_ENCOUNTER_PERIOD_XML, null, "2010-01-13T15:20:00+00:00"),
            Arguments.of(EFFECTIVE_CENTER_UNK_NULL_FLAVOR_ENCOUNTER_PERIOD_XML, null, null),
            Arguments.of(AVAILABILITY_TIME_ENCOUNTER_PERIOD_XML, "2010-01-13T15:20:00+00:00", null),
            Arguments.of(NO_ENCOUNTER_PERIOD_XML, "2012-06-15T14:20:00+00:00", null),
            Arguments.of(NULL_FLAVOR_EFFECTIVE_TIMES_XML, "2010-01-13T15:20:00+00:00", null)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidEhrCompositionTestFiles")
    public void testInvalidEhrCompositions(String inputXML) {
        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtractElement(inputXML);

        Map<String, List<? extends DomainResource>> mappedResources = encounterMapper.mapEncounters(ehrExtract, patient, PRACTISE_CODE);

        assertThat(mappedResources.get(ENCOUNTER_KEY).size()).isZero();
        assertThat(mappedResources.get(CONSULTATION_KEY).size()).isZero();
        assertThat(mappedResources.get(TOPIC_KEY).size()).isZero();
        assertThat(mappedResources.get(CATEGORY_KEY).size()).isZero();
    }

    private static Stream<Arguments> invalidEhrCompositionTestFiles() {
        return Stream.of(
            Arguments.of(INVALID_ENCOUNTER_CODE_1_XML),
            Arguments.of(INVALID_ENCOUNTER_CODE_2_XML),
            Arguments.of(SUPPRESSED_COMPOSITION_WITH_EHR_EMPTY_XML),
            Arguments.of(SUPPRESSED_COMPOSITION_WITH_REGISTRATION_STATEMENT_XML)
        );
    }

    private void assertEncounter(Encounter encounter, String id, Boolean hasLocation, String startDate, String endDate) {
        assertThat(encounter.getId()).isEqualTo(id);
        assertThat(encounter.getMeta().getProfile().get(0).getValue()).isEqualTo(ENCOUNTER_META_PROFILE);
        assertThat(encounter.getIdentifierFirstRep().getSystem()).isEqualTo(IDENTIFIER_SYSTEM);
        assertThat(encounter.getIdentifierFirstRep().getValue()).isEqualTo(id);
        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.FINISHED);
        assertThat(encounter.getSubject().getResource()).isEqualTo(patient);
        assertLocation(encounter, id, hasLocation);
        assertPeriod(encounter.getPeriod(), startDate, endDate);
    }

    private void assertLocation(Encounter encounter, String id, boolean hasLocation) {
        if (hasLocation) {
            assertThat(encounter.getLocationFirstRep().getLocation().getReference())
                .isEqualTo(LOCATION_PREFIX + id + LOCATION_SUFFIX);
        } else {
            assertThat(encounter.getLocation().size()).isZero();
        }
    }

    private void assertPeriod(Period period, String startDate, String endDate) {
        assertThat(period.getStartElement().getValueAsString()).isEqualTo(startDate);
        assertThat(period.getEndElement().getValueAsString()).isEqualTo(endDate);
    }

    private void setUpCodeableConceptMock() {
        var codeableConcept = new CodeableConcept();
        var coding = new Coding();
        coding.setDisplay(CODING_DISPLAY);
        codeableConcept.addCoding(coding);
        lenient().when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(codeableConcept);
        lenient().when(immunizationChecker.isImmunization(any())).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                String code = invocation.getArgument(0);
                return code.equals("1664081000000114");
            }
        });
    }

    private ListResource getList() {
        ListResource listResource = new ListResource();
        listResource.setEncounter(new Reference(ENCOUNTER_ID));
        listResource.getEntry().add(new ListResource.ListEntryComponent().setItem(new Reference(ENCOUNTER_ID)));
        listResource.getEntry().add(new ListResource.ListEntryComponent().setItem(new Reference(ENCOUNTER_ID_2)));
        return listResource;
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtractElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}
