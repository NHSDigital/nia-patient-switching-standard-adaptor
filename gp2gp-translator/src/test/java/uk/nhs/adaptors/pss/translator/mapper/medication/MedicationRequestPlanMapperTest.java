package uk.nhs.adaptors.pss.translator.mapper.medication;

import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestStatus.COMPLETED;
import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestStatus.STOPPED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestStatus.ACTIVE;
import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestIntent.PLAN;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.UnsignedIntType;
import org.hl7.v3.RCMRMT030101UKEhrExtract;
import org.hl7.v3.RCMRMT030101UKComponent2;
import org.hl7.v3.RCMRMT030101UKComponent;
import org.hl7.v3.RCMRMT030101UKComponent3;
import org.hl7.v3.RCMRMT030101UKComponent4;
import org.hl7.v3.RCMRMT030101UKEhrComposition;
import org.hl7.v3.RCMRMT030101UKEhrFolder;
import org.hl7.v3.RCMRMT030101UKMedicationStatement;
import org.hl7.v3.RCMRMT030101UKAuthorise;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@ExtendWith(MockitoExtension.class)
public class MedicationRequestPlanMapperTest {

    private static final String EHR_EXTRACT_WRAPPER = """
        <EhrExtract xmlns="urn:hl7-org:v3">
            <component>
                <ehrFolder>
                    <component>
                        <ehrComposition>
                            <component>
                                {{MedicationStatement}}
                            </component>
                        </ehrComposition>
                    </component>
                </ehrFolder>
            </component>
        </EhrExtract>
        """;

    private static final String PRACTISE_CODE = "TESTPRACTISECODE";
    private static final String MEDICATION_ID = "MEDICATION_ID";
    private static final String TEST_ID = "TEST_ID";
    private static final String TAKE_ONE_DAILY = "One To Be Taken Each Day";
    private static final String AVAILABILITY_TIME = "20060426";
    private static final String REPEATS_ISSUED_URL = "numberOfRepeatPrescriptionsIssued";
    private static final String REPEATS_ALLOWED_URL = "numberOfRepeatPrescriptionsAllowed";
    private static final String REPEATS_EXPIRY_DATE_URL = "authorisationExpiryDate";
    private static final String REPEAT_INFO_URL =
        "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-MedicationRepeatInformation-1";
    private static final String MEDICATION_STATUS_REASON_URL =
        "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-MedicationStatusReason-1";
    private static final String PRESCRIPTION_TYPE_URL =
        "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-PrescriptionType-1";
    private static final String DEFAULT_STATUS_REASON = "No information available";

    private static final int TWO = 2;
    private static final int SIX = 6;
    private static final int TWENTY_EIGHT = 28;
    public static final String STATUS_REASON = "statusReason";

    @Mock
    private MedicationMapper medicationMapper;

    @InjectMocks
    private MedicationRequestPlanMapper medicationRequestPlanMapper;

    @BeforeEach
    public void setup() {
        when(medicationMapper.extractMedicationReference(any()))
            .thenReturn(Optional.of(new Reference(new IdType(ResourceType.Medication.name(), MEDICATION_ID))));
    }

