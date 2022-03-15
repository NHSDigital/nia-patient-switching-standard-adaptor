package uk.nhs.adaptors.pss.translator.mapper;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToDateTimeType;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import lombok.SneakyThrows;

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
    public static final String NAMED_STATEMENT_REF_ID = "NAMED_STATEMENT_REF_ID";
    public static final String STATEMENT_REF_ID = "STATEMENT_REF_ID";
    public static final String STATEMENT_REF_ID_1 = "STATEMENT_REF_ID_1";

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

        assertThat(condition.getExtensionsByUrl(ACTUAL_PROBLEM_URL).size()).isEqualTo(0);
        assertThat(condition.getExtensionsByUrl(PROBLEM_SIGNIFICANCE_URL).size()).isEqualTo(1);
        assertThat(condition.getExtensionsByUrl(RELATED_CLINICAL_CONTENT_URL).size()).isEqualTo(0);

        assertThat(condition.getClinicalStatus().getDisplay()).isEqualTo("Active");
        assertThat(condition.getCode().getCodingFirstRep().hasDisplay()).isFalse();

        assertThat(condition.getSubject().getResource().getIdElement().getIdPart()).isEqualTo(PATIENT_ID);
        assertThat(condition.getAsserter().getReference()).isEqualTo(ASSERTER_ID_REFERENCE);
        assertThat(condition.getContext().hasReference()).isFalse();

        assertThat(condition.getOnsetDateTimeType()).isEqualTo(EHR_EXTRACT_AVAILABILITY_DATETIME);
        assertThat(condition.getAbatementDateTimeType()).isEqualTo(EHR_EXTRACT_AVAILABILITY_DATETIME);
        assertThat(condition.getAssertedDateElement().getValue()).isEqualTo(EHR_EXTRACT_AVAILABILITY_DATETIME.getValue());

        assertThat(condition.getNote().size()).isEqualTo(0);
    }

    @Test
    public void testConditionIsMappedCorrectlyWithNamedStatementRef() {
        when(dateTimeMapper.mapDateTime(any(String.class))).thenCallRealMethod();
        var codeableConcept = new CodeableConcept().addCoding(new Coding().setDisplay(CODING_DISPLAY));
        when(codeableConceptMapper.mapToCodeableConcept(any(), eq(false))).thenReturn(codeableConcept);

        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("linkset_valid_with_reference.xml");
        final List<Condition> conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);
        conditionMapper.addReferences(buildBundleWithNamedStatementObservation(), conditions, ehrExtract);

        assertThat(conditions.get(0).getCode().getCodingFirstRep().getDisplay()).isEqualTo(CODING_DISPLAY);
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
    public void testLinkSetWithNoDatesIsMappedCorrectly() {
        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("linkset_no_dates.xml");
        final List<Condition> conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);

        assertGeneratedComponentsAreCorrect(conditions.get(0));
        assertThat(conditions.get(0).getId()).isEqualTo(LINKSET_ID);

        assertThat(conditions.get(0).getClinicalStatus().getDisplay()).isEqualTo("Inactive");

        assertThat(conditions.get(0).getAbatementDateTimeType()).isNull();
        assertThat(conditions.get(0).getAssertedDateElement().getValue()).isEqualTo(EHR_EXTRACT_AVAILABILITY_DATETIME.getValue());
    }

    private void assertActualProblemExtension(Condition condition) {
        var extension = condition.getExtensionsByUrl(ACTUAL_PROBLEM_URL).get(0);
        assertThat(extension.getValue() instanceof Reference).isTrue();
        assertThat(((Reference) extension.getValue()).getResource() instanceof Observation).isTrue();
        assertThat(((Observation) ((Reference) extension.getValue()).getResource()).getId()).isEqualTo(NAMED_STATEMENT_REF_ID);
    }

    private void assertRelatedClinicalContentExtension(Condition condition) {
        var extensions = condition.getExtensionsByUrl(RELATED_CLINICAL_CONTENT_URL);
        assertThat(extensions.size()).isEqualTo(2);
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
