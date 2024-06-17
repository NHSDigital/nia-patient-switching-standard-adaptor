package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToInstantType;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.CodeableConcept;
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
import uk.nhs.adaptors.pss.translator.util.DatabaseImmunizationChecker;
import uk.nhs.adaptors.pss.translator.util.DegradedCodeableConcepts;

import static uk.nhs.adaptors.common.util.CodeableConceptUtils.createCodeableConcept;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

@ExtendWith(MockitoExtension.class)
public class ObservationMapperTest {

    private static final String EHR_EXTRACT_WRAPPER = """
        <EhrExtract xmlns="urn:hl7-org:v3" classCode="EXTRACT" moodCode="EVN">
            <component typeCode="COMP">
                <ehrFolder classCode="FOLDER" moodCode="EVN">
                    <component typeCode="COMP">
                        {{ehrComposition}}
                    </component>
                </ehrFolder>
            </component>
        </EhrExtract>
        """;

    private static final String XML_RESOURCES_BASE = "xml/Observation/";
    private static final String EXAMPLE_ID = "263B2A9F-0B1D-4697-943A-328F70E068DE";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Observation-1";
    private static final String PRACTISE_CODE = "TESTPRACTISECODE";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/TESTPRACTISECODE";
    private static final String INTERPRETATION_SYSTEM = "http://hl7.org/fhir/v2/0078";
    private static final String CODING_DISPLAY_MOCK = "Test Display";
    private static final String PPRF_PARTICIPANT_ID = "Practitioner/1230F602-6BB1-47E0-B2EC-39912A59787D";
    private static final String NEGATIVE_VALUE = "Negative";
    private static final String TEST_DISPLAY_VALUE = "Test display name";
    private static final BigDecimal QUANTITY_VALUE_BASE = new BigDecimal(27);
    private static final BigDecimal QUANTITY_VALUE = QUANTITY_VALUE_BASE.setScale(3);
    private static final BigDecimal REFERENCE_RANGE_LOW_VALUE = new BigDecimal(20);
    private static final BigDecimal REFERENCE_RANGE_HIGH_VALUE = new BigDecimal(22);
    private static final String SNOMED_SYSTEM = "http://snomed.info/sct";
    private static final List<Encounter> ENCOUNTER_LIST = List.of(
        (Encounter) new Encounter().setId("TEST_ID_MATCHING_ENCOUNTER")
    );
    private static final String ORIGINAL_TEXT = "Original Text";
    private static final String MINUS_ONE_ANNOTATION_TEXT = "minus 1 sequence comment";
    private static final String ZERO_ANNOTATION_TEXT = "zero sequence comment";
    private static final String PLUS_ONE_ANNOTATION_TEXT = "plus 1 sequence comment";
    private static final String NULL_FLAVOR_ANNOTATION_TEXT = "nullFlavor sequence comment";
    private static final String EPISODICITY_WITH_ORIGINAL_TEXT_NOTE_TEXT =
            "{Episodicity : code=255217005, displayName=First}";
    private static final String EPISODICITY_WITH_ORIGINAL_TEXT_NOTE_TEXT_WITH_ORIGINAL_TEXT =
            "{Episodicity : code=303350001, displayName=Ongoing, originalText=Review}";

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @Mock
    private DatabaseImmunizationChecker immunizationChecker;

    @Mock
    private Patient patient;

    @InjectMocks
    private ObservationMapper observationMapper;

