package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT;
import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION;
import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceClinicalStatus.ACTIVE;
import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceVerificationStatus.UNCONFIRMED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

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

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

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
        assertThat(allergyIntolerance.getCode().getCodingFirstRep().getDisplay()).isEqualTo(CODING_DISPLAY_2);
        assertThat(allergyIntolerance.getNote().get(1).getText()).isEqualTo("Allergy Code: " + CODING_DISPLAY_1);
    }

    @Test
    public void testMapNonDrugAllergyWithAllData() {
        when(codeableConceptMapper.mapToCodeableConcept(any(CD.class)))
            .thenReturn(defaultCodeableConcept())
            .thenReturn(secondaryCodeableConcept());
        var ehrExtract = unmarshallEhrExtract("non-drug-allergy-structure.xml");
        List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        assertThat(allergyIntolerances.size()).isEqualTo(1);
        var allergyIntolerance = allergyIntolerances.get(0);

        assertFixedValues(allergyIntolerance);

        assertExtension(allergyIntolerance);
        assertThat(allergyIntolerance.getCategory().get(0).getValue()).isEqualTo(ENVIRONMENT);
        assertThat(allergyIntolerance.getAssertedDateElement().asStringValue()).isEqualTo("1978-12-31");
        assertThat(allergyIntolerance.getRecorder().getReference()).isEqualTo("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D");
        assertThat(allergyIntolerance.getOnsetDateTimeType()
            .asStringValue()).isEqualTo(DateFormatUtil.parseToDateTimeType("19781231").asStringValue());
        assertThat(allergyIntolerance.getAsserter().getReference()).isEqualTo("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D");
        assertThat(allergyIntolerance.getNote().get(0).getText()).isEqualTo(NOTE_TEXT);
        assertThat(allergyIntolerance.getCode().getCodingFirstRep().getDisplay()).isEqualTo(CODING_DISPLAY_1);
    }


    @Test
    public void testMapAllergyWithOriginalTextInObservationCode() {
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

        assertThat(allergyIntolerance.getCode().getCodingFirstRep().getDisplay()).isEqualTo(CODING_DISPLAY_1);
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
        List<AllergyIntolerance> allergyIntolerances = allergyIntoleranceMapper.mapResources(ehrExtract, getPatient(),
            getEncounterList(), PRACTISE_CODE);

        assertThat(allergyIntolerances.size()).isEqualTo(THREE);
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
        assertThat(allergyIntolerance.getCode().getCodingFirstRep().getDisplay()).isEqualTo(CODING_DISPLAY_1);
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
        assertThat(allergyIntolerance.getCode().getCodingFirstRep().getDisplay()).isEqualTo(CODING_DISPLAY_3);
        assertThat(allergyIntolerance.getNote().size()).isOne();
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
            .get();

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
        codeableConcept.addCoding(coding);

        return codeableConcept;
    }

    private CodeableConcept secondaryCodeableConcept() {
        var codeableConcept = new CodeableConcept();
        var coding = new Coding();
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
