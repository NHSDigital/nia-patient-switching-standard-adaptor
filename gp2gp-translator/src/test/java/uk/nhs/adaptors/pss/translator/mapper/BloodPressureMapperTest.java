package uk.nhs.adaptors.pss.translator.mapper;

import static java.math.RoundingMode.DOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.MetaFactory.MetaType.META_WITHOUT_SECURITY;
import static uk.nhs.adaptors.pss.translator.MetaFactory.MetaType.META_WITH_SECURITY;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.v3.CV;
import org.hl7.v3.RCMRMT030101UKEhrExtract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.MetaFactory;
import uk.nhs.adaptors.pss.translator.TestUtility;
import uk.nhs.adaptors.pss.translator.service.ConfidentialityService;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.DegradedCodeableConcepts;

import static uk.nhs.adaptors.common.util.CodeableConceptUtils.createCodeableConcept;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class BloodPressureMapperTest {

    private static final String XML_RESOURCES_BASE = "xml/BloodPressure/";
    private static final String EXAMPLE_ID = "FE739904-2AAB-4B3F-9718-84BE019FD483";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Observation-1";
    private static final String PRACTISE_CODE = "TESTPRACTISECODE";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/TESTPRACTISECODE";
    private static final String CODING_DISPLAY_MOCK = "Test Display";
    private static final String EFFECTIVE_EXAMPLE = "20060425163000";
    private static final String ISSUED_EXAMPLE = "2020-01-01T01:01:01.000+00:00";
    private static final String PPRF_PARTICIPANT_ID = "Practitioner/5DE8CDDA-866F-4CD9-9BB3-527A86DD49A9";
    private static final String COMMENT_EXAMPLE_1 = "Systolic Note: Test systolic pressure text "
        + "Diastolic Note: Test diastolic pressure text BP Note: Systolic Measurement Absent: Unknown";
    private static final String COMMENT_EXAMPLE_2 = "Systolic Note: Test systolic pressure text";
    private static final String COMMENT_EXAMPLE_3 = "Diastolic Note: Test diastolic pressure text";
    private static final String COMMENT_EXAMPLE_4 = "BP Note: Systolic Measurement Absent: Unknown";
    private static final BigDecimal COMPONENT_1_VALUE_QUANTITY_VALUE_BASE = new BigDecimal(80);
    private static final BigDecimal COMPONENT_1_VALUE_QUANTITY_VALUE = COMPONENT_1_VALUE_QUANTITY_VALUE_BASE.
        setScale(3, DOWN);
    private static final String COMPONENT_1_INTERPRETATION_TEXT = "High Text";
    private static final String COMPONENT_1_REFERENCE_RANGE_TEXT = "Test Range 1";
    private static final BigDecimal COMPONENT_2_VALUE_QUANTITY_VALUE_BASE = new BigDecimal(90);
    private static final BigDecimal COMPONENT_2_VALUE_QUANTITY_VALUE = COMPONENT_2_VALUE_QUANTITY_VALUE_BASE
        .setScale(3, DOWN);
    private static final String COMPONENT_2_INTERPRETATION_TEXT = "Low Text";
    private static final String COMPONENT_2_REFERENCE_RANGE_TEXT = "Test Range 2";
    public static final String NOPAT_CODE = "NOPAT";
    public static final String NOPAT_URL_CODESYSTEM = "http://hl7.org/fhir/v3/ActCode";
    public static final String NOPAT_DISPLAY =
        "no disclosure to patient, family or caregivers without attending provider's authorization";
    public static final String NOPAT_OID_CODESYSTEM = "2.16.840.1.113883.4.642.3.47";

    private static final CodeableConcept CODEABLE_CONCEPT = createCodeableConcept(null, null, CODING_DISPLAY_MOCK);
    private static final List<Encounter> ENCOUNTER_LIST = List.of(
        (Encounter) new Encounter().setId("TEST_ID_MATCHING_ENCOUNTER")
    );

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @Mock
    private Patient patient;

    @Mock
    private ConfidentialityService confidentialityService;

    @InjectMocks
    private BloodPressureMapper bloodPressureMapper;

    @Captor
    private ArgumentCaptor<Optional<CV>> confidentialityCodesCaptor;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setupDefaultStubs() {
        lenient()
            .when(codeableConceptMapper.mapToCodeableConcept(any()))
            .thenReturn(CODEABLE_CONCEPT);
        lenient()
            .when(confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
                any(),
                any(Optional[].class)))
            .thenReturn(MetaFactory.getMetaFor(META_WITHOUT_SECURITY, META_PROFILE));
    }

    @Test
    public void mapBloodPressureObservationWithValidData() {
        var ehrExtract = unmarshallEhrExtractElement("full_valid_data_bp_example.xml");

        var bloodPressure = bloodPressureMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertFixedValues(bloodPressure);
        assertThat(bloodPressure.getId()).isEqualTo(EXAMPLE_ID);
        assertThat(bloodPressure.getComponent().get(0).getCode().getCodingFirstRep())
            .isEqualTo(DegradedCodeableConcepts.DEGRADED_OTHER);
        assertThat(bloodPressure.getComponent().get(0).getCode().getCoding().get(1).getDisplay())
            .isEqualTo(CODING_DISPLAY_MOCK);
        assertThat(bloodPressure.hasSubject()).isTrue();
        assertThat(bloodPressure.getIssuedElement().asStringValue()).isEqualTo(ISSUED_EXAMPLE);
        assertThat(bloodPressure.getEffective().toString()).isEqualTo(DateFormatUtil.parseToDateTimeType(EFFECTIVE_EXAMPLE).toString());
        assertThat(bloodPressure.getPerformer().get(0).getReference()).isEqualTo(PPRF_PARTICIPANT_ID);
        assertThat(bloodPressure.getComment()).isEqualTo(COMMENT_EXAMPLE_1);

        assertThat(bloodPressure.getComponent().get(0).getCode().getCodingFirstRep())
            .isEqualTo(DegradedCodeableConcepts.DEGRADED_OTHER);
        assertThat(bloodPressure.getComponent().get(0).getCode().getCoding().get(1).getDisplay())
            .isEqualTo(CODING_DISPLAY_MOCK);
        assertThat(bloodPressure.getComponent().get(0).getValueQuantity().getValue()).isEqualTo(COMPONENT_1_VALUE_QUANTITY_VALUE);
        assertThat(bloodPressure.getComponent().get(0).getInterpretation().getText()).isEqualTo(COMPONENT_1_INTERPRETATION_TEXT);
        assertThat(bloodPressure.getComponent().get(0).getReferenceRange().get(0).getText()).isEqualTo(COMPONENT_1_REFERENCE_RANGE_TEXT);

        assertThat(bloodPressure.getComponent().get(1).getCode().getCodingFirstRep())
            .isEqualTo(DegradedCodeableConcepts.DEGRADED_OTHER);
        assertThat(bloodPressure.getComponent().get(1).getCode().getCoding().get(1).getDisplay())
            .isEqualTo(CODING_DISPLAY_MOCK);
        assertThat(bloodPressure.getComponent().get(1).getValueQuantity().getValue()).isEqualTo(COMPONENT_2_VALUE_QUANTITY_VALUE);
        assertThat(bloodPressure.getComponent().get(1).getInterpretation().getText()).isEqualTo(COMPONENT_2_INTERPRETATION_TEXT);
        assertThat(bloodPressure.getComponent().get(1).getReferenceRange().get(0).getText()).isEqualTo(COMPONENT_2_REFERENCE_RANGE_TEXT);
    }

    @Test
    public void mapBloodPressureWithNoOptionalData() {
        var ehrExtract = unmarshallEhrExtractElement("no_optional_data_bp_example.xml");

        var bloodPressure = bloodPressureMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertFixedValues(bloodPressure);
        assertThat(bloodPressure.getId()).isEqualTo(EXAMPLE_ID);
        assertThat(bloodPressure.getCode().getCodingFirstRep()).isEqualTo(DegradedCodeableConcepts.DEGRADED_OTHER);
        assertThat(bloodPressure.getCode().getCoding().get(1).getDisplay()).isEqualTo(CODING_DISPLAY_MOCK);
        assertThat(bloodPressure.hasSubject()).isTrue();
        assertThat(bloodPressure.getIssuedElement().asStringValue()).isEqualTo(ISSUED_EXAMPLE);
        assertThat(bloodPressure.getPerformer().get(0).getReference()).isEqualTo(PPRF_PARTICIPANT_ID);

        assertThat(bloodPressure.getEffective()).isNull();
        assertThat(StringUtils.isEmpty(bloodPressure.getComment())).isTrue();

        assertThat(bloodPressure.getComponent().get(0).getCode().getCodingFirstRep())
            .isEqualTo(DegradedCodeableConcepts.DEGRADED_OTHER);
        assertThat(bloodPressure.getComponent().get(0).getCode().getCoding().get(1).getDisplay())
            .isEqualTo(CODING_DISPLAY_MOCK);
        assertThat(bloodPressure.getComponent().get(0).getValueQuantity()).isNull();
        assertThat(bloodPressure.getComponent().get(0).getInterpretation().getCoding()).isEmpty();
        assertThat(bloodPressure.getComponent().get(0).getReferenceRange()).isEmpty();

        assertThat(bloodPressure.getComponent().get(1).getCode().getCodingFirstRep())
            .isEqualTo(DegradedCodeableConcepts.DEGRADED_OTHER);
        assertThat(bloodPressure.getComponent().get(1).getCode().getCoding().get(1).getDisplay())
            .isEqualTo(CODING_DISPLAY_MOCK);
        assertThat(bloodPressure.getComponent().get(1).getValueQuantity()).isNull();
        assertThat(bloodPressure.getComponent().get(1).getInterpretation().getCoding()).isEmpty();
        assertThat(bloodPressure.getComponent().get(1).getReferenceRange()).isEmpty();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @ValueSource(strings = {
        "ehr_composition_containing_confidentiality_code",
        "battery_compound_statement_containing_confidentiality_code",
        "systolic_observation_containing_confidentiality_code",
        "diastolic_observation_containing_confidentiality_code"
    })
    public void When_MappingBloodPressureWithConfidentialityCodes_Expect_BloodPressureObservationContainsSecurityMeta(
        String inputXml
    ) {
        final var metaWithSecurity = MetaFactory.getMetaFor(META_WITH_SECURITY, META_PROFILE);
        when(
            confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
                eq("https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Observation-1"),
                confidentialityCodesCaptor.capture()
            )
        ).thenReturn(metaWithSecurity);
        final var ehrExtract = unmarshallEhrExtractElement(inputXml + ".xml");

        final var bloodPressure = bloodPressureMapper
            .mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE)
            .get(0);
        final var confidentialityCode = confidentialityCodesCaptor
            .getAllValues()
            .get(0);
        final var securityMeta = bloodPressure
            .getMeta()
            .getSecurity(NOPAT_URL_CODESYSTEM, NOPAT_CODE);

        assertThat(confidentialityCode).isPresent();
        assertAll(
            () -> assertThat(confidentialityCode.get())
                .usingRecursiveComparison()
                .isEqualTo(
                    TestUtility.createCv(NOPAT_CODE, NOPAT_OID_CODESYSTEM, NOPAT_DISPLAY)
                ),
            () -> assertThat(securityMeta.getDisplay())
                .isEqualTo(NOPAT_DISPLAY)
        );
    }

    @Test
    public void mapBloodPressureObservationWithCompositionIdMatchingEncounter() {
        var ehrExtract = unmarshallEhrExtractElement("ehr_composition_id_matching_encounter_bp_example.xml");

        var bloodPressure = bloodPressureMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertThat(bloodPressure.hasContext()).isTrue();
    }

    @Test
    public void mapBloodPressureObservationWithSystolicOnlyComment() {
        var ehrExtract = unmarshallEhrExtractElement("systolic_comment_only_bp_example.xml");

        var bloodPressure = bloodPressureMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertThat(bloodPressure.getComment()).isEqualTo(COMMENT_EXAMPLE_2);
    }

    @Test
    public void mapBloodPressureObservationWithDiastolicOnlyComment() {
        var ehrExtract = unmarshallEhrExtractElement("diastolic_comment_only_blood_pressure_example.xml");

        var bloodPressure = bloodPressureMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertThat(bloodPressure.getComment()).isEqualTo(COMMENT_EXAMPLE_3);
    }

    @Test
    public void mapBloodPressureObservationWithNarrativeStatementOnlyComment() {
        var ehrExtract = unmarshallEhrExtractElement("narrative_statement_comment_only_bp_example.xml");

        var bloodPressure = bloodPressureMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertThat(bloodPressure.getComment()).isEqualTo(COMMENT_EXAMPLE_4);
    }

    @Test
    public void mapBloodPressureObservationWithEffectiveDateTime() {
        var ehrExtract = unmarshallEhrExtractElement(
            "effective_date_time_using_effective_time_center_bp_example.xml");

        var bloodPressure = bloodPressureMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertThat(bloodPressure.getEffective()).isInstanceOf(DateTimeType.class);
        assertThat(bloodPressure.getEffectiveDateTimeType().getValueAsString()).isEqualTo("2006-04-25");
    }

    @Test
    public void mapBloodPressureObservationWithEffectivePeriod() {
        var ehrExtract = unmarshallEhrExtractElement(
            "effective_period_start_end_using_effective_time_bp_example.xml");

        var bloodPressure = bloodPressureMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertThat(bloodPressure.getEffective()).isInstanceOf(Period.class);
        assertThat(bloodPressure.getEffectivePeriod().getStartElement().getValueAsString()).isEqualTo("2006-04-25");
        assertThat(bloodPressure.getEffectivePeriod().getEndElement().getValueAsString()).isEqualTo("2006-04-26");
    }

    @Test
    public void nonConformantBloodPressureTripleNotMapped() {
        var ehrExtract = unmarshallEhrExtractElement("non-conformant-blood-pressure-triple-not-mapped.xml");

        var bloodPressures = bloodPressureMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE);

        assertThat(bloodPressures).isEmpty();
    }

    @Test
    public void mapBloodPressureWithNoSnomedCodeInCoding() {
        var ehrExtract = unmarshallEhrExtractElement("full_valid_data_bp_example.xml");

        var bloodPressure = bloodPressureMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertThat(bloodPressure.getCode().getCodingFirstRep())
            .isEqualTo(DegradedCodeableConcepts.DEGRADED_OTHER);
        assertThat(bloodPressure.getCode().getCoding().get(1).getDisplay())
            .isEqualTo(CODING_DISPLAY_MOCK);
        assertThat(bloodPressure.getComponent().get(0).getCode().getCodingFirstRep())
            .isEqualTo(DegradedCodeableConcepts.DEGRADED_OTHER);
        assertThat(bloodPressure.getComponent().get(0).getCode().getCoding().get(1).getDisplay())
            .isEqualTo(CODING_DISPLAY_MOCK);
        assertThat(bloodPressure.getComponent().get(1).getCode().getCodingFirstRep())
            .isEqualTo(DegradedCodeableConcepts.DEGRADED_OTHER);
        assertThat(bloodPressure.getComponent().get(1).getCode().getCoding().get(1).getDisplay())
            .isEqualTo(CODING_DISPLAY_MOCK);
    }

    @Test
    public void mapBloodPressureWithSnomedCodeInCoding() {
        var codeableConcept = createCodeableConcept("http://snomed.info/sct", "123456", "Display");
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(codeableConcept);

        var ehrExtract = unmarshallEhrExtractElement("full_valid_data_bp_example.xml");

        var bloodPressure = bloodPressureMapper.mapResources(ehrExtract, patient, ENCOUNTER_LIST, PRACTISE_CODE).get(0);

        assertAll(
            () -> assertEquals(codeableConcept, bloodPressure.getCode()),
            () -> assertEquals(codeableConcept, bloodPressure.getComponent().get(0).getCode()),
            () -> assertEquals(codeableConcept, bloodPressure.getComponent().get(1).getCode())
        );
    }

    private void assertFixedValues(Observation bloodPressure) {
        assertThat(bloodPressure.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThat(bloodPressure.getIdentifier().get(0).getSystem()).isEqualTo(IDENTIFIER_SYSTEM);
        assertThat(bloodPressure.getIdentifier().get(0).getValue()).isEqualTo(EXAMPLE_ID);
        assertThat(bloodPressure.getStatus()).isEqualTo(Observation.ObservationStatus.FINAL);
    }

    @SneakyThrows
    private RCMRMT030101UKEhrExtract unmarshallEhrExtractElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UKEhrExtract.class);
    }

}
