package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT;
import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION;
import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceClinicalStatus.ACTIVE;
import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceVerificationStatus.UNCONFIRMED;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.Collections;
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
import org.hl7.v3.RCMRMT030101UKEhrExtract;

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
import static uk.nhs.adaptors.common.util.CodeableConceptUtils.createCodeableConcept;

@ExtendWith(MockitoExtension.class)
class AllergyIntoleranceMapperTest {

    private static final String XML_RESOURCES_BASE = "xml/AllergyIntolerance/";
    private static final String COMPOUND_STATEMENT_ROOT_ID = "394559384658936";
    private static final String PRACTISE_CODE = "TESTPRACTISECODE";
    private static final String ENCOUNTER_ID = "62A39454-299F-432E-993E-5A6232B4E099";
    private static final String CODING_DISPLAY_1 = "Ischaemic heart disease";
    private static final String CODING_DISPLAY_2 = "H/O: aspirin allergy";
    private static final String CODING_DISPLAY_3 = "H/O: drug allergy";
    private static final String CODING_DISPLAY_4 = "Coconut oil";
    private static final String PATIENT_ID = "9A5D5A78-1F63-434C-9637-1D7E7843341B";
    private static final String META_PROFILE = "https://fhir.nhs"
        + ".uk/STU3/StructureDefinition/CareConnect-GPC-AllergyIntolerance-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/TESTPRACTISECODE";
    private static final String EPISODICITY_WITH_ORIGINAL_TEXT_NOTE_TEXT =
            "Episodicity : code=303350001, displayName=Ongoing, originalText=Review";
    private static final String EPISODICITY_WITHOUT_ORIGINAL_TEXT_NOTE_TEXT =
            "Episodicity : code=303350001, displayName=Ongoing";
    private static final String PERTINENT_NOTE_TEXT =
            "Reason Ended: Patient reports no subsequent recurrence on same medication Status:"
                    + " Resolved Type: Allergy Criticality: Low Risk Last Occurred: 1978-12-31 Example note text";
    private static final String ALLERGY_NOTE_TEXT = "Allergy Code: " + CODING_DISPLAY_1;
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
    void testGivenDrugAllergyWithAllDataThenAllDataPopulated() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("drug-allergy-structure.xml");

        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(defaultCodeableConcept())
            .thenReturn(secondaryCodeableConcept());

        final List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        assertThat(allergyIntolerances).hasSize(1);
        final AllergyIntolerance allergyIntolerance = allergyIntolerances.get(0);

        assertFixedValues(allergyIntolerance);
        assertExtension(allergyIntolerance);

