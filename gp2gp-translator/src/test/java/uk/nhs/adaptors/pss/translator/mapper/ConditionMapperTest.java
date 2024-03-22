package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToDateTimeType;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.common.util.CodeableConceptUtils;
import uk.nhs.adaptors.pss.translator.util.DegradedCodeableConcepts;

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
    private static final String AUTHORISE_ID = "AUTHORISE_ID";
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
    public void testMergeObservationStatementsIfRequiredWhenNoObservationStatementToMergeWith() {
        final var expectedObservationStatement = new RCMRMT030101UK04ObservationStatement();

        final var actualObservationStatement = conditionMapper.mergeObservationStatementsIfRequired(
                expectedObservationStatement,
                Optional.empty());

        assertThat(actualObservationStatement).isEqualTo(expectedObservationStatement);
    }

    @Test
    public void testMergeObservationStatementsIfRequiredWithTwoObservationStatements() {
        final var referencedObservationStatement = buildReferencedObservationStatement();
        final var matchedObservationStatement = Optional.of(buildObservationStatementToBeMatched());
        final var expectedText =
                "Problem severity: Minor (New Episode). H/O: injury to little finger left hand poss glass in wound therefore referred to A+E";

        final var observationStatement = conditionMapper.mergeObservationStatementsIfRequired(
                referencedObservationStatement,
                matchedObservationStatement);
        final var actualText = observationStatement.getPertinentInformation().get(0).getPertinentAnnotation().getText();

        assertThat(actualText).isEqualTo(expectedText);
    }

    @Test
    public void testConditionIsMappedCorrectlyNoReferences() {
        when(dateTimeMapper.mapDateTime(any()))
                .thenReturn(EHR_EXTRACT_AVAILABILITY_DATETIME);

        final var ehrExtract = unmarshallEhrExtract("linkset_valid.xml");
        final var condition = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE).get(0);

        assertAll(
                () -> assertGeneratedComponentsAreCorrect(condition),
                () -> assertThat(condition.getId()).isEqualTo(LINKSET_ID),
                () -> assertThat(condition.getExtensionsByUrl(ACTUAL_PROBLEM_URL)).isEmpty(),
                () -> assertThat(condition.getExtensionsByUrl(PROBLEM_SIGNIFICANCE_URL)).hasSize(1),
                () -> assertThat(condition.getExtensionsByUrl(RELATED_CLINICAL_CONTENT_URL)).isEmpty(),
                () -> assertThat(condition.getClinicalStatus().getDisplay()).isEqualTo("Active"),
                () -> assertThat(condition.getCode().getCodingFirstRep().hasDisplay()).isFalse(),
                () -> assertThat(condition.getSubject().getResource().getIdElement().getIdPart()).isEqualTo(PATIENT_ID),
                () -> assertThat(condition.getAsserter().getReference()).isEqualTo(ASSERTER_ID_REFERENCE),
                () -> assertThat(condition.getContext().hasReference()).isFalse(),
                () -> assertThat(condition.getOnsetDateTimeType()).isEqualTo(EHR_EXTRACT_AVAILABILITY_DATETIME),
                () -> assertThat(condition.getAbatementDateTimeType()).isEqualTo(EHR_EXTRACT_AVAILABILITY_DATETIME),
                () -> assertThat(condition.getAssertedDateElement().getValue()).isNull(),
                () -> assertThat(condition.getNote()).isEmpty()
        );
    }

    @Test
    public void testConditionIsMappedCorrectlyWithNamedStatementRef() {
        final var codeableConcept = CodeableConceptUtils.createCodeableConcept(null, null, CODING_DISPLAY);

        when(dateTimeMapper.mapDateTime(any(String.class))).thenCallRealMethod();
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(codeableConcept);

        final var ehrExtract = unmarshallEhrExtract("linkset_valid_with_reference.xml");
        final var conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);
        conditionMapper.addReferences(buildBundleWithNamedStatementObservation(), conditions, ehrExtract);
        final var actualDisplay = conditions.get(0).getCode().getCoding().get(1).getDisplay();

        assertThat(actualDisplay).isEqualTo(CODING_DISPLAY);
    }

    @Test
    public void testConditionIsMappedCorrectlyWithActualProblemReference() {
        when(dateTimeMapper.mapDateTime(any())).thenReturn(EHR_EXTRACT_AVAILABILITY_DATETIME);

        final var ehrExtract = unmarshallEhrExtract("linkset_valid.xml");
        final var conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);
        conditionMapper.addReferences(buildBundleWithNamedStatementObservation(), conditions, ehrExtract);
        final var condition = conditions.get(0);
        final var actualExtension = condition.getExtensionsByUrl(ACTUAL_PROBLEM_URL).get(0);

        assertAll(
                () -> assertThat(condition.getExtensionsByUrl(ACTUAL_PROBLEM_URL))
                        .isNotEmpty(),
                () -> assertThat(actualExtension.getValue())
                        .isInstanceOf(Reference.class),
                () -> assertThat(((Reference) actualExtension.getValue()).getResource())
                        .isInstanceOf(Observation.class),
                () -> assertThat(((Observation) ((Reference) actualExtension.getValue()).getResource()).getId())
                        .isEqualTo(NAMED_STATEMENT_REF_ID)
        );
    }

    @Test
    public void testConditionIsMappedCorrectlyWithRelatedClinicalContentReference() {
        when(dateTimeMapper.mapDateTime(any()))
                .thenReturn(EHR_EXTRACT_AVAILABILITY_DATETIME);

        final var ehrExtract = unmarshallEhrExtract("linkset_valid.xml");
        final var conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);

        conditionMapper.addReferences(buildBundleWithStatementRefObservations(), conditions, ehrExtract);
        final var condition = conditions.get(0);
        final var actualExtensions = condition.getExtensionsByUrl(RELATED_CLINICAL_CONTENT_URL);
        final var actualFirstExtensionReference = (Reference) actualExtensions.get(0).getValue();
        final var actualSecondExtensionReference = (Reference) actualExtensions.get(1).getValue();

        assertAll(
                () -> assertThat(condition.getExtensionsByUrl(RELATED_CLINICAL_CONTENT_URL))
                        .isNotEmpty(),
                () -> assertThat(actualExtensions)
                        .hasSize(2),
                () -> assertThat(actualFirstExtensionReference.getResource().getIdElement().getValue())
                        .isEqualTo(STATEMENT_REF_ID),
                () -> assertThat(actualSecondExtensionReference.getResource().getIdElement().getValue())
                        .isEqualTo(STATEMENT_REF_ID_1)
        );
    }

    @Test
    public void testConditionIsMappedCorrectlyWithContext() {
        when(dateTimeMapper.mapDateTime(any())).thenReturn(EHR_EXTRACT_AVAILABILITY_DATETIME);

        final var encounters = List.of((Encounter) new Encounter().setId(ENCOUNTER_ID));

        final var ehrExtract = unmarshallEhrExtract("linkset_valid.xml");
        final var conditions = conditionMapper.mapResources(ehrExtract, patient, encounters, PRACTISE_CODE);
        final var actualConditionIdValue = conditions.get(0).getContext().getResource().getIdElement().getValue();

        assertThat(actualConditionIdValue).isEqualTo(ENCOUNTER_ID);
    }

    @Test
    public void testLinkSetWithNoDatesIsMappedWithNullOnsetDateTime() {
        final var ehrExtract = unmarshallEhrExtract("linkset_no_dates.xml");

        final var conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);
        final var condition = conditions.get(0);

        assertAll(
                () -> assertGeneratedComponentsAreCorrect(condition),
                () -> assertThat(condition.getId()).isEqualTo(LINKSET_ID),
                () -> assertThat(condition.getClinicalStatus().getDisplay()).isEqualTo("Inactive"),
                () -> assertThat(condition.getAbatementDateTimeType()).isNull(),
                () -> assertThat(condition.getAssertedDateElement().getValue()).isNull()
        );
    }

    @Test
    public void testLinkSetWithEffectiveTimeLowNullFlavorUnkIsMappedWithNullOnsetDateTime() {
        when(dateTimeMapper.mapDateTime(any())).thenReturn(EHR_EXTRACT_AVAILABILITY_DATETIME);

        final var ehrExtract = unmarshallEhrExtract("linkset_with_null_flavor_unk.xml");

        final var conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);
        final var condition = conditions.get(0);

        assertAll(
                () -> assertGeneratedComponentsAreCorrect(condition),
                () -> assertThat(condition.getId()).isEqualTo(LINKSET_ID),
                () -> assertNull(condition.getOnsetDateTimeType())
        );
    }

    @Test
    public void testLinkSetWithEffectiveTimeCenterNullFlavorUnkIsMappedCorrectly() {
        final var ehrExtract = unmarshallEhrExtract("linkset_with_center_null_flavor_unk.xml");

        final var conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);
        final var condition = conditions.get(0);

        assertAll(
                () -> assertGeneratedComponentsAreCorrect(condition),
                () -> assertThat(condition.getId()).isEqualTo(LINKSET_ID),
                () -> assertThat(condition.getOnsetDateTimeType()).isNull()
        );
    }

    @Test
    public void testConditionWithMedicationRequestsIsMappedCorrectly() {
        final var ehrExtract = unmarshallEhrExtract("linkset_medication_refs.xml");

        final var conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);
        final var condition = conditions.get(0);
        final var extensions = condition.getExtension();
        addMedicationReferences(conditions, condition, ehrExtract);
        final var relatedClinicalContentExtensions = extensions.stream()
                .filter(extension -> extension.getUrl().equals(RELATED_CLINICAL_CONTENT_URL))
                .toList();
        final var actualClinicalContextReferences = relatedClinicalContentExtensions.stream()
                .map(Extension::getValue)
                .map(Reference.class::cast)
                .map(reference -> reference.getReferenceElement().getValue())
                .toList();

        assertAll(
                () -> assertThat(extensions).hasSize(EXPECTED_NUMBER_OF_EXTENSIONS),
                () -> assertThat(relatedClinicalContentExtensions).hasSize(2),
                () -> assertThat(actualClinicalContextReferences).contains(AUTHORISE_ID),
                () -> assertThat(actualClinicalContextReferences).contains(PRESCRIBE_ID)
        );
    }

    @Test
    public void testObservationStatementByCodeableConceptCode() {
        final var ehrExtract = unmarshallEhrExtract("linkset_pertinentInformation.xml");
        final var referencedObservationStatement = buildReferencedObservationStatement();
        final var expectedCode = referencedObservationStatement.getCode();

        final var matchedObservationStatement = conditionMapper.getObservationStatementByCodeableConceptCode(
                ehrExtract,
                referencedObservationStatement);
        assert matchedObservationStatement.isPresent();

        assertAll(
                () -> assertThat(expectedCode.getCodeSystem())
                        .isEqualTo(matchedObservationStatement.get().getCode().getCodeSystem()),
                () -> assertThat(expectedCode.getDisplayName())
                        .isEqualTo(matchedObservationStatement.get().getCode().getDisplayName()),
                () -> assertThat(expectedCode.getTranslation().get(0).getCode())
                        .isEqualTo(matchedObservationStatement.get().getCode().getTranslation().get(0).getCode()),
                () -> assertThat(expectedCode.getTranslation().get(0).getDisplayName())
                        .isEqualTo(matchedObservationStatement.get().getCode().getTranslation().get(0).getDisplayName())
        );

    }

    @Test
    public void test3ObservationStatementsByCodeableConceptCodeAndEnsureCorrectMatchedObservationStatement() {
        final var ehrExtract =
                unmarshallEhrExtract("linkset_pertinentInformation_with_3_observationStatements.xml");
        final var referencedObservationStatement = buildReferencedObservationStatement();
        final var expectedCode = referencedObservationStatement.getCode();

        var matchedObservationStatement = conditionMapper.getObservationStatementByCodeableConceptCode(
                ehrExtract,
                referencedObservationStatement);
        assert matchedObservationStatement.isPresent();

        assertAll(
                () -> assertThat(expectedCode.getCodeSystem())
                        .isEqualTo(matchedObservationStatement.get().getCode().getCodeSystem()),
                () -> assertThat(expectedCode.getDisplayName())
                        .isEqualTo(matchedObservationStatement.get().getCode().getDisplayName()),
                () -> assertThat(expectedCode.getTranslation().get(0).getCode())
                        .isEqualTo(matchedObservationStatement.get().getCode().getTranslation().get(0).getCode()),
                () -> assertThat(expectedCode.getTranslation().get(0).getDisplayName())
                        .isEqualTo(matchedObservationStatement.get().getCode().getTranslation().get(0).getDisplayName())
        );
    }

    @Test
    public void test2DifferentObservationStatementsByCodeableConceptCodeAndExpectNoMatchedObservationStatementFound() {
        final var ehrExtract =
                unmarshallEhrExtract("linkset_pertinentInformation_with_different_observation_statements.xml");

        final var referencedObservationStatement = buildReferencedObservationStatement();

        final var matchedObservationStatement = conditionMapper.getObservationStatementByCodeableConceptCode(
                ehrExtract,
                referencedObservationStatement);

        assertThat(matchedObservationStatement).isEmpty();
    }

    @Test
    public void mapConditionWithoutSnomedCodeInCoding() {
        final var expectedCodeableConcept = CodeableConceptUtils.createCodeableConcept(
                null,
                null,
                CODING_DISPLAY);

        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(expectedCodeableConcept);
        when(dateTimeMapper.mapDateTime(any(String.class))).thenCallRealMethod();

        final var ehrExtract = unmarshallEhrExtract("linkset_valid_with_reference.xml");
        final var conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);
        final var condition = conditions.get(0);
        conditionMapper.addReferences(buildBundleWithNamedStatementObservation(), conditions, ehrExtract);

        assertAll(
                () -> assertThat(condition.getCode().getCoding().get(0))
                        .isEqualTo(DegradedCodeableConcepts.DEGRADED_OTHER),
                () -> assertThat(condition.getCode().getCoding().get(1).getDisplay())
                        .isEqualTo(CODING_DISPLAY)
        );
    }

    @Test
    public void mapConditionWithSnomedCodeInCoding() {

        final var expectedCodeableConcept = CodeableConceptUtils.createCodeableConcept(
                "123456",
                "http://snomed.info/sct",
                "Display");

        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(expectedCodeableConcept);
        when(dateTimeMapper.mapDateTime(any(String.class))).thenCallRealMethod();

        final var ehrExtract = unmarshallEhrExtract("linkset_valid_with_reference.xml");
        final var conditions = conditionMapper.mapResources(ehrExtract, patient, List.of(), PRACTISE_CODE);
        conditionMapper.addReferences(buildBundleWithNamedStatementObservation(), conditions, ehrExtract);

        assertEquals(expectedCodeableConcept, conditions.get(0).getCode());
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

    @NotNull
    private static CD buildCd(String code, String system, String displayName) {
        CD cd = new CD();
        cd.setCode(code);
        cd.setCodeSystem(system);
        cd.setDisplayName(displayName);
        return cd;
    }

    @NotNull
    private static CD buildMatchableObservationStatementCode() {
        CD code = buildCd("14J..", "2.16.840.1.113883.2.1.3.2.4.14", "H/O: injury");
        CD translationCD1 = buildCd("161586000", "2.16.840.1.113883.2.1.3.2.4.15", "H/O: injury");
        CD translationCD2 = buildCd("14J..00", "2.16.840.1.113883.2.1.6.2", "H/O: injury");
        code.getTranslation().add(translationCD1);
        code.getTranslation().add(translationCD2);
        return code;
    }

    @NotNull
    private static RCMRMT030101UKObservationStatement buildReferencedObservationStatement() {
        RCMRMT030101UKObservationStatement observationStatement = new RCMRMT030101UK04ObservationStatement();
        observationStatement.setCode(buildMatchableObservationStatementCode());
        observationStatement.getPertinentInformation().add(buildPertinentInformation(
                "Problem severity: Minor H/O: injury to little finger left hand poss gla..."));
        final var id = new II();
        id.setRoot("DC4A4731-896D-11EE-B3A3-48DF37DF55D0");
        observationStatement.setId(id);

        return observationStatement;
    }

    @NotNull
    private static RCMRMT030101UKObservationStatement buildObservationStatementToBeMatched() {
        final var observationStatement = new RCMRMT030101UK04ObservationStatement();
        observationStatement.setCode(buildMatchableObservationStatementCode());
        observationStatement.getPertinentInformation().add(buildPertinentInformation(
                "(New Episode). H/O: injury to little finger left hand poss glass in wound therefore referred to A+E"));

        return observationStatement;
    }

    @NotNull
    private static RCMRMT030101UK04PertinentInformation02 buildPertinentInformation(String text) {
        var pertinentInformation = new RCMRMT030101UK04PertinentInformation02();
        var pertinentAnnotation = new RCMRMT030101UK04Annotation();
        pertinentAnnotation.setText(text);
        pertinentInformation.setPertinentAnnotation(pertinentAnnotation);

        return pertinentInformation;
    }

    private void addMedicationReferences(List<Condition> conditions,
                                         Condition condition,
                                         RCMRMT030101UK04EhrExtract ehrExtract) {

        final var bundle = new Bundle();
        final var planMedicationRequest = new MedicationRequest().setId(AUTHORISE_ID);
        final var orderMedicationRequest = new MedicationRequest().setId(PRESCRIBE_ID);
        bundle.addEntry(new BundleEntryComponent().setResource(condition));
        bundle.addEntry(new BundleEntryComponent().setResource(planMedicationRequest));
        bundle.addEntry(new BundleEntryComponent().setResource(orderMedicationRequest));
        conditionMapper.addReferences(bundle, conditions, ehrExtract);
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtract(String filename) {
        return unmarshallFile(getFile("classpath:" + CONDITION_RESOURCES_BASE + filename), RCMRMT030101UK04EhrExtract.class);
    }
}
