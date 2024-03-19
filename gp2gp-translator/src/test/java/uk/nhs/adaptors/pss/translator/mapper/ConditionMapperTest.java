package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToDateTimeType;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.CD;
import org.hl7.v3.II;
import org.hl7.v3.RCMRMT030101UK04Authorise;
import org.hl7.v3.RCMRMT030101UK04Component2;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04MedicationStatement;
import org.hl7.v3.RCMRMT030101UK04Prescribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils;
import uk.nhs.adaptors.pss.translator.util.DegradedCodeableConcepts;
import static uk.nhs.adaptors.common.util.CodeableConceptUtils.createCodeableConcept;

@ExtendWith(MockitoExtension.class)
public class ConditionMapperTest {

    private static final String CONDITION_RESOURCES_BASE = "xml/Condition/";
    private static final String PATIENT_ID = "PATIENT_ID";
    private static final String PRACTISE_CODE = "TESTPRACTISECODE";
    private static final String ENCOUNTER_ID = "EHR_COMPOSITION_ENCOUNTER_ID";
    private static final String ASSERTER_ID_REFERENCE = "Practitioner/ASSERTER_ID";
    private static final String LINKSET_ID = "LINKSET_ID";
    private static final String CODING_DISPLAY = "THIS IS A TEST";
    private static final DateTimeType EHR_EXTRACT_AVAILABILITY_DATETIME = parseToDateTimeType("20101209114846.00");

    private static final String ACTUAL_PROBLEM_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect"
        + "-ActualProblem-1";
    private static final String PROBLEM_SIGNIFICANCE_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect"
        + "-ProblemSignificance-1";
    private static final String RELATED_CLINICAL_CONTENT_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect"
        + "-RelatedClinicalContent-1";
    private static final String MEDICATION_STATEMENT_PLAN_ID = "PLAN_REF_ID";
    private static final String AUTHORISE_ID = "AUTHORISE_ID";
    private static final String MEDICATION_STATEMENT_ORDER_ID = "ORDER_REF_ID";
    private static final String PRESCRIBE_ID = "PRESCRIBE_ID";
    public static final String NAMED_STATEMENT_REF_ID = "NAMED_STATEMENT_REF_ID";
    public static final String STATEMENT_REF_ID = "STATEMENT_REF_ID";
    public static final String STATEMENT_REF_ID_1 = "STATEMENT_REF_ID_1";
    public static final int EXPECTED_NUMBER_OF_EXTENSIONS = 4;

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @Mock
    private DateTimeMapper dateTimeMapper;

    @InjectMocks
    private ConditionMapper conditionMapper;

    private Patient patient;

    @BeforeEach
    public void setUp() {
        patient = (Patient) new Patient().setId(PATIENT_ID);
    }

    @Test
    public void testConditionIsMappedCorrectlyNoReferences() {
        when(dateTimeMapper.mapDateTime(any())).thenReturn(EHR_EXTRACT_AVAILABILITY_DATETIME);

        final List<Encounter> emptyEncounterList = List.of();
        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("linkset_valid.xml");
        final List<Condition> conditions = conditionMapper.mapResources(ehrExtract, patient, emptyEncounterList, PRACTISE_CODE);

        assertThat(conditions).isNotEmpty();

        final Condition condition = conditions.get(0);

        assertGeneratedComponentsAreCorrect(condition);
        assertThat(condition.getId()).isEqualTo(LINKSET_ID);

        assertThat(condition.getExtensionsByUrl(ACTUAL_PROBLEM_URL)).isEmpty();
        assertThat(condition.getExtensionsByUrl(PROBLEM_SIGNIFICANCE_URL)).hasSize(1);
        assertThat(condition.getExtensionsByUrl(RELATED_CLINICAL_CONTENT_URL)).isEmpty();

        assertThat(condition.getClinicalStatus().getDisplay()).isEqualTo("Active");
        assertThat(condition.getCode().getCodingFirstRep().hasDisplay()).isFalse();

        assertThat(condition.getSubject().getResource().getIdElement().getIdPart()).isEqualTo(PATIENT_ID);
        assertThat(condition.getAsserter().getReference()).isEqualTo(ASSERTER_ID_REFERENCE);
        assertThat(condition.getContext().hasReference()).isFalse();

        assertThat(condition.getOnsetDateTimeType()).isEqualTo(EHR_EXTRACT_AVAILABILITY_DATETIME);
        assertThat(condition.getAbatementDateTimeType()).isEqualTo(EHR_EXTRACT_AVAILABILITY_DATETIME);
        assertThat(condition.getAssertedDateElement().getValue()).isNull();

        assertThat(condition.getNote()).isEmpty();
    }