    @Test
    public void When_MappingAuthoriseResourceWithAllOptionals_Expect_AllFieldsToBeMappedCorrectly() {
        var medicationStatementXml = """
            <MedicationStatement xmlns="urn:hl7-org:v3" classCode="SBADM" moodCode="INT">
                <id root="B4D70A6D-2EE4-41B6-B1FB-F9F0AD84C503"/>
                <component typeCode="COMP">
                    <ehrSupplyAuthorise classCode="SPLY" moodCode="INT">
                        <id root="TEST_ID"/>
                        <effectiveTime>
                            <high value="20060427"/>
                        </effectiveTime>
                        <availabilityTime value="20100114"/>
                        <repeatNumber value="6"/>
                        <quantity value="28" unit="1" />
                        <predecessor typeCode="SUCC">
                            <priorMedicationRef classCode="SBADM" moodCode="INT">
                                <id root="TEST_ID"/>
                            </priorMedicationRef>
                        </predecessor>
                        <pertinentInformation typeCode="PERT">
                            <pertinentSupplyAnnotation classCode="OBS" moodCode="EVN">
                                <text>Pharmacy Text: Repeat Dispensing Pharmacy Note. 1</text>
                            </pertinentSupplyAnnotation>
                        </pertinentInformation>
                        <pertinentInformation typeCode="PERT">
                            <pertinentSupplyAnnotation classCode="OBS" moodCode="EVN">
                                <text>Pharmacy Text: Repeat Dispensing Pharmacy Note. 2</text>
                            </pertinentSupplyAnnotation>
                        </pertinentInformation>
                    </ehrSupplyAuthorise>
                </component>
                <component typeCode="COMP">
                    <ehrSupplyPrescribe classCode="SPLY" moodCode="RQO">
                        <id root="9B4B797A-D674-4362-B666-2ADC8551EEDA"/>
                        <quantity value="1" unit="1" />
                        <inFulfillmentOf typeCode="FLFS">
                            <priorMedicationRef moodCode="INT">
                                <id root="TEST_ID"/>
                            </priorMedicationRef>
                        </inFulfillmentOf>
                    </ehrSupplyPrescribe>
                </component>
                <component typeCode="COMP">
                    <ehrSupplyDiscontinue classCode="SPLY" moodCode="RQO">
                        <id root="D0BF39CA-E656-4322-879F-83EE6E688053"/>
                        <availabilityTime value="20060426"/>
                        <reversalOf typeCode="REV">
                            <priorMedicationRef classCode="SBADM" moodCode="ORD">
                                <id root="TEST_ID"/>
                            </priorMedicationRef>
                        </reversalOf>
                        <pertinentInformation typeCode="PERT">
                            <pertinentSupplyAnnotation classCode="OBS" moodCode="EVN">
                                <text>Patient no longer requires these</text>
                            </pertinentSupplyAnnotation>
                        </pertinentInformation>
                    </ehrSupplyDiscontinue>
                </component>
                <pertinentInformation typeCode="PERT">
                    <pertinentMedicationDosage classCode="SBADM" moodCode="RMD">
                        <text>One To Be Taken Each Day</text>
                    </pertinentMedicationDosage>
                </pertinentInformation>
            </MedicationStatement>
            """;
        var medicationRequest = mapPlanMedicationRequest(medicationStatementXml);

        var repeatInformation = medicationRequest.getExtensionsByUrl(REPEAT_INFO_URL);
        assertThat(repeatInformation).hasSize(1);
        assertRepeatInformation(repeatInformation.get(0));

        var statusReason = medicationRequest.getExtensionsByUrl(MEDICATION_STATUS_REASON_URL);
        assertThat(statusReason).hasSize(1);
        assertStatusReasonInformation(statusReason.get(0));

        var prescriptionType = medicationRequest.getExtensionsByUrl(PRESCRIPTION_TYPE_URL);
        assertThat(prescriptionType).hasSize(1);

        var codeableConcept = (CodeableConcept) prescriptionType.get(0).getValue();
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("Repeat");

        assertThat(medicationRequest.getStatus()).isEqualTo(STOPPED);
        assertThat(medicationRequest.getIntent()).isEqualTo(PLAN);
        assertThat(medicationRequest.getMedicationReference().getReferenceElement().getIdPart()).isEqualTo(MEDICATION_ID);
        assertThat(medicationRequest.getNote()).hasSize(TWO);

        assertThat(medicationRequest.getDosageInstructionFirstRep().getText()).isEqualTo(TAKE_ONE_DAILY);
        assertThat(medicationRequest.getDispenseRequest().getQuantity().getValue().intValue()).isEqualTo(TWENTY_EIGHT);
        assertThat(medicationRequest.getDispenseRequest().getValidityPeriod().hasStart()).isTrue();
        assertThat(medicationRequest.getDispenseRequest().getValidityPeriod().hasEnd()).isTrue();
        assertThat(medicationRequest.getPriorPrescription().getReferenceElement().getIdPart()).isEqualTo(TEST_ID);
    }

