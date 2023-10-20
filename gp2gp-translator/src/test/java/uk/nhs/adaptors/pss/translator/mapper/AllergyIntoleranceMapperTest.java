package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT;
import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION;
import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceClinicalStatus.ACTIVE;
import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceVerificationStatus.UNCONFIRMED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;
import java.util.stream.Stream;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.CD;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.DegradedCodeableConcepts;

@ExtendWith(MockitoExtension.class)
public class AllergyIntoleranceMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/AllergyIntolerance/";
    private static final String COMPOUND_STATEMENT_ROOT_ID = "394559384658936";
    private static final String PRACTISE_CODE = "TESTPRACTISECODE";
    private static final String ENCOUNTER_ID = "62A39454-299F-432E-993E-5A6232B4E099";
    private static final String CODING_DISPLAY_1 = "Ischaemic heart disease";
    private static final String CODING_DISPLAY_2 = "H/O: aspirin allergy";
    private static final String CODING_DISPLAY_3 = "H/O: drug allergy";
    private static final String PATIENT_ID = "9A5D5A78-1F63-434C-9637-1D7E7843341B";
    private static final String META_PROFILE = "https://fhir.nhs"
        + ".uk/STU3/StructureDefinition/CareConnect-GPC-AllergyIntolerance-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/TESTPRACTISECODE";
    private static final String NOTE_TEXT = "Reason Ended: Patient reports no subsequent recurrence on same medication Status:"
        + " Resolved Type: Allergy Criticality: Low Risk Last Occurred: 1978-12-31 Example note text";
    private static final int THREE = 3;

    private static final String ORIGINAL_TEXT_IN_CODE = "OriginalText from Code";
    private static final String DISPLAY_NAME_IN_VALUE = "Value displayName";
    private static final String MULTILEX_CODE_SYSTEM = "2.16.840.1.113883.2.1.6.4";
    private static final String MULTILEX_COCONUT_OIL = "01142009";
    private static final String SNOMED_CODE_SYSTEM = "2.16.840.1.113883.2.1.3.2.4.15";
    private static final String SNOMED_COCONUT_OIL = "14613911000001107";

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @Captor
    private ArgumentCaptor<CD> cdCaptor;

    @InjectMocks
    private AllergyIntoleranceMapper allergyIntoleranceMapper;

    @Test
    public void testMapDrugAllergyWithAllData() {
        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(defaultCodeableConcept())
            .thenReturn(secondaryCodeableConcept());
        var ehrExtract = unmarshallEhrExtract("drug-allergy-structure.xml");
        List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        assertThat(allergyIntolerances.size()).isEqualTo(1);
        var allergyIntolerance = allergyIntolerances.get(0);

        assertFixedValues(allergyIntolerance);

        assertExtension(allergyIntolerance);
        assertThat(allergyIntolerance.getCategory().get(0).getValue()).isEqualTo(MEDICATION);
        assertThat(allergyIntolerance.getAssertedDateElement().asStringValue()).isEqualTo("1978-12-31");
        assertThat(allergyIntolerance.getRecorder().getReference()).isEqualTo("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D");
        assertThat(allergyIntolerance.getOnsetDateTimeType().asStringValue())
            .isEqualTo(DateFormatUtil.parseToDateTimeType("19781231").asStringValue());
        assertThat(allergyIntolerance.getAsserter().getReference()).isEqualTo("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D");
        assertThat(allergyIntolerance.getNote().get(0).getText()).isEqualTo(NOTE_TEXT);
        assertThat(allergyIntolerance.getCode().getCodingFirstRep()).isEqualTo(DegradedCodeableConcepts.DEGRADED_DRUG_ALLERGY);
        assertThat(allergyIntolerance.getCode().getCoding().get(1).getDisplay()).isEqualTo(CODING_DISPLAY_2);
        assertThat(allergyIntolerance.getNote().get(1).getText()).isEqualTo("Allergy Code: " + CODING_DISPLAY_1);
    }

    @Test
    public void testMapAuthorAndParticipantToRecorderAndAsserterAllergyIntolerance() {

        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(defaultCodeableConcept())
            .thenReturn(secondaryCodeableConcept());
        var ehrExtract = unmarshallEhrExtract("allergy-structure-with-valid-author-and-participant2.xml");
        List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
                                                                                             getEncounterList(), PRACTISE_CODE);

        assertEquals(1, allergyIntolerances.size());
        var allergyIntolerance = allergyIntolerances.get(0);

        assertExtension(allergyIntolerance);
        assertEquals("Practitioner/9F2ABD26-1682-FDFE-1E88-19673307C67A", allergyIntolerance.getRecorder().getReference());
        assertEquals("Practitioner/E7E7B550-09EF-BE85-C20F-34598014166C", allergyIntolerance.getAsserter().getReference());
    }

    @Test
    public void testGivenAuthorAndAutParticipant2AuthorAndRecorderPopulatedWithParticipant2() {
        // At this moment it is not very clear if this is the correct behavior.
        // We haven't seen a supplier send over a HL7 in this form, but we want to specify some behaviour.
        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(defaultCodeableConcept())
            .thenReturn(secondaryCodeableConcept());
        var ehrExtract = unmarshallEhrExtract("allergy-structure-with-participant-of-aut-typecode.xml");
        List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
                                                                                             getEncounterList(), PRACTISE_CODE);

        assertEquals(1, allergyIntolerances.size());
        var allergyIntolerance = allergyIntolerances.get(0);

        assertExtension(allergyIntolerance);
        assertEquals("Practitioner/9F2ABD26-1682-FDFE-1E88-19673307C67A", allergyIntolerance.getRecorder().getReference());
        assertEquals("Practitioner/9F2ABD26-1682-FDFE-1E88-19673307C67A", allergyIntolerance.getAsserter().getReference());
    }

    @Test
    public void testGivenAuthorAndMultipleParticipant2sAndOneAutParticipant2AuthorAndRecorderPopulatedWithAuthorAndParticipant2() {
        // At this moment it is not very clear if this is the correct behavior with such number of participants.
        // We haven't seen a supplier send over a HL7 in this form, but we want to specify some behaviour.
        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(defaultCodeableConcept())
            .thenReturn(secondaryCodeableConcept());
        var ehrExtract = unmarshallEhrExtract("allergy-structure-with-author-and-multiple-participants.xml");
        List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
                                                                                             getEncounterList(), PRACTISE_CODE);

        assertEquals(1, allergyIntolerances.size());
        var allergyIntolerance = allergyIntolerances.get(0);

        assertExtension(allergyIntolerance);
        assertEquals("Practitioner/9F2ABD26-1682-FDFE-1E88-19673307C67A", allergyIntolerance.getRecorder().getReference());
        assertEquals("Practitioner/E7E7B550-09EF-BE85-C20F-34598014166C", allergyIntolerance.getAsserter().getReference());
    }

    @Test
    public void testMapNonDrugAllergyWithAllData() {
        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(defaultCodeableConcept())
            .thenReturn(secondaryCodeableConcept());
        var ehrExtract = unmarshallEhrExtract("non-drug-allergy-structure.xml");
        List<AllergyIntolerance> allergyIntolerances
                                    = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        assertEquals(1, allergyIntolerances.size());
        var allergyIntolerance = allergyIntolerances.get(0);

        assertFixedValues(allergyIntolerance);

        assertExtension(allergyIntolerance);
        assertEquals(ENVIRONMENT, allergyIntolerance.getCategory().get(0).getValue());
        assertEquals("1978-12-31", allergyIntolerance.getAssertedDateElement().asStringValue());
        assertEquals("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D", allergyIntolerance.getRecorder().getReference());
        assertEquals(DateFormatUtil.parseToDateTimeType("19781231").asStringValue(),
                                                    allergyIntolerance.getOnsetDateTimeType().asStringValue());
        assertEquals("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D", allergyIntolerance.getAsserter().getReference());
        assertEquals(NOTE_TEXT, allergyIntolerance.getNote().get(0).getText());
        assertEquals(DegradedCodeableConcepts.DEGRADED_NON_DRUG_ALLERGY, allergyIntolerance.getCode().getCodingFirstRep());
        assertEquals(CODING_DISPLAY_1, allergyIntolerance.getCode().getCoding().get(1).getDisplay());
    }

    @Test
    public void testMapDegradedNonDrugAllergy() {
        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
                .thenReturn(new CodeableConcept());

        var ehrExtract = unmarshallEhrExtract("degraded-non-drug-allergy-structure.xml");
        List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        assertThat(allergyIntolerances.size()).isEqualTo(1);
        var allergyIntolerance = allergyIntolerances.get(0);

        assertThat(allergyIntolerance.getCode().getCodingFirstRep())
            .isEqualTo(DegradedCodeableConcepts.DEGRADED_NON_DRUG_ALLERGY);
    }

    @Test
    public void testMapDegradedDrugAllergy() {
        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
                .thenReturn(new CodeableConcept());

        var ehrExtract = unmarshallEhrExtract("degraded-drug-allergy-structure.xml");
        List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
                getEncounterList(), PRACTISE_CODE);

        assertThat(allergyIntolerances.size()).isEqualTo(1);
        var allergyIntolerance = allergyIntolerances.get(0);

        assertThat(allergyIntolerance.getCode().getCodingFirstRep())
                .isEqualTo(DegradedCodeableConcepts.DEGRADED_DRUG_ALLERGY);
    }

    @Test
    public void When_AllergyWithOriginalTextAndNoValue_Expect_MapsCodingTextFromCodeOriginalText() {
        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(defaultCodeableConcept())
            .thenReturn(secondaryCodeableConcept());

        var ehrExtract = unmarshallEhrExtract("allergy-structure-with-original-text-in-code.xml");
        List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        assertThat(allergyIntolerances.size()).isEqualTo(1);
        var allergyIntolerance = allergyIntolerances.get(0);

        assertFixedValues(allergyIntolerance);

        assertThat(allergyIntolerance.getCode().getText()).isEqualTo(ORIGINAL_TEXT_IN_CODE);
    }

    @Test
    public void testMapAllergyWithNoOptionalData() {
        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class))).thenReturn(defaultCodeableConcept());
        var ehrExtract = unmarshallEhrExtract("allergy-structure-with-optional-data.xml");
        List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        assertThat(allergyIntolerances.size()).isEqualTo(1);
        var allergyIntolerance = allergyIntolerances.get(0);

        assertFixedValues(allergyIntolerance);

        assertThat(allergyIntolerance.getCode().getCodingFirstRep()).isEqualTo(DegradedCodeableConcepts.DEGRADED_DRUG_ALLERGY);
        assertThat(allergyIntolerance.getCode().getCoding().get(1).getDisplay()).isEqualTo(CODING_DISPLAY_1);
        assertThat(allergyIntolerance.getAssertedDateElement().asStringValue()).isEqualTo("2019-07-08T13:35:00+00:00");
        assertThat(allergyIntolerance.getRecorder().getReference()).isNull(); // this is added later in the UnknownPractitionerHandler
        assertThat(allergyIntolerance.getAsserter().getReference()).isNull();
        assertThat(allergyIntolerance.getOnset()).isNull();
        assertThat(allergyIntolerance.getNote()).isEmpty();
    }

    @Test
    public void testMapMultipleAllergies() {
        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class))).thenReturn(defaultCodeableConcept());
        var ehrExtract = unmarshallEhrExtract("allergy-structure-with-multiple-allergy.xml");
        List<AllergyIntolerance> allergyIntolerances
                                = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        assertEquals(THREE, allergyIntolerances.size());
    }

    @Test
    public void testMapStandaloneAllergy() {
        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class))).thenReturn(defaultCodeableConcept());
        var ehrExtract = unmarshallEhrExtract("allergy-structure-invalid-encounter-reference.xml");
        List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        var allergyIntolerance = allergyIntolerances.get(0);
        assertThat(allergyIntolerance.getExtension()).isEmpty();
    }

    @Test
    public void testMapAllergyWithSameTermTexts() {
        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class))).thenReturn(defaultCodeableConcept());
        var ehrExtract = unmarshallEhrExtract("drug-allergy-structure-with-term-text.xml");
        List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        assertThat(allergyIntolerances.size()).isEqualTo(1);
        var allergyIntolerance = allergyIntolerances.get(0);

        assertFixedValues(allergyIntolerance);

        assertExtension(allergyIntolerance);
        assertThat(allergyIntolerance.getCategory().get(0).getValue()).isEqualTo(MEDICATION);
        assertThat(allergyIntolerance.getAssertedDateElement().asStringValue()).isEqualTo("1978-12-31");
        assertThat(allergyIntolerance.getRecorder().getReference()).isEqualTo("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D");
        assertThat(allergyIntolerance.getOnsetDateTimeType().asStringValue())
            .isEqualTo(DateFormatUtil.parseToDateTimeType("19781231").asStringValue());
        assertThat(allergyIntolerance.getAsserter().getReference()).isEqualTo("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D");
        assertThat(allergyIntolerance.getNote().get(0).getText()).isEqualTo(NOTE_TEXT);
        assertThat(allergyIntolerance.getCode().getCodingFirstRep()).isEqualTo(DegradedCodeableConcepts.DEGRADED_DRUG_ALLERGY);
        assertThat(allergyIntolerance.getCode().getCoding().get(1).getDisplay()).isEqualTo(CODING_DISPLAY_1);
        assertThat(allergyIntolerance.getNote().size()).isOne();
    }

    @Test
    public void testMapAllergyWithDrugTermText() {
        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(tertiaryCodeableConcept())
            .thenReturn(tertiaryCodeableConcept());
        var ehrExtract = unmarshallEhrExtract("drug-allergy-structure-with-term-text.xml");
        List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        assertThat(allergyIntolerances.size()).isEqualTo(1);
        var allergyIntolerance = allergyIntolerances.get(0);

        assertFixedValues(allergyIntolerance);

        assertExtension(allergyIntolerance);
        assertThat(allergyIntolerance.getCategory().get(0).getValue()).isEqualTo(MEDICATION);
        assertThat(allergyIntolerance.getAssertedDateElement().asStringValue()).isEqualTo("1978-12-31");
        assertThat(allergyIntolerance.getRecorder().getReference()).isEqualTo("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D");
        assertThat(allergyIntolerance.getOnsetDateTimeType().asStringValue())
            .isEqualTo(DateFormatUtil.parseToDateTimeType("19781231").asStringValue());
        assertThat(allergyIntolerance.getAsserter().getReference()).isEqualTo("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D");
        assertThat(allergyIntolerance.getNote().get(0).getText()).isEqualTo(NOTE_TEXT);
        assertThat(allergyIntolerance.getCode().getCodingFirstRep()).isEqualTo(DegradedCodeableConcepts.DEGRADED_DRUG_ALLERGY);
        assertThat(allergyIntolerance.getCode().getCoding().get(1).getDisplay()).isEqualTo(CODING_DISPLAY_3);
        assertThat(allergyIntolerance.getNote().size()).isOne();
    }

    @Test void When_DrugAllergyWithValueElement_Expect_MapsCodingTextFromValueDescription() {
        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(defaultCodeableConcept())
            .thenReturn(secondaryCodeableConcept());
        var ehrExtract = unmarshallEhrExtract("drug-allergy-with-value.xml");
        List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        assertThat(allergyIntolerances.size()).isEqualTo(1);
        var allergyIntolerance = allergyIntolerances.get(0);

        assertFixedValues(allergyIntolerance);

        assertExtension(allergyIntolerance);

        assertThat(allergyIntolerance.getCode().getText()).isEqualTo(DISPLAY_NAME_IN_VALUE);
    }

    @ParameterizedTest
    @MethodSource("allergyStructuresWithTranslations")
    public void testTppNamedSchemaInValue(String filename) {
        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
                .thenReturn(tertiaryCodeableConcept());

        var ehrExtract = unmarshallEhrExtract(filename);

        allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        verify(codeableConceptMapper, times(2)).mapToCodeableConcept(cdCaptor.capture());

        CD cd = cdCaptor.getAllValues().get(1);

        assertThat(cd.getCode()).isEqualTo(MULTILEX_COCONUT_OIL);
        assertThat(cd.getCodeSystem()).isEqualTo(MULTILEX_CODE_SYSTEM);
        assertThat(cd.getDisplayName()).isEqualTo("Coconut oil");
        assertThat(cd.getTranslation().size()).isOne();

        var translation = cd.getTranslation().get(0);

        assertThat(translation.getCodeSystem()).isEqualTo(SNOMED_CODE_SYSTEM);
        assertThat(translation.getCode()).isEqualTo(SNOMED_COCONUT_OIL);
        assertThat(translation.getDisplayName()).isEqualTo("Coconut oil");
    }

    private static Stream<Arguments> allergyStructuresWithTranslations() {
        return Stream.of(
            Arguments.of("allergy-structure-tpp-named-schema.xml"),
            Arguments.of("allergy-structure-with-translations.xml")
        );
    }

    private void assertFixedValues(AllergyIntolerance allergyIntolerance) {
        assertThat(allergyIntolerance.getId()).isEqualTo(COMPOUND_STATEMENT_ROOT_ID);
        assertThat(allergyIntolerance.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertIdentifier(allergyIntolerance.getIdentifierFirstRep(), allergyIntolerance.getId());
        assertThat(allergyIntolerance.getClinicalStatus()).isEqualTo(ACTIVE);
        assertThat(allergyIntolerance.getVerificationStatus()).isEqualTo(UNCONFIRMED);
        assertThat(allergyIntolerance.getPatient().getResource().getIdElement().getValue()).isEqualTo(PATIENT_ID);
    }

    private void assertExtension(AllergyIntolerance allergyIntolerance) {
        var encounterID = allergyIntolerance.getExtension().stream()
            .map(Extension::getValue)
            .map(Reference.class::cast)
            .map(Reference::getResource)
            .map(encounter -> encounter.getIdElement().getValue())
            .findFirst()
            .orElseThrow();

        assertThat(encounterID).isEqualTo(ENCOUNTER_ID);
    }

    private void assertIdentifier(Identifier identifier, String id) {
        assertThat(identifier.getSystem()).isEqualTo(IDENTIFIER_SYSTEM);
        assertThat(identifier.getValue()).isEqualTo(id);
    }

    private List<Encounter> getEncounterList() {
        var encounter = new Encounter();
        encounter.setId(ENCOUNTER_ID);
        return List.of(encounter);
    }

    private Patient getPatient() {
        var patient = new Patient();
        patient.setId(PATIENT_ID);
        return patient;
    }

    private CodeableConcept defaultCodeableConcept() {
        var codeableConcept = new CodeableConcept();
        var coding = new Coding();
        coding.setDisplay(CODING_DISPLAY_1);
        coding.setSystem(SNOMED_CODE_SYSTEM);
        codeableConcept.addCoding(coding);

        return codeableConcept;
    }

    private CodeableConcept secondaryCodeableConcept() {
        var codeableConcept = new CodeableConcept();
        var coding = new Coding();
        coding.setSystem(SNOMED_CODE_SYSTEM);
        coding.setDisplay(CODING_DISPLAY_2);
        codeableConcept.addCoding(coding);

        return codeableConcept;
    }

    private CodeableConcept tertiaryCodeableConcept() {
        var codeableConcept = new CodeableConcept();
        var coding = new Coding();
        coding.setDisplay(CODING_DISPLAY_3);
        codeableConcept.addCoding(coding);

        return codeableConcept;
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}