    @Test
    public void mapObservationWithValidData() {
        var ehrExtract = unmarshallEhrExtractElement("full_valid_data_observation_example.xml");
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertFixedValues(observation);
        assertThat(observation.getId()).isEqualTo(EXAMPLE_ID);
        assertThat(observation.getEffective() instanceof DateTimeType).isTrue();
        assertThat(observation.getEffectiveDateTimeType().getValue()).isEqualTo("2019-07-08T13:35:00+00:00");
        assertThat(observation.getPerformer().get(0).getReference()).isEqualTo(PPRF_PARTICIPANT_ID);
        assertThat(observation.getValue() instanceof Quantity).isTrue();
        assertQuantity(observation.getValueQuantity(), QUANTITY_VALUE, "kilogram per square meter", "kg/m2");
        assertInterpretation(observation.getInterpretation(), "High", "H", "High");
        assertThat(observation.getComment()).isEqualTo("Subject: Uncle Test text 1");
        assertThat(observation.getReferenceRange().get(0).getText()).isEqualTo("Age and sex based");
        assertQuantity(observation.getReferenceRange().get(0).getLow(), REFERENCE_RANGE_LOW_VALUE, "liter", "L");
        assertQuantity(observation.getReferenceRange().get(0).getHigh(), REFERENCE_RANGE_HIGH_VALUE, "liter", "L");
        assertThat(observation.hasSubject()).isTrue();
    }

    /**
     * Testing episodicity comment renders without any existing comments being present.
     * Formatting should be similar to Allergy Intolerance.
     */
    @Test
    public void mapObservationWithEpisodicity() {
        var ehrExtract = unmarshallEhrExtractElement("data_observation_with_episodicity.xml");
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertFixedValues(observation);
        assertThat(observation.getComment()).isEqualTo(EPISODICITY_WITH_ORIGINAL_TEXT_NOTE_TEXT);
    }

    /**
     * Testing when episodicity contains an originalText element.
     */
    @Test
    public void mapObservationWithEpisodicityWithOriginalText() {
        var ehrExtract = unmarshallEhrExtractElement("data_observation_with_episodicity_with_original_text.xml");
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertFixedValues(observation);
        assertThat(observation.getComment()).isEqualTo(EPISODICITY_WITH_ORIGINAL_TEXT_NOTE_TEXT_WITH_ORIGINAL_TEXT);
    }
    @Test
    public void mapObservationWithEpisodicityWithOriginalTextAndExistingComment() {
        var ehrExtract = unmarshallEhrExtractElement("data_observation_with_episodicity_with_existing_comment.xml");
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertFixedValues(observation);
        assertThat(observation.getComment()).contains(EPISODICITY_WITH_ORIGINAL_TEXT_NOTE_TEXT_WITH_ORIGINAL_TEXT);
    }
    @Test
    public void mapObservationWhichIsBloodPressureWithoutBatteryOrBloodPressureTripleExpectObservationMapped() {
        var ehrExtract = unmarshallEhrExtractElement(
                "observation_is_blood_pressure_without_battery_or_triple.xml");
        var observations = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE);