    @Test
    public void When_MappingAuthoriseResourceWithNoEffectiveTime_Expect_NoExpiryDateExtensionAdded() {
        var medicationStatementXml = """
            <MedicationStatement xmlns="urn:hl7-org:v3" classCode="SBADM" moodCode="INT">
                <id root="B4D70A6D-2EE4-41B6-B1FB-F9F0AD84C503"/>
                <component typeCode="COMP">
                    <ehrSupplyAuthorise classCode="SPLY" moodCode="INT">
                        <id root="TEST_ID"/>
                    </ehrSupplyAuthorise>
                </component>
            </MedicationStatement>
            """;
        var medicationRequest = mapPlanMedicationRequest(medicationStatementXml);
        var repeatInformation = medicationRequest.getExtensionsByUrl(REPEAT_INFO_URL);
        assertThat(repeatInformation).hasSize(1);

        assertThat(repeatInformation.get(0).getExtensionsByUrl(REPEATS_EXPIRY_DATE_URL)).isEmpty();
    }

    @Test
    public void When_MappingAuthoriseResourceEffectiveTimeWithNullHighValue_Expect_NoExpiryDateExtensionAdded() {
        var medicationStatementXml = """
            <MedicationStatement xmlns="urn:hl7-org:v3" classCode="SBADM" moodCode="INT">
                <id root="B4D70A6D-2EE4-41B6-B1FB-F9F0AD84C503"/>
                <component typeCode="COMP">
                    <ehrSupplyAuthorise classCode="SPLY" moodCode="INT">
                        <id root="TEST_ID"/>
                        <effectiveTime>
                            <high nullFlavor="UNK" />
                        </effectiveTime>
                    </ehrSupplyAuthorise>
                </component>
            </MedicationStatement>
            """;
        var medicationRequest = mapPlanMedicationRequest(medicationStatementXml);
        final var repeatInformation = medicationRequest.getExtensionsByUrl(REPEAT_INFO_URL);
        assertThat(repeatInformation).hasSize(1);

        assertThat(repeatInformation.get(0).getExtensionsByUrl(REPEATS_EXPIRY_DATE_URL)).hasSize(0);
    }

    @Test
    public void When_MappingDiscontinueWithPertinentInformation_Expect_StatusReasonAdded() {
        var ehrSupplyDiscontinue = """
            <ehrSupplyDiscontinue classCode="SPLY" moodCode="RQO">
                <id root="D0BF39CA-E656-4322-879F-83EE6E688053"/>
                <availabilityTime value="20060426"/>
                <reversalOf typeCode="REV">
                    <priorMedicationRef classCode="SBADM" moodCode="ORD">
                        <id root="TEST_ID"/>
                    </priorMedicationRef>
                </reversalOf>
                <pertinentInformation typeCode="PERT">
                    <pertinentSupplyAnnotation classCode="OBS" moodCode="EVN">
                        <text>Patient no longer requires these</text>
                    </pertinentSupplyAnnotation>
                </pertinentInformation>
            </ehrSupplyDiscontinue>
            """;
        var medicationRequest = mapPlanMedicationRequest(medicationStatementFromEhrSupplyDiscontinue(ehrSupplyDiscontinue));

        assertStatusReasonIsEqualTo(medicationRequest, "Patient no longer requires these");
    }

    @Test
    public void When_MappingDiscontinueWithCodeDisplayAndMissingPertinentInformation_Expect_DefaultTextAddedAsReason() {
        var ehrSupplyDiscontinue = """
            <ehrSupplyDiscontinue classCode="SPLY" moodCode="RQO">
                <id root="D0BF39CA-E656-4322-879F-83EE6E688053"/>
                <code code="EMISDRUG_DISCONTINUATION"
                      codeSystem="2.16.840.1.113883.2.1.6.3"
                      displayName="Medication Course Ended"
                />
                <availabilityTime value="20060426"/>
                <reversalOf typeCode="REV">
                    <priorMedicationRef classCode="SBADM" moodCode="ORD">
                        <id root="TEST_ID"/>
                    </priorMedicationRef>
                </reversalOf>
            </ehrSupplyDiscontinue>
            """;
        var medicationRequest = mapPlanMedicationRequest(medicationStatementFromEhrSupplyDiscontinue(ehrSupplyDiscontinue));

        assertStatusReasonIsEqualTo(medicationRequest, DEFAULT_STATUS_REASON);
    }