        assertThat(allergyIntolerance.getCategory().get(0).getValue()).isEqualTo(MEDICATION);
        assertThat(allergyIntolerance.getAssertedDateElement().asStringValue()).isEqualTo("1978-12-31");
        assertThat(allergyIntolerance.getRecorder().getReference()).isEqualTo("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D");
        assertThat(allergyIntolerance.getOnsetDateTimeType().asStringValue())
            .isEqualTo(DateFormatUtil.parseToDateTimeType("19781231").asStringValue());
        assertThat(allergyIntolerance.getAsserter().getReference()).isEqualTo("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D");
        assertThat(allergyIntolerance.getNote().get(0).getText()).isEqualTo(EPISODICITY_WITH_ORIGINAL_TEXT_NOTE_TEXT);
        assertThat(allergyIntolerance.getNote().get(1).getText()).isEqualTo(PERTINENT_NOTE_TEXT);
        assertThat(allergyIntolerance.getNote().get(2).getText()).isEqualTo(ALLERGY_NOTE_TEXT);
        assertThat(allergyIntolerance.getCode().getCodingFirstRep()).isEqualTo(DegradedCodeableConcepts.DEGRADED_DRUG_ALLERGY);
        assertThat(allergyIntolerance.getCode().getCoding().get(1).getDisplay()).isEqualTo(CODING_DISPLAY_2);
    }

    @Test
    void testGivenAuthorAndParticipantThenMapsToRecorderAndAsserterAllergyIntolerance() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("allergy-structure-with-valid-author-and-participant2.xml");

        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(defaultCodeableConcept())
            .thenReturn(secondaryCodeableConcept());

        final List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        assertEquals(1, allergyIntolerances.size());
        final AllergyIntolerance allergyIntolerance = allergyIntolerances.get(0);

        assertAll(
            () -> assertExtension(allergyIntolerance),
            () -> assertEquals("Practitioner/9F2ABD26-1682-FDFE-1E88-19673307C67A", allergyIntolerance.getAsserter().getReference()),
            () -> assertEquals("Practitioner/E7E7B550-09EF-BE85-C20F-34598014166C", allergyIntolerance.getRecorder().getReference())
        );
    }

    /**
     * At this moment it is not very clear if this is the correct behavior.
     * We haven't seen a supplier send over a HL7 in this form, but we want to specify some behaviour.
     */
    @Test
    void testGivenAuthorAndAutParticipant2AuthorAndRecorderThenPopulatedWithParticipant2() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("allergy-structure-with-participant-of-aut-typecode.xml");

        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(defaultCodeableConcept())
            .thenReturn(secondaryCodeableConcept());

        final List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        assertEquals(1, allergyIntolerances.size());
        final AllergyIntolerance allergyIntolerance = allergyIntolerances.get(0);

        assertAll(
            () -> assertExtension(allergyIntolerance),
            () -> assertEquals("Practitioner/9F2ABD26-1682-FDFE-1E88-19673307C67A", allergyIntolerance.getRecorder().getReference()),
            () -> assertEquals("Practitioner/9F2ABD26-1682-FDFE-1E88-19673307C67A", allergyIntolerance.getAsserter().getReference())
        );
    }

    @Test
    void testGivenConfidentialityCodeWithinObservationStatementThenMetaSecurityPopulated() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("allergy-structure-with-observation-statement-confidentiality-code.xml");
        final Coding coding = new Coding()
            .setSystem("http://hl7.org/fhir/v3/ActCode")
            .setCode("NOPAT")
            .setDisplay("no disclosure to patient, family or caregivers without attending provider's authorization");

        final List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper
            .mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        assertEquals(1, allergyIntolerances.size());
        final AllergyIntolerance allergyIntolerance = allergyIntolerances.get(0);
        assertThat(allergyIntolerance.getMeta().getSecurity()).usingRecursiveComparison().isEqualTo(Collections.singletonList(coding));
    }

    @Test
    void testGivenConfidentialityCodeWithinObservationStatementThenMetaSecurityNotPopulated() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("allergy-structure-with-participant-of-aut-typecode.xml");

        final List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper
            .mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        assertEquals(1, allergyIntolerances.size());
        final AllergyIntolerance allergyIntolerance = allergyIntolerances.get(0);
        assertThat(allergyIntolerance.getMeta().getSecurity()).isEmpty();
    }

    @Test
    void testGivenConfidentialityCodeWithinEhrCompositionAndNotObservationStatementThenMetaSecurityPopulated() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("allergy-structure-with-ehr-composition-confidentiality-code.xml");
        final Coding coding = new Coding()
            .setSystem("http://hl7.org/fhir/v3/ActCode")
            .setCode("NOPAT")
            .setDisplay("no disclosure to patient, family or caregivers without attending provider's authorization");

        final List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper
            .mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        assertEquals(1, allergyIntolerances.size());
        final AllergyIntolerance allergyIntolerance = allergyIntolerances.get(0);
        assertThat(allergyIntolerance.getMeta().getSecurity()).usingRecursiveComparison().isEqualTo(Collections.singletonList(coding));
    }

    /**
     * At this moment it is not very clear if this is the correct behavior with such number of participants.
     * We haven't seen a supplier send over a HL7 in this form, but we want to specify some behaviour.
     */
    @Test
    void testGivenAuthorAndMultipleParticipant2sAndOneAutParticipant2AuthorAndRecorderThenPopulatedWithAuthorAndParticipant2() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("allergy-structure-with-author-and-multiple-participants.xml");

        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(defaultCodeableConcept())
            .thenReturn(secondaryCodeableConcept());

        final List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        assertEquals(1, allergyIntolerances.size());
        final AllergyIntolerance allergyIntolerance = allergyIntolerances.get(0);

        assertAll(
            () -> assertExtension(allergyIntolerance),
            () -> assertEquals("Practitioner/9F2ABD26-1682-FDFE-1E88-19673307C67A", allergyIntolerance.getAsserter().getReference()),
            () -> assertEquals("Practitioner/E7E7B550-09EF-BE85-C20F-34598014166C", allergyIntolerance.getRecorder().getReference())
        );
    }

    @Test
    void testGivenNonDrugAllergyWithAllDataThenMapsSuccessfully() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("non-drug-allergy-structure.xml");

        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(defaultCodeableConcept())
            .thenReturn(secondaryCodeableConcept());

        final List<AllergyIntolerance> allergyIntolerances
            = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        assertEquals(1, allergyIntolerances.size());
        final AllergyIntolerance allergyIntolerance = allergyIntolerances.get(0);

        assertFixedValues(allergyIntolerance);

        assertExtension(allergyIntolerance);
        assertEquals(ENVIRONMENT, allergyIntolerance.getCategory().get(0).getValue());
        assertEquals("1978-12-31", allergyIntolerance.getAssertedDateElement().asStringValue());
        assertEquals("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D", allergyIntolerance.getRecorder().getReference());
        assertEquals(DateFormatUtil.parseToDateTimeType("19781231").asStringValue(),
                                                    allergyIntolerance.getOnsetDateTimeType().asStringValue());
        assertEquals("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D", allergyIntolerance.getAsserter().getReference());
        assertEquals(PERTINENT_NOTE_TEXT, allergyIntolerance.getNote().get(0).getText());
        assertEquals(DegradedCodeableConcepts.DEGRADED_NON_DRUG_ALLERGY, allergyIntolerance.getCode().getCodingFirstRep());
        assertEquals(CODING_DISPLAY_1, allergyIntolerance.getCode().getCoding().get(1).getDisplay());
    }

    @Test
    void testGivenCompoundStatementCodeOfNonDrugAllergyCodeThenSetsCodeToTransferDegradedNonDrugAllergy() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("degraded-non-drug-allergy-structure.xml");

        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(new CodeableConcept());

        final List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        assertThat(allergyIntolerances).hasSize(1);
        final AllergyIntolerance allergyIntolerance = allergyIntolerances.get(0);

        assertThat(allergyIntolerance.getCode().getCodingFirstRep())
            .isEqualTo(DegradedCodeableConcepts.DEGRADED_NON_DRUG_ALLERGY);
    }

    @Test
    void testGivenCompoundStatementCodeOfDrugAllergyCodeThenSetsCodeToTransferDegradedDrugAllergy() {
        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
                .thenReturn(nonSnomedCodeableConcept());

        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("degraded-drug-allergy-structure.xml");
        final List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
                getEncounterList(), PRACTISE_CODE);

        assertThat(allergyIntolerances).hasSize(1);
        final AllergyIntolerance allergyIntolerance = allergyIntolerances.get(0);

        assertThat(allergyIntolerance.getCode().getCodingFirstRep())
                .isEqualTo(DegradedCodeableConcepts.DEGRADED_DRUG_ALLERGY);
    }

    @Test
    void testGivenAllergyWithOriginalTextAndNoValueThenMapsCodingTextFromCodeOriginalText() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("allergy-structure-with-original-text-in-code.xml");

        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(defaultCodeableConcept())
            .thenReturn(secondaryCodeableConcept());

        final List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        assertThat(allergyIntolerances).hasSize(1);
        final AllergyIntolerance allergyIntolerance = allergyIntolerances.get(0);

        assertFixedValues(allergyIntolerance);

        assertThat(allergyIntolerance.getCode().getText()).isEqualTo(ORIGINAL_TEXT_IN_CODE);
    }

    @Test
    void testGivenAllergyWithNoOptionalData() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("allergy-structure-with-optional-data.xml");

        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class))).thenReturn(defaultCodeableConcept());

        final List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        assertThat(allergyIntolerances).hasSize(1);
        final AllergyIntolerance allergyIntolerance = allergyIntolerances.get(0);
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
    void testGivenUnkAvailabilityTimeThenAssertedDateIsAuthorTime() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("allergy-structure-with-asserted-date-fallback.xml");

        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class))).thenReturn(defaultCodeableConcept());

        final List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        assertThat(allergyIntolerances).hasSize(1);
        final AllergyIntolerance allergyIntolerance = allergyIntolerances.get(0);

        assertFixedValues(allergyIntolerance);

        assertThat(allergyIntolerance.getAssertedDateElement().asStringValue()).isEqualTo("2010-02-09T12:31:51+00:00");
    }

    @Test
    void testGivenMultipleAllergiesThenExpectAllToBePresent() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("allergy-structure-with-multiple-allergy.xml");

        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class))).thenReturn(defaultCodeableConcept());

        final List<AllergyIntolerance> allergyIntolerances
            = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        assertEquals(THREE, allergyIntolerances.size());
    }

    @Test
    void testGivenStandaloneAllergyThenNoExtensionPresent() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("allergy-structure-invalid-encounter-reference.xml");

        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class))).thenReturn(defaultCodeableConcept());

        final List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        final AllergyIntolerance allergyIntolerance = allergyIntolerances.get(0);
        assertThat(allergyIntolerance.getExtension()).isEmpty();
    }

    @Test
    void testGivenAllergyWithSameTermTexts() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("drug-allergy-structure-with-term-text.xml");

        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class))).thenReturn(defaultCodeableConcept());

        final List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        assertThat(allergyIntolerances).hasSize(1);
        final AllergyIntolerance allergyIntolerance = allergyIntolerances.get(0);

        assertFixedValues(allergyIntolerance);

        assertExtension(allergyIntolerance);
        assertThat(allergyIntolerance.getCategory().get(0).getValue()).isEqualTo(MEDICATION);
        assertThat(allergyIntolerance.getAssertedDateElement().asStringValue()).isEqualTo("1978-12-31");
        assertThat(allergyIntolerance.getRecorder().getReference()).isEqualTo("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D");
        assertThat(allergyIntolerance.getOnsetDateTimeType().asStringValue())
            .isEqualTo(DateFormatUtil.parseToDateTimeType("19781231").asStringValue());
        assertThat(allergyIntolerance.getAsserter().getReference()).isEqualTo("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D");
        assertThat(allergyIntolerance.getNote().get(0).getText()).isEqualTo(PERTINENT_NOTE_TEXT);
        assertThat(allergyIntolerance.getCode().getCodingFirstRep()).isEqualTo(DegradedCodeableConcepts.DEGRADED_DRUG_ALLERGY);
        assertThat(allergyIntolerance.getCode().getCoding().get(1).getDisplay()).isEqualTo(CODING_DISPLAY_1);
        assertThat(allergyIntolerance.getNote().size()).isOne();
    }

    @Test
    void testMapAllergyWithDrugTermText() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("drug-allergy-structure-with-term-text.xml");

        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(tertiaryCodeableConcept())
            .thenReturn(tertiaryCodeableConcept());

        final List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        assertThat(allergyIntolerances).hasSize(1);
        final AllergyIntolerance allergyIntolerance = allergyIntolerances.get(0);

        assertFixedValues(allergyIntolerance);

        assertExtension(allergyIntolerance);
        assertThat(allergyIntolerance.getCategory().get(0).getValue()).isEqualTo(MEDICATION);
        assertThat(allergyIntolerance.getAssertedDateElement().asStringValue()).isEqualTo("1978-12-31");
        assertThat(allergyIntolerance.getRecorder().getReference()).isEqualTo("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D");
        assertThat(allergyIntolerance.getOnsetDateTimeType().asStringValue())
            .isEqualTo(DateFormatUtil.parseToDateTimeType("19781231").asStringValue());
        assertThat(allergyIntolerance.getAsserter().getReference()).isEqualTo("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D");
        assertThat(allergyIntolerance.getNote().get(0).getText()).isEqualTo(PERTINENT_NOTE_TEXT);
        assertThat(allergyIntolerance.getCode().getCodingFirstRep()).isEqualTo(DegradedCodeableConcepts.DEGRADED_DRUG_ALLERGY);
        assertThat(allergyIntolerance.getCode().getCoding().get(1).getDisplay()).isEqualTo(CODING_DISPLAY_3);
        assertThat(allergyIntolerance.getNote().size()).isOne();
    }

    @Test
    void testGivenDrugAllergyWithValueElementThenMapsCodingTextFromValueDescription() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("drug-allergy-with-value.xml");

        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(defaultCodeableConcept())
            .thenReturn(secondaryCodeableConcept());

        final List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        assertThat(allergyIntolerances).hasSize(1);
        final AllergyIntolerance allergyIntolerance = allergyIntolerances.get(0);

        assertFixedValues(allergyIntolerance);

        assertExtension(allergyIntolerance);

        assertThat(allergyIntolerance.getCode().getText()).isEqualTo(DISPLAY_NAME_IN_VALUE);
    }

    @Test
    void testGivenAllergyIntoleranceWithQualifierAndOriginalTextThenNotesContainsEpisodicity() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("drug_allergy_with_qualifier_and_original_text.xml");

        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(defaultCodeableConcept());

        final List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        final AllergyIntolerance allergyIntolerance = allergyIntolerances.get(0);

        assertThat(allergyIntolerance.getNote().get(0).getText())
                .isEqualTo(EPISODICITY_WITH_ORIGINAL_TEXT_NOTE_TEXT);
    }

    @Test
    void testGivenAllergyIntoleranceWithQualifierAndWithoutOriginalTextThenNotesContainsEpisodicity() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("drug_allergy_with_qualifier_without_original_text.xml");

        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(defaultCodeableConcept());

        final List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        final AllergyIntolerance allergyIntolerance = allergyIntolerances.get(0);

        assertThat(allergyIntolerance.getNote().get(0).getText())
                .isEqualTo(EPISODICITY_WITHOUT_ORIGINAL_TEXT_NOTE_TEXT);
    }

    @ParameterizedTest
    @MethodSource("allergyStructuresWithTranslations")
    void testTppNamedSchemaInValue(String filename) {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract(filename);

        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(tertiaryCodeableConcept());

        allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        verify(codeableConceptMapper, times(2)).mapToCodeableConcept(cdCaptor.capture());

        final CD cd = cdCaptor.getAllValues().get(1);

        assertThat(cd.getCode()).isEqualTo(MULTILEX_COCONUT_OIL);
        assertThat(cd.getCodeSystem()).isEqualTo(MULTILEX_CODE_SYSTEM);
        assertThat(cd.getDisplayName()).isEqualTo(CODING_DISPLAY_4);
        assertThat(cd.getTranslation().size()).isOne();

        final CD translation = cd.getTranslation().get(0);

        assertThat(translation.getCodeSystem()).isEqualTo(SNOMED_CODE_SYSTEM);
        assertThat(translation.getCode()).isEqualTo(SNOMED_COCONUT_OIL);
        assertThat(translation.getDisplayName()).isEqualTo(CODING_DISPLAY_4);
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
        return createCodeableConcept(null, SNOMED_CODE_SYSTEM, CODING_DISPLAY_1);
    }

    private CodeableConcept secondaryCodeableConcept() {
        return createCodeableConcept(null, SNOMED_CODE_SYSTEM, CODING_DISPLAY_2);
    }

    private CodeableConcept tertiaryCodeableConcept() {
        return createCodeableConcept(null, null, CODING_DISPLAY_3);
    }

    private CodeableConcept nonSnomedCodeableConcept() {
        return createCodeableConcept(MULTILEX_COCONUT_OIL, MULTILEX_CODE_SYSTEM, CODING_DISPLAY_4);
    }

    @SneakyThrows
    private RCMRMT030101UKEhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UKEhrExtract.class);
    }
}