        assertThat(observations).isNotEmpty();
    }

    @Test
    public void mappingObservationWhichIsBloodPressureExpectObservationNotMapped() {
        var ehrExtract = unmarshallEhrExtractElement(
                "observation_is_blood_pressure.xml");
        var observations = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE);

        assertThat(observations).isEmpty();
    }

    @Test
    public void mapObservationWithNoOptionalData() {
        var ehrExtract = unmarshallEhrExtractElement("no_optional_data_observation_example.xml");
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertFixedValues(observation);
        assertThat(observation.getId()).isEqualTo(EXAMPLE_ID);
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
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertThat(observation.getValue() instanceof StringType).isTrue();
        assertThat(observation.getValueStringType().getValue()).isEqualToIgnoringWhitespace(NEGATIVE_VALUE);
    }

    @Test
    public void mapObservationWithValueStringUsingValueTypeCVOriginalText() {
        var ehrExtract = unmarshallEhrExtractElement("value_cv_display_name_observation_example.xml");
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertThat(observation.getValue() instanceof StringType).isTrue();
        assertThat(observation.getValueStringType().getValue()).isEqualTo(TEST_DISPLAY_VALUE);
    }

    @Test
    public void mapObservationWithMultiplePertinentInformation() {
        var ehrExtract = unmarshallEhrExtractElement("multiple_pertinent_information_observation_example.xml");
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertThat(observation.getComment()).isEqualTo("Test text 1 Test text 2");
    }

    @Test
    public void mapObservationWithCompositionIdMatchingEncounter() {
        var ehrExtract = unmarshallEhrExtractElement("ehr_composition_id_matching_encounter_observation_example.xml");
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertThat(observation.hasContext()).isTrue();
    }

    @Test
    public void mapObservationWithEffectiveDateTime() {
        var ehrExtract = unmarshallEhrExtractElement(
            "effective_date_time_type_using_effective_time_center.xml");
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertThat(observation.getEffective() instanceof DateTimeType);
        assertThat(observation.getEffectiveDateTimeType().getValueAsString()).isEqualTo("2010-05-21");
    }

    @Test
    public void mapObservationWithEffectivePeriod() {
        var ehrExtract = unmarshallEhrExtractElement(
            "effective_period_start_end_using_effective_time_observation_example.xml");
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertThat(observation.getEffective() instanceof Period);
        assertThat(observation.getEffectivePeriod().getStartElement().getValueAsString()).isEqualTo("2010-05-21");
        assertThat(observation.getEffectivePeriod().getEndElement().getValueAsString()).isEqualTo("2010-05-22");
    }

    @Test
    public void handleEmptyComponentWithNoObservationStatement() {
        var ehrExtract = unmarshallEhrExtractElement("ehr_composition_with_no_observation_statements_example.xml");
        var observations = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE);

        assertThat(observations).isEmpty();
    }

    @Test
    public void When_MapObservation_With_Plus1SequenceComment_Expect_CommentMapped() {
        var ehrExtract = unmarshallEhrExtractElement(
            "sequence_comment_plus1__originalText_observation.xml");
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertThat(observation.getComment()).isEqualTo(PLUS_ONE_ANNOTATION_TEXT);
    }

    @Test
    public void When_MapObservation_With_Minus1SequenceComment_Expect_CommentAndOriginalTextMapped() {
        var ehrExtract = unmarshallEhrExtractElement(
            "sequence_comment_minus1_originalText_observation.xml");
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        var expectedComment = MINUS_ONE_ANNOTATION_TEXT + StringUtils.SPACE + ORIGINAL_TEXT;

        assertThat(observation.getComment()).isEqualTo(expectedComment);
    }

    @Test
    public void When_MapObservation_With_Minus1AndPlus1SequenceComment_Expect_CommentsAndOriginalTextMapped() {
        var ehrExtract = unmarshallEhrExtractElement(
            "sequence_comment_plus1_minus1_originalText_observation.xml");
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        var expectedComment = MINUS_ONE_ANNOTATION_TEXT + StringUtils.SPACE + ORIGINAL_TEXT + StringUtils.SPACE + PLUS_ONE_ANNOTATION_TEXT;

        assertThat(observation.getComment()).isEqualTo(expectedComment);
    }

    @Test
    public void When_MapObservation_With_ZeroSequenceAndPlus1SequenceComment_Expect_CommentsMapped() {
        var ehrExtract = unmarshallEhrExtractElement(
            "sequence_comment_plus1_zero_originalText_observation.xml");
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        var expectedComment = ZERO_ANNOTATION_TEXT + StringUtils.SPACE + PLUS_ONE_ANNOTATION_TEXT;

        assertThat(observation.getComment()).isEqualTo(expectedComment);
    }

    @Test
    public void When_MapObservation_With_ZeroSequenceComment_Expect_CommentMapped() {
        var ehrExtract = unmarshallEhrExtractElement(
            "sequence_comment_zero_originalText_observation.xml");
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertThat(observation.getComment()).isEqualTo(ZERO_ANNOTATION_TEXT);
    }

    @Test
    public void When_MapObservation_With_Minus1SequenceCommentAndNoOriginalText_Expect_CommentMapped() {
        var ehrExtract = unmarshallEhrExtractElement(
            "sequence_comment_minus1_observation.xml");
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertThat(observation.getComment()).isEqualTo(MINUS_ONE_ANNOTATION_TEXT);
    }

    @Test
    public void When_MapObservation_With_AllSequenceCommentsAndOriginalText_Expect_MappedInCorrectOrder() {
        var ehrExtract = unmarshallEhrExtractElement(
            "sequence_comment_all_observation.xml");
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        var expectedComment = MINUS_ONE_ANNOTATION_TEXT + StringUtils.SPACE + ORIGINAL_TEXT + StringUtils.SPACE + ZERO_ANNOTATION_TEXT
            + StringUtils.SPACE + PLUS_ONE_ANNOTATION_TEXT;

        assertThat(observation.getComment()).isEqualTo(expectedComment);
    }

    @Test
    public void When_MapObservation_WithNullFlavorSequenceComments_Expect_CommentsPostFixed() {
        var ehrExtract = unmarshallEhrExtractElement(
            "sequence_comment_minus1_nullFlavor_originalText.xml");
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        var expectedComment = MINUS_ONE_ANNOTATION_TEXT + StringUtils.SPACE + ORIGINAL_TEXT + StringUtils.SPACE
            + NULL_FLAVOR_ANNOTATION_TEXT;

        assertThat(observation.getComment()).isEqualTo(expectedComment);
    }

    @Test
    public void When_MapObservation_WithoutSnomedCodeInCode_Expect_DegradedCodeableConcept() {

        var codeableConcept = createCodeableConcept(null, "1.2.3.4.5", CODING_DISPLAY_MOCK);
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(codeableConcept);

        var ehrExtract = unmarshallEhrExtractElement("full_valid_data_observation_example.xml");
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertThat(observation.getCode().getCodingFirstRep())
            .isEqualTo(DegradedCodeableConcepts.DEGRADED_OTHER);
    }

    @Test
    public void When_MapObservation_WithSnomedCodeInCode_Expect_MappedWithoutDegrading() {

        var codeableConcept = createCodeableConcept(null, SNOMED_SYSTEM, CODING_DISPLAY_MOCK);
        lenient().when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(codeableConcept);

        var ehrExtract = unmarshallEhrExtractElement("full_valid_data_observation_example.xml");
        var observation = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertThat(observation.getCode().getCodingFirstRep().getDisplay())
            .isEqualTo(CODING_DISPLAY_MOCK);
        assertThat(observation.getCode().getCodingFirstRep().getSystem())
            .isEqualTo(SNOMED_SYSTEM);
    }

    @Test
    public void When_MappingObservationFromObservationStatementWithAvailabilityTime_Expect_IssuedUsesThisValue() {
        final var ehrCompositionXml = """
            <ehrComposition classCode="COMPOSITION" moodCode="EVN">
                <id root="CA4AD477-C33B-4FE7-8417-A242BB3D23AF"/>
                <author typeCode="AUT" contextControlCode="OP">
                    <time value="20200101010101"/>
                </author>
                <component typeCode="COMP">
                    <ObservationStatement classCode="OBS" moodCode="EVN">
                        <id root="263B2A9F-0B1D-4697-943A-328F70E068DE"/>
                        <availabilityTime value="20200716"/>
                    </ObservationStatement>
                </component>
            </ehrComposition>
            """;
        var ehrExtract = unmarshallEhrExtractFromEhrCompositionXml(ehrCompositionXml);

        var observations = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE);
        var observation = observations.get(0);

        assertThat(observation.getIssuedElement().asStringValue()).
            isEqualTo(parseToInstantType("20200716").asStringValue());
    }

    @Test
    public void When_MappingObservationFromObservationStatementWithoutAvailabilityTime_Expect_IssuedUsesAuthorTime() {
        final var ehrCompositionXml = """
            <ehrComposition classCode="COMPOSITION" moodCode="EVN">
                <id root="CA4AD477-C33B-4FE7-8417-A242BB3D23AF"/>
                <author typeCode="AUT" contextControlCode="OP">
                    <time value="20200101010101"/>
                </author>
                <component typeCode="COMP">
                    <ObservationStatement classCode="OBS" moodCode="EVN">
                        <id root="263B2A9F-0B1D-4697-943A-328F70E068DE"/>
                    </ObservationStatement>
                </component>
            </ehrComposition>
            """;
        var ehrExtract = unmarshallEhrExtractFromEhrCompositionXml(ehrCompositionXml);

        var observations = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE);
        var observation = observations.get(0);

        assertThat(observation.getIssuedElement().asStringValue()).
            isEqualTo(parseToInstantType("20200101010101").asStringValue());
    }

    @Test
    public void When_MappingObservationFromRequestStatementWithAvailabilityTime_Expect_IssuedUsesThisValue() {
        final var ehrCompositionXml = """
            <ehrComposition classCode="COMPOSITION" moodCode="EVN">
                <id root="CA4AD477-C33B-4FE7-8417-A242BB3D23AF"/>
                <component contextConductionInd="true" typeCode="COMP">
                    <RequestStatement classCode="OBS" moodCode="RQO">
                        <id root="8D61D723-D51D-44FB-B814-0EB69DB1D6F4"/>
                        <code code="8H62.00" codeSystem="2.16.840.1.113883.2.1.6.2" displayName="Referral to G.P.">
                            <qualifier codeSystem="2.16.840.1.113883.2.1.6.3" inverted="false">
                                <name code="RequestType" displayName="RequestType"/>
                                <value code="SelfReferral" displayName="SelfReferral"/>
                            </qualifier>
                        </code>
                        <availabilityTime value="20100119"/>
                        <priorityCode code="394848005" codeSystem="2.16.840.1.113883.2.1.3.2.4.15" displayName="Normal">
                            <originalText>Routine</originalText>
                        </priorityCode>
                    </RequestStatement>
                </component>
            </ehrComposition>
            """;
        var ehrExtract = unmarshallEhrExtractFromEhrCompositionXml(ehrCompositionXml);

        var observations = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE);
        var observation = observations.get(0);

        assertThat(observation.getIssuedElement().asStringValue()).
            isEqualTo(parseToInstantType("20100119").asStringValue());
    }

    @Test
    public void When_MappingObservationFromRequestStatementWithoutAvailabilityTime_Expect_IssuedUsesAuthorTime() {
        final var ehrCompositionXml = """
            <ehrComposition classCode="COMPOSITION" moodCode="EVN">
                <id root="CA4AD477-C33B-4FE7-8417-A242BB3D23AF"/>
                <author typeCode="AUT" contextControlCode="OP">
                    <time value="20200101010101"/>
                </author>
                <component contextConductionInd="true" typeCode="COMP">
                    <RequestStatement classCode="OBS" moodCode="RQO">
                        <id root="8D61D723-D51D-44FB-B814-0EB69DB1D6F4"/>
                        <code code="8H62.00" codeSystem="2.16.840.1.113883.2.1.6.2" displayName="Referral to G.P.">
                            <qualifier codeSystem="2.16.840.1.113883.2.1.6.3" inverted="false">
                                <name code="RequestType" displayName="RequestType"/>
                                <value code="SelfReferral" displayName="SelfReferral"/>
                            </qualifier>
                        </code>
                        <priorityCode code="394848005" codeSystem="2.16.840.1.113883.2.1.3.2.4.15" displayName="Normal">
                            <originalText>Routine</originalText>
                        </priorityCode>
                    </RequestStatement>
                </component>
            </ehrComposition>
            """;
        var ehrExtract = unmarshallEhrExtractFromEhrCompositionXml(ehrCompositionXml);

        var observations = observationMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE);
        var observation = observations.get(0);

        assertThat(observation.getIssuedElement().asStringValue()).
            isEqualTo(parseToInstantType("20200101010101").asStringValue());
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtractFromEhrCompositionXml(String ehrCompositionXml) {
        var ehrExtractXml = EHR_EXTRACT_WRAPPER.replace("{{ehrComposition}}", ehrCompositionXml);
        return unmarshallString(ehrExtractXml, RCMRMT030101UK04EhrExtract.class);
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

    private void assertQuantity(Quantity quantity, BigDecimal value, String unit, String code) {
        assertThat(quantity.getValue()).isEqualTo(value);
        assertThat(quantity.getUnit()).isEqualTo(unit);
        assertThat(quantity.getCode()).isEqualTo(code);
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtractElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}