    @Test
    public void When_MappingDiscontinue_With_NoPertinentInformationAndHasCodeOriginalText_Expect_OriginalTextAndDefaultText() {
        var ehrSupplyDiscontinue = """
            <ehrSupplyDiscontinue classCode="SPLY" moodCode="RQO">
                <id root="D0BF39CA-E656-4322-879F-83EE6E688053"/>
                <code code="EMISDRUG_DISCONTINUATION" codeSystem="2.16.840.1.113883.2.1.6.3">
                    <originalText>Ended</originalText>
                </code>
                <availabilityTime value="20060426"/>
                <reversalOf typeCode="REV">
                    <priorMedicationRef classCode="SBADM" moodCode="ORD">
                        <id root="TEST_ID"/>
                    </priorMedicationRef>
                </reversalOf>
            </ehrSupplyDiscontinue>
            """;
        var medicationRequest = mapPlanMedicationRequest(medicationStatementFromEhrSupplyDiscontinue(ehrSupplyDiscontinue));

        assertStatusReasonIsEqualTo(medicationRequest, "(Ended) " + DEFAULT_STATUS_REASON);
    }

    @Test
    public void When_MappingDiscontinue_With_MissingPertinentInformation_Expect_DefaultTextAddedAsReason() {
        var ehrSupplyDiscontinue = """
            <ehrSupplyDiscontinue classCode="SPLY" moodCode="RQO">
                <id root="D0BF39CA-E656-4322-879F-83EE6E688053"/>
                <availabilityTime value="20060426"/>
                <reversalOf typeCode="REV">
                    <priorMedicationRef classCode="SBADM" moodCode="ORD">
                        <id root="TEST_ID"/>
                    </priorMedicationRef>
                </reversalOf>
            </ehrSupplyDiscontinue>
            """;
        var medicationRequest = mapPlanMedicationRequest(medicationStatementFromEhrSupplyDiscontinue(ehrSupplyDiscontinue));

        assertStatusReasonIsEqualTo(medicationRequest, DEFAULT_STATUS_REASON);
    }

    private static Stream<Arguments> When_MappingDiscontinue_WithPertinentInformationAndOriginalText_Expect_StatusReasonIs() {
        return Stream.of(
            Arguments.of(Named.of("Different pertinent information and original text",
                "Patient no longer requires these"), "Ended", "(Ended) Patient no longer requires these"),
            Arguments.of(Named.of("Same pertinent information and original text doesn't duplicate",
                "Ended"), "Ended", "Ended"),
            Arguments.of(Named.of("Original text is a prefix of pertinent information doesn't duplicate",
                "Prescribing error, incorrect dosage"), "Prescribing error", "Prescribing error, incorrect dosage"),
            Arguments.of(Named.of("Different cases for pertinent information and original text",
                "PRESCRIBING ERROR, incorrect dosage"), "Prescribing error", "(Prescribing error) PRESCRIBING ERROR, incorrect dosage"),
            Arguments.of(Named.of("Original text is in middle of pertinent information",
                "A, Prescribing error, B"), "Prescribing error", "(Prescribing error) A, Prescribing error, B")
        );
    }

    @ParameterizedTest @MethodSource void When_MappingDiscontinue_WithPertinentInformationAndOriginalText_Expect_StatusReasonIs(
        String pertinentInformationText, String originalText, String expectedReason
    ) {
        var ehrSupplyDiscontinue = """
            <ehrSupplyDiscontinue classCode="SPLY" moodCode="RQO">
                <id root="D0BF39CA-E656-4322-879F-83EE6E688053"/>
                <code code="EMISDRUG_DISCONTINUATION" codeSystem="2.16.840.1.113883.2.1.6.3">
                    <originalText>""" + originalText + "</originalText>" + """
                </code>
                <availabilityTime value="20060426"/>
                <reversalOf typeCode="REV">
                    <priorMedicationRef classCode="SBADM" moodCode="ORD">
                        <id root="TEST_ID"/>
                    </priorMedicationRef>
                </reversalOf>
                <pertinentInformation typeCode="PERT">
                    <pertinentSupplyAnnotation classCode="OBS" moodCode="EVN">
                        <text>""" + pertinentInformationText + "</text>" + """
                    </pertinentSupplyAnnotation>
                </pertinentInformation>
            </ehrSupplyDiscontinue>
            """;
        var medicationRequest = mapPlanMedicationRequest(medicationStatementFromEhrSupplyDiscontinue(ehrSupplyDiscontinue));

        assertStatusReasonIsEqualTo(medicationRequest, expectedReason);
    }

