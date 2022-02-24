package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationStatus;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
public class ObservationMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/Observation/";
    private static final String EXAMPLE_ID = "263B2A9F-0B1D-4697-943A-328F70E068DE";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Observation-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String INTERPRETATION_SYSTEM = "http://hl7.org/fhir/v2/0078";
    private static final String CODING_DISPLAY_MOCK = "Test Display";
    private static final String QUANTITY_SYSTEM = "http://unitsofmeasure.org";
    private static final String ISSUED_EHR_COMPOSITION_EXAMPLE = "2020-01-01T01:01:01.000+00:00";
    private static final String PPRF_PARTICIPANT_ID = "Practitioner/1230F602-6BB1-47E0-B2EC-39912A59787D";
    private static final String NEGATIVE_VALUE = "Negative";
    private static final String TEST_DISPLAY_VALUE = "Test display name";

    private static final CodeableConcept CODEABLE_CONCEPT = new CodeableConcept()
        .addCoding(new Coding().setDisplay(CODING_DISPLAY_MOCK));

    private static final List<Encounter> ENCOUNTER_LIST = List.of(
        (Encounter) new Encounter().setId("TEST_ID_MATCHING_ENCOUNTER")
    );

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @Mock
    private Patient patient;

    @InjectMocks
    private ObservationMapper observationMapper;

    @Test
    public void mapObservationWithValidData() {
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(CODEABLE_CONCEPT);

        var ehrExtract = unmarshallEhrExtractElement("full_valid_data_observation_example.xml");
        var observation = observationMapper.mapObservations(ehrExtract, patient, ENCOUNTER_LIST).get(0);

        assertFixedValues(observation);
        assertThat(observation.getId()).isEqualTo(EXAMPLE_ID);
        assertThat(observation.getCode().getCodingFirstRep().getDisplay()).isEqualTo(CODING_DISPLAY_MOCK);
        assertThat(observation.getEffective() instanceof DateTimeType).isTrue();
        assertThat(observation.getEffectiveDateTimeType().getValue()).isEqualTo("2019-07-08T13:35:00+00:00");
        assertThat(observation.getIssuedElement().asStringValue()).isEqualTo(ISSUED_EHR_COMPOSITION_EXAMPLE);
        assertThat(observation.getPerformer().get(0).getReference()).isEqualTo(PPRF_PARTICIPANT_ID);
        assertThat(observation.getValue() instanceof Quantity).isTrue();
        assertQuantity(observation.getValueQuantity(), "27", "kg/m2");
        assertInterpretation(observation.getInterpretation(), "High", "H", "High");
        assertThat(observation.getComment()).isEqualTo("Subject: Uncle Test text 1");
        assertThat(observation.getReferenceRange().get(0).getText()).isEqualTo("Age and sex based");
        assertQuantity(observation.getReferenceRange().get(0).getLow(), "20", "L");
        assertQuantity(observation.getReferenceRange().get(0).getHigh(), "22", "L");
        assertThat(observation.hasSubject()).isTrue();
    }

    @Test
    public void mapObservationWithNoOptionalData() {
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(CODEABLE_CONCEPT);
        var ehrExtract = unmarshallEhrExtractElement("no_optional_data_observation_example.xml");
        var observation = observationMapper.mapObservations(ehrExtract, patient, ENCOUNTER_LIST).get(0);

        assertFixedValues(observation);
        assertThat(observation.getId()).isEqualTo(EXAMPLE_ID);
        assertThat(observation.getCode().getCodingFirstRep().getDisplay()).isEqualTo(CODING_DISPLAY_MOCK);
        assertThat(observation.getIssuedElement().asStringValue()).isEqualTo(ISSUED_EHR_COMPOSITION_EXAMPLE);
        assertThat(observation.getPerformer().get(0).getReference()).isEqualTo(PPRF_PARTICIPANT_ID);
        assertThat(observation.getEffective()).isNull();
        assertThat(observation.getValue()).isNull();
        assertThat(observation.getInterpretation().getCoding()).isEmpty();
        assertThat(observation.getComment()).isNull();
        assertThat(observation.getReferenceRange()).isEmpty();
    }

    @Test
    public void mapObservationWithValueStringUsingValueTypeST() {
        var ehrExtract = unmarshallEhrExtractElement("value_st_observation_example.xml");
        var observation = observationMapper.mapObservations(ehrExtract, patient, ENCOUNTER_LIST).get(0);

        assertThat(observation.getValue() instanceof StringType).isTrue();
        assertThat(observation.getValueStringType().getValue()).isEqualToIgnoringWhitespace(NEGATIVE_VALUE);
    }

    @Test
    public void mapObservationWithValueStringUsingValueTypeCVOriginalText() {
        var ehrExtract = unmarshallEhrExtractElement("value_cv_display_name_observation_example.xml");
        var observation = observationMapper.mapObservations(ehrExtract, patient, ENCOUNTER_LIST).get(0);

        assertThat(observation.getValue() instanceof StringType).isTrue();
        assertThat(observation.getValueStringType().getValue()).isEqualTo(TEST_DISPLAY_VALUE);
    }

    @Test
    public void mapObservationWithMultiplePertinentInformation() {
        var ehrExtract = unmarshallEhrExtractElement("multiple_pertinent_information_observation_example.xml");
        var observation = observationMapper.mapObservations(ehrExtract, patient, ENCOUNTER_LIST).get(0);

        assertThat(observation.getComment()).isEqualTo("Test text 1 Test text 2");
    }

    @Test
    public void mapObservationWithCompositionIdMatchingEncounter() {
        var ehrExtract = unmarshallEhrExtractElement("ehr_composition_id_matching_encounter_observation_example.xml");
        var observation = observationMapper.mapObservations(ehrExtract, patient, ENCOUNTER_LIST).get(0);

        assertThat(observation.hasContext()).isTrue();
    }

    @Test
    public void mapObservationWithEffectiveDateTime() {
        var ehrExtract = unmarshallEhrExtractElement(
            "effective_date_time_type_using_effective_time_center.xml");
        var observation = observationMapper.mapObservations(ehrExtract, patient, ENCOUNTER_LIST).get(0);

        assertThat(observation.getEffective() instanceof DateTimeType);
        assertThat(observation.getEffectiveDateTimeType().getValueAsString()).isEqualTo("2010-05-21");
    }

    @Test
    public void mapObservationWithEffectivePeriod() {
        var ehrExtract = unmarshallEhrExtractElement(
            "effective_period_start_end_using_effective_time_observation_example.xml");
        var observation = observationMapper.mapObservations(ehrExtract, patient, ENCOUNTER_LIST).get(0);

        assertThat(observation.getEffective() instanceof Period);
        assertThat(observation.getEffectivePeriod().getStartElement().getValueAsString()).isEqualTo("2010-05-21");
        assertThat(observation.getEffectivePeriod().getEndElement().getValueAsString()).isEqualTo("2010-05-22");
    }

    @Test
    public void handleEmptyComponentWithNoObservationStatement() {
        var ehrExtract = unmarshallEhrExtractElement("ehr_composition_with_no_observation_statements_example.xml");
        var observations = observationMapper.mapObservations(ehrExtract, patient, ENCOUNTER_LIST);

        assertThat(observations).isEmpty();
    }

    private void assertFixedValues(Observation observation) {
        assertThat(observation.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThat(observation.getIdentifier().get(0).getSystem()).isEqualTo(IDENTIFIER_SYSTEM);
        assertThat(observation.getIdentifier().get(0).getValue()).isEqualTo(EXAMPLE_ID);
        assertThat(observation.getStatus()).isEqualTo(ObservationStatus.FINAL);
    }

    private void assertInterpretation(CodeableConcept interpretation, String text, String code, String display) {
        assertThat(interpretation.getText()).isEqualTo(text);
        assertThat(interpretation.getCoding().get(0).getCode()).isEqualTo(code);
        assertThat(interpretation.getCoding().get(0).getDisplay()).isEqualTo(display);
        assertThat(interpretation.getCoding().get(0).getSystem()).isEqualTo(INTERPRETATION_SYSTEM);
    }

    private void assertQuantity(Quantity quantity, String value, String unitAndCode) {
        assertThat(quantity.getValue().toString()).isEqualTo(value);
        assertThat(quantity.getUnit()).isEqualTo(unitAndCode);
        assertThat(quantity.getCode()).isEqualTo(unitAndCode);
        assertThat(quantity.getSystem()).isEqualTo(QUANTITY_SYSTEM);
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtractElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}