    @Test
    public void testConditionIsMappedCorrectlyWithNamedStatementRef() {
        when(dateTimeMapper.mapDateTime(any(String.class))).thenCallRealMethod();
        var codeableConcept = new CodeableConcept().addCoding(new Coding().setDisplay(CODING_DISPLAY));
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(codeableConcept);

        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("linkset_valid_with_reference.xml");
        final List<Condition> conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);
        conditionMapper.addReferences(buildBundleWithNamedStatementObservation(), conditions, ehrExtract);

        assertThat(conditions.get(0).getCode().getCodingFirstRep()).isEqualTo(DegradedCodeableConcepts.DEGRADED_OTHER);
        assertThat(conditions.get(0).getCode().getCoding().get(1).getDisplay()).isEqualTo(CODING_DISPLAY);
    }

    @Test
    public void testConditionIsMappedCorrectlyWithActualProblemReference() {
        when(dateTimeMapper.mapDateTime(any())).thenReturn(EHR_EXTRACT_AVAILABILITY_DATETIME);

        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("linkset_valid.xml");
        final List<Condition> conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);

        conditionMapper.addReferences(buildBundleWithNamedStatementObservation(), conditions, ehrExtract);

        assertThat(conditions).isNotEmpty();
        assertThat(conditions.get(0).getExtensionsByUrl(ACTUAL_PROBLEM_URL)).isNotEmpty();
        assertActualProblemExtension(conditions.get(0));
    }

    @Test
    public void testConditionIsMappedCorrectlyWithRelatedClinicalContentReference() {
        when(dateTimeMapper.mapDateTime(any())).thenReturn(EHR_EXTRACT_AVAILABILITY_DATETIME);

        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("linkset_valid.xml");
        final List<Condition> conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);

        conditionMapper.addReferences(buildBundleWithStatementRefObservations(), conditions, ehrExtract);

        assertThat(conditions).isNotEmpty();
        assertThat(conditions.get(0).getExtensionsByUrl(RELATED_CLINICAL_CONTENT_URL)).isNotEmpty();
        assertRelatedClinicalContentExtension(conditions.get(0));
    }

    @Test
    public void testConditionIsMappedCorrectlyWithContext() {
        when(dateTimeMapper.mapDateTime(any())).thenReturn(EHR_EXTRACT_AVAILABILITY_DATETIME);

        final List<Encounter> encounters = List.of((Encounter) new Encounter().setId(ENCOUNTER_ID));

        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("linkset_valid.xml");
        final List<Condition> conditions = conditionMapper.mapResources(ehrExtract, patient, encounters, PRACTISE_CODE);

        assertThat(conditions).isNotEmpty();
        assertThat(conditions.get(0).getContext().getResource().getIdElement().getValue()).isEqualTo(ENCOUNTER_ID);
    }

    @Test
    public void testLinkSetWithNoDatesIsMappedWithNullOnsetDateTime() {
        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("linkset_no_dates.xml");
        final List<Condition> conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);

        assertGeneratedComponentsAreCorrect(conditions.get(0));
        assertThat(conditions.get(0).getId()).isEqualTo(LINKSET_ID);

        assertThat(conditions.get(0).getClinicalStatus().getDisplay()).isEqualTo("Inactive");

        assertThat(conditions.get(0).getAbatementDateTimeType()).isNull();
        assertThat(conditions.get(0).getAssertedDateElement().getValue()).isNull();
    }

    @Test
    public void testLinkSetWithEffectiveTimeLowNullFlavorUnkIsMappedWithNullOnsetDateTime() {
        when(dateTimeMapper.mapDateTime(any())).thenReturn(EHR_EXTRACT_AVAILABILITY_DATETIME);
        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("linkset_with_null_flavor_unk.xml");
        final List<Condition> conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);

        assertGeneratedComponentsAreCorrect(conditions.get(0));
        assertThat(conditions.get(0).getId()).isEqualTo(LINKSET_ID);

        assertNull(conditions.get(0).getOnsetDateTimeType());
    }

    @Test
    public void testLinkSetWithEffectiveTimeCenterNullFlavorUnkIsMappedCorrectly() {
        //when(dateTimeMapper.mapDateTime(any())).thenReturn(EHR_EXTRACT_AVAILABILITY_DATETIME);
        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("linkset_with_center_null_flavor_unk.xml");
        final List<Condition> conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);

        assertGeneratedComponentsAreCorrect(conditions.get(0));
        assertThat(conditions.get(0).getId()).isEqualTo(LINKSET_ID);

        assertNull(conditions.get(0).getOnsetDateTimeType());
    }

    @Test
    public void testConditionWithMedicationRequestsIsMappedCorrectly() {
        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("linkset_medication_refs.xml");

        MockedStatic<MedicationMapperUtils> mockedMedicationMapperUtils = Mockito.mockStatic(MedicationMapperUtils.class);

        // spotbugs doesn't allow try with resources due to de-referenced null check
        try {
            mockedMedicationMapperUtils.when(() -> MedicationMapperUtils.getMedicationStatements(ehrExtract))
                .thenReturn(getMedicationStatements());

            final List<Condition> conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);

            assertThat(conditions.size()).isOne();

            var bundle = new Bundle();
            bundle.addEntry(new BundleEntryComponent().setResource(conditions.get(0)));
            addMedicationRequestsToBundle(bundle);

            conditionMapper.addReferences(bundle, conditions, ehrExtract);

            var extensions = conditions.get(0).getExtension();

            assertThat(extensions).hasSize(EXPECTED_NUMBER_OF_EXTENSIONS);
            var relatedClinicalContentExtensions = extensions.stream()
                .filter(extension -> extension.getUrl().equals(RELATED_CLINICAL_CONTENT_URL))
                .toList();

            assertThat(relatedClinicalContentExtensions).hasSize(2);

            List<String> clinicalContextReferences = relatedClinicalContentExtensions.stream()
                .map(Extension::getValue)
                .map(Reference.class::cast)
                .map(reference -> reference.getReferenceElement().getValue())
                .toList();

            assertThat(clinicalContextReferences).contains(AUTHORISE_ID);
            assertThat(clinicalContextReferences).contains(PRESCRIBE_ID);
        } finally {
            mockedMedicationMapperUtils.close();
        }

    }

    @Test
    public void testConditionWithPertinentAnnotationTextIsMappedCorrectly() {
        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("linkset_pertinentInformation.xml");

        when(dateTimeMapper.mapDateTime(any())).thenReturn(EHR_EXTRACT_AVAILABILITY_DATETIME);
        MockedStatic<MedicationMapperUtils> mockedMedicationMapperUtils = Mockito.mockStatic(MedicationMapperUtils.class);

        // spotbugs doesn't allow try with resources due to de-referenced null check
        try {
            mockedMedicationMapperUtils.when(() -> MedicationMapperUtils.getMedicationStatements(ehrExtract))
                .thenReturn(getMedicationStatements());

            final List<Condition> conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);

            assertThat(conditions.size()).isOne();

            var bundle = new Bundle();
            bundle.addEntry(new BundleEntryComponent().setResource(conditions.get(0)));
            addMedicationRequestsToBundle(bundle);

            conditionMapper.addReferences(bundle, conditions, ehrExtract);

            var noteText = conditions.get(0).getNote().get(1).getText();
            assertEquals("Problem severity: Minor (New Episode). H/O: injury to little finger left hand poss glass in wound therefore refered to A+E",
                         noteText);

            var extensions = conditions.get(0).getExtension();

            assertThat(extensions).hasSize(EXPECTED_NUMBER_OF_EXTENSIONS);
            var relatedClinicalContentExtensions = extensions.stream()
                .filter(extension -> extension.getUrl().equals(RELATED_CLINICAL_CONTENT_URL))
                .toList();

            assertThat(relatedClinicalContentExtensions).hasSize(2);

            List<String> clinicalContextReferences = relatedClinicalContentExtensions.stream()
                .map(Extension::getValue)
                .map(Reference.class::cast)
                .map(reference -> reference.getReferenceElement().getValue())
                .toList();

            assertThat(clinicalContextReferences).contains(AUTHORISE_ID);
            assertThat(clinicalContextReferences).contains(PRESCRIBE_ID);
        } finally {
            mockedMedicationMapperUtils.close();
        }

    }

    @Test
    public void compareCodeableConcepts() {
        CD c1 = new CD();
        c1.setCode("Code1");
        c1.setCodeSystem("CodeSystem1");
        c1.setDisplayName("DisplayName1");

        CD c2 = new CD();
        c2.setCode("Code1");
        c2.setCodeSystem("CodeSystem1");
        c2.setDisplayName("DisplayName1");

        assertTrue(conditionMapper.compareCodeableConcepts(c1, c2));
    }

    @Test
    public void mapConditionWithoutSnomedCodeInCoding() {
        var codeableConcept = new CodeableConcept().addCoding(new Coding().setDisplay(CODING_DISPLAY));
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(codeableConcept);
        when(dateTimeMapper.mapDateTime(any(String.class))).thenCallRealMethod();

        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("linkset_valid_with_reference.xml");
        final List<Condition> conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);
        conditionMapper.addReferences(buildBundleWithNamedStatementObservation(), conditions, ehrExtract);

        assertThat(conditions).isNotEmpty();
        assertThat(conditions.get(0).getCode().getCodingFirstRep())
            .isEqualTo(DegradedCodeableConcepts.DEGRADED_OTHER);
        assertThat(conditions.get(0).getCode().getCoding().get(1).getDisplay())
            .isEqualTo(CODING_DISPLAY);
    }

    @Test
    public void mapConditionWithSnomedCodeInCoding() {

        var codeableConcept = createCodeableConcept("123456", "http://snomed.info/sct", "Display");
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(codeableConcept);
        when(dateTimeMapper.mapDateTime(any(String.class))).thenCallRealMethod();

        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("linkset_valid_with_reference.xml");
        final List<Condition> conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);
        conditionMapper.addReferences(buildBundleWithNamedStatementObservation(), conditions, ehrExtract);

        assertThat(conditions).isNotEmpty();
        assertEquals(codeableConcept, conditions.get(0).getCode());
    }

    private void addMedicationRequestsToBundle(Bundle bundle) {
        var planMedicationRequest = new MedicationRequest().setId(AUTHORISE_ID);
        var orderMedicationRequest = new MedicationRequest().setId(PRESCRIBE_ID);

        bundle.addEntry(new BundleEntryComponent().setResource(planMedicationRequest));
        bundle.addEntry(new BundleEntryComponent().setResource(orderMedicationRequest));
    }

    private List<RCMRMT030101UK04MedicationStatement> getMedicationStatements() {

        var planMedicationStatement = new RCMRMT030101UK04MedicationStatement();
        planMedicationStatement.setId(createIdWithRoot(MEDICATION_STATEMENT_PLAN_ID));
        planMedicationStatement.getMoodCode().add("INT");

        var authorise = new RCMRMT030101UK04Authorise();
        authorise.setId(createIdWithRoot(AUTHORISE_ID));

        var planComponent = new RCMRMT030101UK04Component2();
        planComponent.setEhrSupplyAuthorise(authorise);

        planMedicationStatement.getComponent().add(planComponent);

        var orderMedicationStatement = new RCMRMT030101UK04MedicationStatement();
        orderMedicationStatement.setId(createIdWithRoot(MEDICATION_STATEMENT_ORDER_ID));
        orderMedicationStatement.getMoodCode().add("ORD");

        var prescribe = new RCMRMT030101UK04Prescribe();
        prescribe.setId(createIdWithRoot(PRESCRIBE_ID));

        var orderComponent = new RCMRMT030101UK04Component2();
        orderComponent.setEhrSupplyPrescribe(prescribe);

        orderMedicationStatement.getComponent().add(orderComponent);

        return List.of(planMedicationStatement, orderMedicationStatement);
    }

    private II createIdWithRoot(String rootValue) {
        var id = new II();
        id.setRoot(rootValue);

        return id;
    }

    private void assertActualProblemExtension(Condition condition) {
        var extension = condition.getExtensionsByUrl(ACTUAL_PROBLEM_URL).get(0);
        assertThat(extension.getValue()).isInstanceOf(Reference.class);
        assertThat(((Reference) extension.getValue()).getResource()).isInstanceOf(Observation.class);
        assertThat(((Observation) ((Reference) extension.getValue()).getResource()).getId()).isEqualTo(NAMED_STATEMENT_REF_ID);
    }

    private void assertRelatedClinicalContentExtension(Condition condition) {
        var extensions = condition.getExtensionsByUrl(RELATED_CLINICAL_CONTENT_URL);
        assertThat(extensions).hasSize(2);
        assertThat(((Reference) extensions.get(0).getValue()).getResource().getIdElement().getValue()).isEqualTo(STATEMENT_REF_ID);
        assertThat(((Reference) extensions.get(1).getValue()).getResource().getIdElement().getValue()).isEqualTo(STATEMENT_REF_ID_1);
    }

    private void assertGeneratedComponentsAreCorrect(Condition condition) {
        assertThat(condition.getMeta().getProfile().get(0)).isNotNull();
        assertThat(condition.getIdentifierFirstRep().getValue()).isEqualTo(LINKSET_ID);
        assertThat(condition.getCategoryFirstRep().getCodingFirstRep().getDisplay()).isEqualTo("Problem List Item");
    }

    private Bundle buildBundleWithNamedStatementObservation() {
        return new Bundle()
            .addEntry(new BundleEntryComponent()
                .setResource(new Observation().setId(NAMED_STATEMENT_REF_ID)));
    }

    private Bundle buildBundleWithStatementRefObservations() {
        return new Bundle()
            .addEntry(new BundleEntryComponent()
                .setResource(new Observation().setId(STATEMENT_REF_ID)))
            .addEntry(new BundleEntryComponent()
                .setResource(new Observation().setId(STATEMENT_REF_ID_1)));
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtract(String filename) {
        return unmarshallFile(getFile("classpath:" + CONDITION_RESOURCES_BASE + filename), RCMRMT030101UK04EhrExtract.class);
    }
}