    @Test
    public void When_MappingAuthoriseResource_WithActiveStatusAndNoDiscontinue_Expect_ActiveStatus() {
        var medicationStatementXml = """
            <MedicationStatement xmlns="urn:hl7-org:v3" classCode="SBADM" moodCode="INT">
                <id root="B4D70A6D-2EE4-41B6-B1FB-F9F0AD84C503"/>
                <component typeCode="COMP">
                    <ehrSupplyAuthorise classCode="SPLY" moodCode="INT">
                        <id root="TEST_ID"/>
                        <statusCode code="ACTIVE"/>
                    </ehrSupplyAuthorise>
                </component>
            </MedicationStatement>
            """;
        var medicationRequest = mapPlanMedicationRequest(medicationStatementXml);

        assertThat(medicationRequest.getStatus()).isEqualTo(ACTIVE);
    }

    @Test
    public void When_MappingAuthoriseResource_WithCompleteStatusAndNoDiscontinue_Expect_CompletedStatus() {
        var medicationStatementXml = """
            <MedicationStatement xmlns="urn:hl7-org:v3" classCode="SBADM" moodCode="INT">
                <id root="B4D70A6D-2EE4-41B6-B1FB-F9F0AD84C503"/>
                <component typeCode="COMP">
                    <ehrSupplyAuthorise classCode="SPLY" moodCode="INT">
                        <id root="TEST_ID"/>
                        <statusCode code="COMPLETE"/>
                    </ehrSupplyAuthorise>
                </component>
            </MedicationStatement>
            """;
        var medicationRequest = mapPlanMedicationRequest(medicationStatementXml);

        assertThat(medicationRequest.getStatus()).isEqualTo(COMPLETED);
    }

    @Test
    public void When_MappingAuthoriseResource_With_NoDiscontinue_Expect_NoStatusReasonExtension() {
        var medicationStatementXml = """
            <MedicationStatement xmlns="urn:hl7-org:v3" classCode="SBADM" moodCode="INT">
                <id root="B4D70A6D-2EE4-41B6-B1FB-F9F0AD84C503"/>
                <component typeCode="COMP">
                    <ehrSupplyAuthorise classCode="SPLY" moodCode="INT">
                        <id root="TEST_ID"/>
                        <statusCode code="COMPLETE"/>
                    </ehrSupplyAuthorise>
                </component>
            </MedicationStatement>
            """;
        var medicationRequest = mapPlanMedicationRequest(medicationStatementXml);
        var statusExt = medicationRequest.getExtensionsByUrl(MEDICATION_STATUS_REASON_URL);

        assertThat(statusExt).isEmpty();
    }

    @Test
    public void When_MappingDiscontinue_With_UnknownDate_Expect_DiscontinueIgnored() {
        var medicationStatementXml = """
            <MedicationStatement xmlns="urn:hl7-org:v3" classCode="SBADM" moodCode="INT">
                <id root="B4D70A6D-2EE4-41B6-B1FB-F9F0AD84C503"/>
                <component typeCode="COMP">
                    <ehrSupplyAuthorise classCode="SPLY" moodCode="INT">
                        <id root="TEST_ID"/>
                        <statusCode code="COMPLETE"/>
                    </ehrSupplyAuthorise>
                </component>
                <component typeCode="COMP">
                    <ehrSupplyDiscontinue classCode="SPLY" moodCode="RQO">
                        <id root="D0BF39CA-E656-4322-879F-83EE6E688053"/>
                        <availabilityTime nullFlavor="UNK"/>
                        <reversalOf typeCode="REV">
                            <priorMedicationRef classCode="SBADM" moodCode="ORD">
                                <id root="TEST_ID"/>
                            </priorMedicationRef>
                        </reversalOf>
                    </ehrSupplyDiscontinue>
                </component>
            </MedicationStatement>
            """;
        var medicationRequest = mapPlanMedicationRequest(medicationStatementXml);
        var statusExt = medicationRequest.getExtensionsByUrl(MEDICATION_STATUS_REASON_URL);

        assertThat(medicationRequest.getStatus()).isEqualTo(COMPLETED);
        assertThat(statusExt).isEmpty();
    }

    private MedicationRequest mapPlanMedicationRequest(String medicationStatementXml) {
        var ehrExtract = unmarshallEhrExtractFromMedicationRequestXml(medicationStatementXml);
        var medicationStatement = extractMedicationStatement(ehrExtract);
        var supplyAuthorise = extractSupplyAuthorise(medicationStatement);

        return medicationRequestPlanMapper.mapToPlanMedicationRequest(
            ehrExtract,
            medicationStatement,
            supplyAuthorise,
            PRACTISE_CODE
        );
    }

