package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationStatus;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.junit.jupiter.api.BeforeEach;
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
    private static final int QUANTITY_VALUE_MOCK = 100;
    private static final String QUANTITY_UNIT_CODE_MOCK = "ml";
    private static final String QUANTITY_SYSTEM_MOCK = "http://unitsofmeasure.org";
    private static final String QUANTITY_LOW_VALUE = "20";
    private static final String QUANTITY_HIGH_VALUE = "22";
    private static final String ISSUED_EHR_COMPOSITION_EXAMPLE = "2020-01-01T01:01:01.000+00:00";
    private static final String ISSUED_EHR_EXTRACT_EXAMPLE = "2020-02-01T01:01:01.000+00:00";
    private static final String EFFECTIVE_PERIOD_START_EXAMPLE_1 = "Fri May 21 01:00:00 BST 2010";
    private static final String EFFECTIVE_PERIOD_START_EXAMPLE_2 = "Thu May 20 01:00:00 BST 2010";
    private static final String EFFECTIVE_PERIOD_END_EXAMPLE = "Sat May 22 01:00:00 BST 2010";
    private static final String PRF_PARTICIPANT_ID = "Practitioner/58341512-03F3-4C8E-B41C-A8FCA3886BBB";
    private static final String PPRF_PARTICIPANT_ID = "Practitioner/1230F602-6BB1-47E0-B2EC-39912A59787D";
    private static final String PARTICIPANT2_PARTICIPANT_ID = "Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D";
    private static final String QUANTITY_EXTENSION_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect"
        + "-ValueApproximation-1";

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @Mock
    private QuantityMapper quantityMapper;

    @InjectMocks
    private ObservationMapper observationMapper;

    @BeforeEach
    public void setUp() {
        var coding = new Coding().setDisplay(CODING_DISPLAY_MOCK);
        var codeableConcept = new CodeableConcept().addCoding(coding);
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(codeableConcept);

        var quantity = new Quantity()
            .setValue(QUANTITY_VALUE_MOCK)
            .setCode(QUANTITY_UNIT_CODE_MOCK)
            .setSystem(QUANTITY_SYSTEM_MOCK)
            .setUnit(QUANTITY_UNIT_CODE_MOCK);

        lenient().when(quantityMapper.mapQuantity(any())).thenReturn(quantity);
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtractElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }

    private RCMRMT030101UK04ObservationStatement getObservationStatement(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent().get(0).getEhrFolder().getComponent().get(0).getEhrComposition().getComponent().get(0)
            .getObservationStatement();
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

    private void assertQuantity(Quantity quantity, String value) {
        assertThat(quantity.getValue().toString()).isEqualTo(value);
        assertThat(quantity.getUnit()).isEqualTo(QUANTITY_UNIT_CODE_MOCK);
        assertThat(quantity.getCode()).isEqualTo(QUANTITY_UNIT_CODE_MOCK);
        assertThat(quantity.getSystem()).isEqualTo(QUANTITY_SYSTEM_MOCK);
    }

    @Test
    public void mapObservationWithValidData() {
        var ehrExtract = unmarshallEhrExtractElement("full_valid_data_observation_example.xml");
        var observationStatement = getObservationStatement(ehrExtract);

        var observation = observationMapper.mapToObservation(ehrExtract, observationStatement);

        assertFixedValues(observation);
        assertThat(observation.getId()).isEqualTo(EXAMPLE_ID);
        assertThat(observation.getCode().getCodingFirstRep().getDisplay()).isEqualTo(CODING_DISPLAY_MOCK);
        assertThat(observation.getEffective() instanceof DateTimeType);
        assertThat(observation.getEffectiveDateTimeType().getValue()).isEqualTo("2019-07-08T13:35:00+00:00");
        assertThat(observation.getIssuedElement().asStringValue()).isEqualTo(ISSUED_EHR_COMPOSITION_EXAMPLE);
        assertThat(observation.getPerformer().get(0).getReference()).isEqualTo(PPRF_PARTICIPANT_ID);
        assertThat(observation.getValue() instanceof Quantity);
        assertQuantity(observation.getValueQuantity(), "100");
        assertInterpretation(observation.getInterpretation(), "High", "H", "High");
        assertThat(observation.getComment()).isEqualTo("Subject: Uncle Test text 1");
        assertThat(observation.getReferenceRange().get(0).getText()).isEqualTo("Age and sex based");
        assertQuantity(observation.getReferenceRange().get(0).getLow(), QUANTITY_LOW_VALUE);
        assertQuantity(observation.getReferenceRange().get(0).getHigh(), QUANTITY_HIGH_VALUE);
    }

    @Test
    public void mapObservationWithNoOptionalData() {
        var ehrExtract = unmarshallEhrExtractElement("no_optional_data_observation_example.xml");
        var observationStatement = getObservationStatement(ehrExtract);

        var observation = observationMapper.mapToObservation(ehrExtract, observationStatement);

        assertFixedValues(observation);
        assertThat(observation.getId()).isEqualTo(EXAMPLE_ID);
        assertThat(observation.getCode().getCodingFirstRep().getDisplay()).isEqualTo(CODING_DISPLAY_MOCK);
        assertThat(observation.getIssuedElement().asStringValue()).isEqualTo(ISSUED_EHR_COMPOSITION_EXAMPLE);
        assertThat(observation.getPerformer().get(0).getReference()).isEqualTo(PPRF_PARTICIPANT_ID);
        assertThat(observation.getEffective()).isNull();
        assertThat(observation.getValue()).isNull();
        assertThat(observation.getInterpretation().getCoding().isEmpty());
        assertThat(observation.getComment()).isNull();
        assertThat(observation.getReferenceRange().isEmpty());
    }

    @Test
    public void mapObservationWithEffectivePeriodStartEndUsingEffectiveTime() {
        var ehrExtract = unmarshallEhrExtractElement(
            "effective_period_start_end_using_effective_time_observation_example.xml");
        var observationStatement = getObservationStatement(ehrExtract);

        var observation = observationMapper.mapToObservation(ehrExtract, observationStatement);

        assertThat(observation.getEffective() instanceof Period);
        assertThat(observation.getEffectivePeriod().getStart().toString()).isEqualTo(EFFECTIVE_PERIOD_START_EXAMPLE_1);
        assertThat(observation.getEffectivePeriod().getEnd().toString()).isEqualTo(EFFECTIVE_PERIOD_END_EXAMPLE);
    }

    @Test
    public void mapObservationWithEffectivePeriodStartNoEnd() {
        var ehrExtract = unmarshallEhrExtractElement(
            "effective_period_start_no_high_observation_example.xml");
        var observationStatement = getObservationStatement(ehrExtract);

        var observation = observationMapper.mapToObservation(ehrExtract, observationStatement);

        assertThat(observation.getEffective() instanceof Period);
        assertThat(observation.getEffectivePeriod().getStart().toString()).isEqualTo(EFFECTIVE_PERIOD_START_EXAMPLE_1);
        assertThat(observation.getEffectivePeriod().getEnd()).isNull();
    }

    @Test
    public void mapObservationWithEffectivePeriodStartNoStart() {
        var ehrExtract = unmarshallEhrExtractElement(
            "effective_period_end_no_low_observation_example.xml");
        var observationStatement = getObservationStatement(ehrExtract);

        var observation = observationMapper.mapToObservation(ehrExtract, observationStatement);

        assertThat(observation.getEffective() instanceof Period);
        assertThat(observation.getEffectivePeriod().getStart()).isNull();
        assertThat(observation.getEffectivePeriod().getEnd().toString()).isEqualTo(EFFECTIVE_PERIOD_END_EXAMPLE);
    }

    @Test
    public void mapObservationWithEffectivePeriodStartEndLowIsAvailabilityTime() {
        var ehrExtract = unmarshallEhrExtractElement(
            "effective_period_start_end_low_is_availability_time_observation_example.xml");
        var observationStatement = getObservationStatement(ehrExtract);

        var observation = observationMapper.mapToObservation(ehrExtract, observationStatement);

        assertThat(observation.getEffective() instanceof Period);
        assertThat(observation.getEffectivePeriod().getStart().toString()).isEqualTo(EFFECTIVE_PERIOD_START_EXAMPLE_2);
        assertThat(observation.getEffectivePeriod().getEnd().toString()).isEqualTo(EFFECTIVE_PERIOD_END_EXAMPLE);
    }

    @Test
    public void mapObservationWithEffectiveDateTimeTypeUsingAvailabilityTime() {
        var ehrExtract = unmarshallEhrExtractElement(
            "effective_date_time_type_using_availability_time_observation_example.xml");
        var observationStatement = getObservationStatement(ehrExtract);

        var observation = observationMapper.mapToObservation(ehrExtract, observationStatement);

        assertThat(observation.getEffective() instanceof DateTimeType);
        assertThat(observation.getEffectiveDateTimeType().getValue()).isEqualTo("2010-05-20T01:00:00.000");
    }

    @Test
    public void mapObservationWithNoEffective() {
        var ehrExtract = unmarshallEhrExtractElement(
            "no_effective_observation_example.xml");
        var observationStatement = getObservationStatement(ehrExtract);

        var observation = observationMapper.mapToObservation(ehrExtract, observationStatement);

        assertThat(observation.getEffective()).isNull();
    }

    @Test
    public void mapObservationWithIssuedUsingEhrExtract() {
        var ehrExtract = unmarshallEhrExtractElement(
            "issued_using_ehr_extract_observation_example.xml");
        var observationStatement = getObservationStatement(ehrExtract);

        var observation = observationMapper.mapToObservation(ehrExtract, observationStatement);

        assertThat(observation.getIssuedElement().asStringValue()).isEqualTo(ISSUED_EHR_EXTRACT_EXAMPLE);
    }

    @Test
    public void mapObservationWithPerformerPrfParticipant() {
        var ehrExtract = unmarshallEhrExtractElement(
            "performer_prf_participant_observation_example.xml");
        var observationStatement = getObservationStatement(ehrExtract);

        var observation = observationMapper.mapToObservation(ehrExtract, observationStatement);

        assertThat(observation.getPerformer().get(0).getReference()).isEqualTo(PRF_PARTICIPANT_ID);
    }

    @Test
    public void mapObservationWithPerformerParticipant2() {
        var ehrExtract = unmarshallEhrExtractElement(
            "performer_participant2_observation_example.xml");
        var observationStatement = getObservationStatement(ehrExtract);

        var observation = observationMapper.mapToObservation(ehrExtract, observationStatement);

        assertThat(observation.getPerformer().get(0).getReference()).isEqualTo(PARTICIPANT2_PARTICIPANT_ID);
    }

    @Test
    public void mapObservationWithIvlpqValueQuantity() {
        var ehrExtract = unmarshallEhrExtractElement(
            "ivl_pq_value_quantity_observation_example.xml");
        var observationStatement = getObservationStatement(ehrExtract);

        var observation = observationMapper.mapToObservation(ehrExtract, observationStatement);

        assertThat(observation.getValue() instanceof Quantity);
        assertQuantity(observation.getValueQuantity(), "100");
    }

    @Test
    public void mapObservationWithValueQuantityWithExtension() {
        var ehrExtract = unmarshallEhrExtractElement(
            "value_quantity_with_extension_observation_example.xml");
        var observationStatement = getObservationStatement(ehrExtract);

        var observation = observationMapper.mapToObservation(ehrExtract, observationStatement);

        assertThat(observation.getValue() instanceof Quantity);
        assertThat(observation.getValueQuantity().getExtension().get(0).getUrl()).isEqualTo(QUANTITY_EXTENSION_URL);
        assertThat(observation.getValueQuantity().getExtension().get(0).getValueAsPrimitive().getValue()).isEqualTo(true);
    }

    @Test
    public void mapObservationWithValueStringUsingValueTypeST() {
        var ehrExtract = unmarshallEhrExtractElement(
            "value_st_observation_example.xml");
        var observationStatement = getObservationStatement(ehrExtract);

        var observation = observationMapper.mapToObservation(ehrExtract, observationStatement);

        assertThat(observation.getValue() instanceof StringType);
        assertThat(observation.getValueStringType().getValue()).isEqualTo("");
    }

    @Test
    public void mapObservationWithValueStringUsingValueTypeCVOriginalText() {
        var ehrExtract = unmarshallEhrExtractElement(
            "value_cv_original_text_observation_example.xml");
        var observationStatement = getObservationStatement(ehrExtract);

        var observation = observationMapper.mapToObservation(ehrExtract, observationStatement);

        //todo: translation/original text???
        //todo: add test for display
        assertThat(observation.getValue() instanceof StringType);
        assertThat(observation.getValueStringType().getValue()).isEqualTo("");
    }

    @Test
    public void mapObservationWithValueStringUsingValueTypeCVDisplayName() {
        var ehrExtract = unmarshallEhrExtractElement(
            "value_cv_display_name_observation_example.xml");
        var observationStatement = getObservationStatement(ehrExtract);

        var observation = observationMapper.mapToObservation(ehrExtract, observationStatement);

        assertThat(observation.getValue() instanceof StringType);
        assertThat(observation.getValueStringType().getValue()).isEqualTo("");
    }

    @Test
    public void mapObservationWithInterpretationLow() {
        var ehrExtract = unmarshallEhrExtractElement(
            "interpretation_low_observation_example.xml");
        var observationStatement = getObservationStatement(ehrExtract);

        var observation = observationMapper.mapToObservation(ehrExtract, observationStatement);

        assertInterpretation(observation.getInterpretation(), "Low Text", "L", "Low");
    }

    @Test
    public void mapObservationWithInterpretationAbnormal() {
        var ehrExtract = unmarshallEhrExtractElement(
            "interpretation_abnormal_observation_example.xml");
        var observationStatement = getObservationStatement(ehrExtract);

        var observation = observationMapper.mapToObservation(ehrExtract, observationStatement);

        assertInterpretation(observation.getInterpretation(), "Abnormal Text", "A", "Abnormal");
    }

    @Test
    public void mapObservationWithMultiplePertinentInformation() {
        var ehrExtract = unmarshallEhrExtractElement(
            "multiple_pertinent_information_observation_example.xml");
        var observationStatement = getObservationStatement(ehrExtract);

        var observation = observationMapper.mapToObservation(ehrExtract, observationStatement);

        assertThat(observation.getComment()).isEqualTo("Test text 1 Test text 2");
    }

    @Test
    public void mapObservationWithMultipleReferenceRanges() {
        var ehrExtract = unmarshallEhrExtractElement(
            "multiple_reference_range_observation_example.xml");
        var observationStatement = getObservationStatement(ehrExtract);

        var observation = observationMapper.mapToObservation(ehrExtract, observationStatement);

        assertThat(observation.getReferenceRange().get(0).getText()).isEqualTo("Test Range 1");
        assertThat(observation.getReferenceRange().get(0).getLow().getValue().toString()).isEqualTo("10");
        assertThat(observation.getReferenceRange().get(0).getHigh().getValue().toString()).isEqualTo("12");
        assertThat(observation.getReferenceRange().get(1).getText()).isEqualTo("Test Range 2");
        assertThat(observation.getReferenceRange().get(1).getLow().getValue().toString()).isEqualTo("20");
        assertThat(observation.getReferenceRange().get(1).getHigh().getValue().toString()).isEqualTo("22");
    }
}
