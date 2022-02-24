package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
public class BloodPressureMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/BloodPressure/";
    private static final String EXAMPLE_ID = "FE739904-2AAB-4B3F-9718-84BE019FD483";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Observation-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String CODING_DISPLAY_MOCK = "Test Display";
    private static final String EFFECTIVE_EXAMPLE = "2006-04-25T16:30:00.000+00:00";
    private static final String ISSUED_EXAMPLE = "2020-01-01T01:01:01.000+00:00";
    private static final String PPRF_PARTICIPANT_ID = "Practitioner/5DE8CDDA-866F-4CD9-9BB3-527A86DD49A9";
    private static final String COMMENT_EXAMPLE_1 = "Systolic Note: Test systolic pressure text "
        + "Diastolic Note: Test diastolic pressure text BP Note: Systolic Measurement Absent: Unknown";
    private static final String COMMENT_EXAMPLE_2 = "Systolic Note: Test systolic pressure text";
    private static final String COMMENT_EXAMPLE_3 = "Diastolic Note: Test diastolic pressure text";
    private static final String COMMENT_EXAMPLE_4 = "BP Note: Systolic Measurement Absent: Unknown";
    private static final String COMPONENT_1_VALUE_QUANTITY_VALUE = "80";
    private static final String COMPONENT_1_INTERPRETATION_TEXT = "High Text";
    private static final String COMPONENT_1_REFERENCE_RANGE_TEXT = "Test Range 1";
    private static final String COMPONENT_2_VALUE_QUANTITY_VALUE = "90";
    private static final String COMPONENT_2_INTERPRETATION_TEXT = "Low Text";
    private static final String COMPONENT_2_REFERENCE_RANGE_TEXT = "Test Range 2";

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
    private BloodPressureMapper bloodPressureMapper;

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtractElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }

    private void assertFixedValues(Observation bloodPressure) {
        assertThat(bloodPressure.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThat(bloodPressure.getIdentifier().get(0).getSystem()).isEqualTo(IDENTIFIER_SYSTEM);
        assertThat(bloodPressure.getIdentifier().get(0).getValue()).isEqualTo(EXAMPLE_ID);
        assertThat(bloodPressure.getStatus()).isEqualTo(Observation.ObservationStatus.FINAL);
    }

    @Test
    public void mapBloodPressureObservationWithValidData() {
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(CODEABLE_CONCEPT);

        var ehrExtract = unmarshallEhrExtractElement("full_valid_data_bp_example.xml");

        var bloodPressure = bloodPressureMapper.mapBloodPressure(ehrExtract, patient, ENCOUNTER_LIST).get(0);

        assertFixedValues(bloodPressure);
        assertThat(bloodPressure.getId()).isEqualTo(EXAMPLE_ID);
        assertThat(bloodPressure.getCode().getCodingFirstRep().getDisplay()).isEqualTo(CODING_DISPLAY_MOCK);
        assertThat(bloodPressure.hasSubject()).isTrue();
        assertThat(bloodPressure.getIssuedElement().asStringValue()).isEqualTo(ISSUED_EXAMPLE);
        assertThat(bloodPressure.getEffectiveDateTimeType().getValue()).isEqualTo(EFFECTIVE_EXAMPLE);
        assertThat(bloodPressure.getPerformer().get(0).getReference()).isEqualTo(PPRF_PARTICIPANT_ID);
        assertThat(bloodPressure.getComment()).isEqualTo(COMMENT_EXAMPLE_1);

        assertThat(bloodPressure.getComponent().get(0).getCode().getCodingFirstRep().getDisplay()).isEqualTo(CODING_DISPLAY_MOCK);
        assertThat(bloodPressure.getComponent().get(0).getValueQuantity().getValue().toString())
            .isEqualTo(COMPONENT_1_VALUE_QUANTITY_VALUE);
        assertThat(bloodPressure.getComponent().get(0).getInterpretation().getText()).isEqualTo(COMPONENT_1_INTERPRETATION_TEXT);
        assertThat(bloodPressure.getComponent().get(0).getReferenceRange().get(0).getText())
            .isEqualTo(COMPONENT_1_REFERENCE_RANGE_TEXT);

        assertThat(bloodPressure.getComponent().get(1).getCode().getCodingFirstRep().getDisplay()).isEqualTo(CODING_DISPLAY_MOCK);
        assertThat(bloodPressure.getComponent().get(1).getValueQuantity().getValue().toString())
            .isEqualTo(COMPONENT_2_VALUE_QUANTITY_VALUE);
        assertThat(bloodPressure.getComponent().get(1).getInterpretation().getText()).isEqualTo(COMPONENT_2_INTERPRETATION_TEXT);
        assertThat(bloodPressure.getComponent().get(1).getReferenceRange().get(0).getText()).isEqualTo(COMPONENT_2_REFERENCE_RANGE_TEXT);
    }

    @Test
    public void mapBloodPressureWithNoOptionalData() {
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(CODEABLE_CONCEPT);

        var ehrExtract = unmarshallEhrExtractElement("no_optional_data_bp_example.xml");

        var bloodPressure = bloodPressureMapper.mapBloodPressure(ehrExtract, patient, ENCOUNTER_LIST).get(0);

        assertFixedValues(bloodPressure);
        assertThat(bloodPressure.getId()).isEqualTo(EXAMPLE_ID);
        assertThat(bloodPressure.getCode().getCodingFirstRep().getDisplay()).isEqualTo(CODING_DISPLAY_MOCK);
        assertThat(bloodPressure.hasSubject()).isTrue();
        assertThat(bloodPressure.getIssuedElement().asStringValue()).isEqualTo(ISSUED_EXAMPLE);
        assertThat(bloodPressure.getPerformer().get(0).getReference()).isEqualTo(PPRF_PARTICIPANT_ID);

        assertThat(bloodPressure.getEffective()).isNull();
        assertThat(StringUtils.isEmpty(bloodPressure.getComment()));

        assertThat(bloodPressure.getComponent().get(0).getCode().getCodingFirstRep().getDisplay()).isEqualTo(CODING_DISPLAY_MOCK);
        assertThat(bloodPressure.getComponent().get(0).getValueQuantity()).isNull();
        assertThat(bloodPressure.getComponent().get(0).getInterpretation().getCoding().isEmpty());
        assertThat(bloodPressure.getComponent().get(0).getReferenceRange().isEmpty());

        assertThat(bloodPressure.getComponent().get(1).getCode().getCodingFirstRep().getDisplay()).isEqualTo(CODING_DISPLAY_MOCK);
        assertThat(bloodPressure.getComponent().get(1).getValueQuantity()).isNull();
        assertThat(bloodPressure.getComponent().get(1).getInterpretation().getCoding().isEmpty());
        assertThat(bloodPressure.getComponent().get(1).getReferenceRange().isEmpty());
    }

    @Test
    public void mapBloodPressureObservationWithCompositionIdMatchingEncounter() {
        var ehrExtract = unmarshallEhrExtractElement(
            "ehr_composition_id_matching_encounter_bp_example.xml");

        var bloodPressure = bloodPressureMapper.mapBloodPressure(ehrExtract, patient, ENCOUNTER_LIST).get(0);

        assertThat(bloodPressure.hasContext()).isTrue();
    }

    @Test
    public void mapBloodPressureObservationWithSystolicOnlyComment() {
        var ehrExtract = unmarshallEhrExtractElement(
            "systolic_comment_only_bp_example.xml");

        var bloodPressure = bloodPressureMapper.mapBloodPressure(ehrExtract, patient, ENCOUNTER_LIST).get(0);

        assertThat(bloodPressure.getComment()).isEqualTo(COMMENT_EXAMPLE_2);
    }

    @Test
    public void mapBloodPressureObservationWithDiastolicOnlyComment() {
        var ehrExtract = unmarshallEhrExtractElement(
            "diastolic_comment_only_blood_pressure_example.xml");

        var bloodPressure = bloodPressureMapper.mapBloodPressure(ehrExtract, patient, ENCOUNTER_LIST).get(0);

        assertThat(bloodPressure.getComment()).isEqualTo(COMMENT_EXAMPLE_3);
    }

    @Test
    public void mapBloodPressureObservationWithNarrativeStatementOnlyComment() {
        var ehrExtract = unmarshallEhrExtractElement(
            "narrative_statement_comment_only_bp_example.xml");

        var bloodPressure = bloodPressureMapper.mapBloodPressure(ehrExtract, patient, ENCOUNTER_LIST).get(0);

        assertThat(bloodPressure.getComment()).isEqualTo(COMMENT_EXAMPLE_4);
    }

    @Test
    public void mapBloodPressureObservationWithEffectiveDateTime() {
        var ehrExtract = unmarshallEhrExtractElement(
            "effective_date_time_using_effective_time_center_bp_example.xml");

        var bloodPressure = bloodPressureMapper.mapBloodPressure(ehrExtract, patient, ENCOUNTER_LIST).get(0);

        assertThat(bloodPressure.getEffective() instanceof DateTimeType);
        assertThat(bloodPressure.getEffectiveDateTimeType().getValueAsString()).isEqualTo("2006-04-25");
    }

    @Test
    public void mapBloodPressureObservationWithEffectivePeriod() {
        var ehrExtract = unmarshallEhrExtractElement(
            "effective_period_start_end_using_effective_time_bp_example.xml");

        var bloodPressure = bloodPressureMapper.mapBloodPressure(ehrExtract, patient, ENCOUNTER_LIST).get(0);

        assertThat(bloodPressure.getEffective() instanceof Period);
        assertThat(bloodPressure.getEffectivePeriod().getStartElement().getValueAsString()).isEqualTo("2006-04-25");
        assertThat(bloodPressure.getEffectivePeriod().getEndElement().getValueAsString()).isEqualTo("2006-04-26");
    }
}