    private RCMRMT030101UKAuthorise extractSupplyAuthorise(RCMRMT030101UKMedicationStatement medicationStatement) {
        return medicationStatement
            .getComponent()
            .stream()
            .map(RCMRMT030101UKComponent2::getEhrSupplyAuthorise)
            .findAny().orElseThrow();
    }

    private void assertStatusReasonInformation(Extension extension) {
        var changeDate = extension.getExtensionsByUrl("statusChangeDate").get(0);
        var changeDateValue = (DateTimeType) changeDate.getValue();
        assertThat(changeDateValue.getValue()).isEqualTo(DateFormatUtil.parseToDateTimeType(AVAILABILITY_TIME).getValue());

        var statusReason = extension.getExtensionsByUrl(STATUS_REASON).get(0);
        assertThat(statusReason.hasValue()).isTrue();
    }

    private void assertRepeatInformation(Extension extension) {
        var repeatsAllowed = extension.getExtensionsByUrl(REPEATS_ALLOWED_URL);
        assertThat(repeatsAllowed).hasSize(1);
        assertThat(((UnsignedIntType) repeatsAllowed.get(0).getValue()).getValue()).isEqualTo(new UnsignedIntType(SIX).getValue());

        var repeatsIssued = extension.getExtensionsByUrl(REPEATS_ISSUED_URL);
        assertThat(repeatsIssued).hasSize(1);
        assertThat(((UnsignedIntType) repeatsIssued.get(0).getValue()).getValue()).isEqualTo(new UnsignedIntType(1).getValue());

        var expiryDate = extension.getExtensionsByUrl(REPEATS_EXPIRY_DATE_URL);
        assertThat(expiryDate).hasSize(1);
        var date = expiryDate.get(0).getValue().toString();
        assertThat(date).isEqualTo(DateFormatUtil.parseToDateTimeType("20060427").toString());
    }

    private static void assertStatusReasonIsEqualTo(MedicationRequest medicationRequest, String expectedReason) {
        var statusExt = medicationRequest.getExtensionsByUrl(MEDICATION_STATUS_REASON_URL);
        assertThat(statusExt).hasSize(1);

        assertThat(statusExt.get(0).getExtensionsByUrl(STATUS_REASON)).usingRecursiveComparison().isEqualTo(List.of(
                new Extension(STATUS_REASON, new CodeableConcept().setText(expectedReason))
        ));
    }

    private RCMRMT030101UKMedicationStatement extractMedicationStatement(RCMRMT030101UKEhrExtract ehrExtract) {
        return ehrExtract
            .getComponent()
            .stream()
            .map(RCMRMT030101UKComponent::getEhrFolder)
            .map(RCMRMT030101UKEhrFolder::getComponent)
            .flatMap(List::stream)
            .map(RCMRMT030101UKComponent3::getEhrComposition)
            .map(RCMRMT030101UKEhrComposition::getComponent)
            .flatMap(List::stream)
            .map(RCMRMT030101UKComponent4::getMedicationStatement)
            .findFirst()
            .orElseThrow();
    }

    @SneakyThrows
    private RCMRMT030101UKEhrExtract unmarshallEhrExtractFromMedicationRequestXml(String medicationRequestXml) {
        var ehrExtractXml = EHR_EXTRACT_WRAPPER.replace("{{MedicationStatement}}", medicationRequestXml);
        return unmarshallString(ehrExtractXml, RCMRMT030101UKEhrExtract.class);
    }

    private static @NotNull String medicationStatementFromEhrSupplyDiscontinue(String ehrSupplyDiscontinue) {
        return """
            <MedicationStatement xmlns="urn:hl7-org:v3" classCode="SBADM" moodCode="INT">
                <id root="B4D70A6D-2EE4-41B6-B1FB-F9F0AD84C503"/>
                <component typeCode="COMP">
                    <ehrSupplyAuthorise classCode="SPLY" moodCode="INT">
                        <id root="TEST_ID"/>
                    </ehrSupplyAuthorise>
                </component>
                <component typeCode="COMP">""" + ehrSupplyDiscontinue + """
                </component>
            </MedicationStatement>
            """;
    }
}
