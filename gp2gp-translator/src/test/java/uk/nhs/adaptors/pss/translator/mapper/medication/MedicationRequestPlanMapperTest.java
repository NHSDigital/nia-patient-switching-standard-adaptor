package uk.nhs.adaptors.pss.translator.mapper.medication;

import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestStatus.COMPLETED;
import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestStatus.STOPPED;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestStatus.ACTIVE;
import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestIntent.PLAN;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.UnsignedIntType;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UKComponent2;
import org.hl7.v3.RCMRMT030101UKComponent;
import org.hl7.v3.RCMRMT030101UKComponent3;
import org.hl7.v3.RCMRMT030101UKComponent4;
import org.hl7.v3.RCMRMT030101UKEhrComposition;
import org.hl7.v3.RCMRMT030101UKEhrExtract;
import org.hl7.v3.RCMRMT030101UKEhrFolder;
import org.hl7.v3.RCMRMT030101UKMedicationStatement;
import org.hl7.v3.RCMRMT030101UKAuthorise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private static final int ONE = 1;
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
        var ehrExtract = unmarshallEhrExtractFromMedicationRequestXml(medicationStatementXml);
        var medicationStatement = extractMedicationStatement(ehrExtract);
        var supplyAuthorise = extractSupplyAuthorise(medicationStatement);

        var medicationRequest = medicationRequestPlanMapper.mapToPlanMedicationRequest(
                ehrExtract,
                medicationStatement,
                supplyAuthorise,
                PRACTISE_CODE
        );

        var repeatInformation = medicationRequest.getExtensionsByUrl(REPEAT_INFO_URL);
        assertThat(repeatInformation).hasSize(ONE);
        assertRepeatInformation(repeatInformation.get(0));

        var statusReason = medicationRequest.getExtensionsByUrl(MEDICATION_STATUS_REASON_URL);
        assertThat(statusReason).hasSize(ONE);
        assertStatusReasonInformation(statusReason.get(0));

        var prescriptionType = medicationRequest.getExtensionsByUrl(PRESCRIPTION_TYPE_URL);
        assertThat(prescriptionType).hasSize(ONE);

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
        var ehrExtract = unmarshallEhrExtractFromMedicationRequestXml(medicationStatementXml);
        var medicationStatement = extractMedicationStatement(ehrExtract);
        var supplyAuthorise = extractSupplyAuthorise(medicationStatement);

        var medicationRequest = medicationRequestPlanMapper.mapToPlanMedicationRequest(
            ehrExtract,
            medicationStatement,
            supplyAuthorise,
            PRACTISE_CODE
        );
        var repeatInformation = medicationRequest.getExtensionsByUrl(REPEAT_INFO_URL);
        var expiryDate = repeatInformation.get(0).getExtensionsByUrl(REPEATS_EXPIRY_DATE_URL);

        assertAll(
            () -> assertThat(repeatInformation).hasSize(ONE),
            () -> assertThat(expiryDate.size()).isZero()
        );
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
        var ehrExtract = unmarshallEhrExtractFromMedicationRequestXml(medicationStatementXml);
        var medicationStatement = extractMedicationStatement(ehrExtract);
        var supplyAuthorise = extractSupplyAuthorise(medicationStatement);

        var medicationRequest = medicationRequestPlanMapper.mapToPlanMedicationRequest(
            ehrExtract,
            medicationStatement,
            supplyAuthorise,
            PRACTISE_CODE
        );
        var repeatInformation = medicationRequest.getExtensionsByUrl(REPEAT_INFO_URL);
        var expiryDate = repeatInformation.get(0).getExtensionsByUrl(REPEATS_EXPIRY_DATE_URL);

        assertAll(
            () -> assertThat(repeatInformation).hasSize(ONE),
            () -> assertThat(expiryDate.size()).isZero()
        );
    }

    @Test
    public void When_MappingDiscontinueWithPertinentInformation_Expect_StatusReasonAdded() {
        var medicationStatementXml = """
            <MedicationStatement xmlns="urn:hl7-org:v3" classCode="SBADM" moodCode="INT">
                <id root="B4D70A6D-2EE4-41B6-B1FB-F9F0AD84C503"/>
                <component typeCode="COMP">
                    <ehrSupplyAuthorise classCode="SPLY" moodCode="INT">
                        <id root="TEST_ID"/>
                    </ehrSupplyAuthorise>
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
            </MedicationStatement>
            """;
        var ehrExtract = unmarshallEhrExtractFromMedicationRequestXml(medicationStatementXml);
        var medicationStatement = extractMedicationStatement(ehrExtract);
        var supplyAuthorise = extractSupplyAuthorise(medicationStatement);

        var medicationRequest = medicationRequestPlanMapper.mapToPlanMedicationRequest(
            ehrExtract,
            medicationStatement,
            supplyAuthorise,
            PRACTISE_CODE
        );
        var statusExt = medicationRequest.getExtensionsByUrl(MEDICATION_STATUS_REASON_URL);
        var statusReasonExt = statusExt.get(0).getExtensionsByUrl(STATUS_REASON);
        var statusReason = (CodeableConcept) statusReasonExt.get(0).getValue();

        assertAll(
            () -> assertThat(statusExt).hasSize(1),
            () -> assertThat(statusReasonExt).hasSize(1),
            () -> assertThat(statusReason.getText()).isEqualTo("Patient no longer requires these")
        );
    }

    @Test
    public void When_MappingDiscontinueWithCodeDisplayAndMissingPertinentInformation_Expect_DefaultTextAddedAsReason() {
        var medicationStatementXml = """
            <MedicationStatement xmlns="urn:hl7-org:v3" classCode="SBADM" moodCode="INT">
                <id root="B4D70A6D-2EE4-41B6-B1FB-F9F0AD84C503"/>
                <component typeCode="COMP">
                    <ehrSupplyAuthorise classCode="SPLY" moodCode="INT">
                        <id root="TEST_ID"/>
                    </ehrSupplyAuthorise>
                </component>
                <component typeCode="COMP">
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
                </component>
            </MedicationStatement>
            """;
        var ehrExtract = unmarshallEhrExtractFromMedicationRequestXml(medicationStatementXml);
        var medicationStatement = extractMedicationStatement(ehrExtract);
        var supplyAuthorise = extractSupplyAuthorise(medicationStatement);

        var medicationRequest = medicationRequestPlanMapper.mapToPlanMedicationRequest(
            ehrExtract,
            medicationStatement,
            supplyAuthorise,
            PRACTISE_CODE
        );
        var statusExt = medicationRequest.getExtensionsByUrl(MEDICATION_STATUS_REASON_URL);
        var statusReasonExt = statusExt.get(0).getExtensionsByUrl(STATUS_REASON);
        var statusReason = (CodeableConcept) statusReasonExt.get(0).getValue();

        assertAll(
            () -> assertThat(statusExt).hasSize(1),
            () -> assertThat(statusReasonExt).hasSize(1),
            () -> assertThat(statusReason.getText()).isEqualTo(DEFAULT_STATUS_REASON)
        );
    }

    @Test
    public void When_MappingDiscontinue_With_NoPertinentInformationAndHasCodeOriginalText_Expect_OriginalTextAndDefaultText() {
        var medicationStatementXml = """
            <MedicationStatement xmlns="urn:hl7-org:v3" classCode="SBADM" moodCode="INT">
                <id root="B4D70A6D-2EE4-41B6-B1FB-F9F0AD84C503"/>
                <component typeCode="COMP">
                    <ehrSupplyAuthorise classCode="SPLY" moodCode="INT">
                        <id root="TEST_ID"/>
                    </ehrSupplyAuthorise>
                </component>
                <component typeCode="COMP">
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
                </component>
            </MedicationStatement>
            """;
        var ehrExtract = unmarshallEhrExtractFromMedicationRequestXml(medicationStatementXml);
        var medicationStatement = extractMedicationStatement(ehrExtract);
        var supplyAuthorise = extractSupplyAuthorise(medicationStatement);

        var medicationRequest = medicationRequestPlanMapper.mapToPlanMedicationRequest(
            ehrExtract,
            medicationStatement,
            supplyAuthorise,
            PRACTISE_CODE
        );
        var statusExt = medicationRequest.getExtensionsByUrl(MEDICATION_STATUS_REASON_URL);
        var statusReasonExt = statusExt.get(0).getExtensionsByUrl(STATUS_REASON);
        var statusReason = (CodeableConcept) statusReasonExt.get(0).getValue();

        assertAll(
            () -> assertThat(statusExt).hasSize(1),
            () -> assertThat(statusReasonExt).hasSize(1),
            () -> assertThat(statusReason.getText()).isEqualTo("(Ended) " + DEFAULT_STATUS_REASON)
        );
    }

    @Test
    public void When_MappingDiscontinue_With_MissingPertinentInformation_Expect_DefaultTextAddedAsReason() {
        var medicationStatementXml = """
            <MedicationStatement xmlns="urn:hl7-org:v3" classCode="SBADM" moodCode="INT">
                <id root="B4D70A6D-2EE4-41B6-B1FB-F9F0AD84C503"/>
                <component typeCode="COMP">
                    <ehrSupplyAuthorise classCode="SPLY" moodCode="INT">
                        <id root="TEST_ID"/>
                    </ehrSupplyAuthorise>
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
                    </ehrSupplyDiscontinue>
                </component>
            </MedicationStatement>
            """;
        var ehrExtract = unmarshallEhrExtractFromMedicationRequestXml(medicationStatementXml);
        var medicationStatement = extractMedicationStatement(ehrExtract);
        var supplyAuthorise = extractSupplyAuthorise(medicationStatement);

        var medicationRequest = medicationRequestPlanMapper.mapToPlanMedicationRequest(
            ehrExtract,
            medicationStatement,
            supplyAuthorise,
            PRACTISE_CODE
        );
        var statusExt = medicationRequest.getExtensionsByUrl(MEDICATION_STATUS_REASON_URL);
        var statusReasonExt = statusExt.get(0).getExtensionsByUrl(STATUS_REASON);
        var statusReason = (CodeableConcept) statusReasonExt.get(0).getValue();

        assertAll(
            () -> assertThat(statusExt).hasSize(1),
            () -> assertThat(statusReasonExt).hasSize(1),
            () -> assertThat(statusReason.getText()).isEqualTo(DEFAULT_STATUS_REASON)
        );
    }

    @Test void When_MappingDiscontinue_With_CodingOriginalTextAndDifferentPertinentInformation_Expect_BothDisplayed() {
        var medicationStatementXml = """
            <MedicationStatement xmlns="urn:hl7-org:v3" classCode="SBADM" moodCode="INT">
                <id root="B4D70A6D-2EE4-41B6-B1FB-F9F0AD84C503"/>
                <component typeCode="COMP">
                    <ehrSupplyAuthorise classCode="SPLY" moodCode="INT">
                        <id root="TEST_ID"/>
                    </ehrSupplyAuthorise>
                </component>
                <component typeCode="COMP">
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
                        <pertinentInformation typeCode="PERT">
                            <pertinentSupplyAnnotation classCode="OBS" moodCode="EVN">
                                <text>Patient no longer requires these</text>
                            </pertinentSupplyAnnotation>
                        </pertinentInformation>
                    </ehrSupplyDiscontinue>
                </component>
            </MedicationStatement>
            """;
        var ehrExtract = unmarshallEhrExtractFromMedicationRequestXml(medicationStatementXml);
        var medicationStatement = extractMedicationStatement(ehrExtract);
        var supplyAuthorise = extractSupplyAuthorise(medicationStatement);

        var medicationRequest = medicationRequestPlanMapper.mapToPlanMedicationRequest(
            ehrExtract,
            medicationStatement,
            supplyAuthorise,
            PRACTISE_CODE
        );
        var statusExt = medicationRequest.getExtensionsByUrl(MEDICATION_STATUS_REASON_URL);
        var statusReasonExt = statusExt.get(0).getExtensionsByUrl(STATUS_REASON);
        var statusReason = (CodeableConcept) statusReasonExt.get(0).getValue();

        assertAll(
            () -> assertThat(statusExt).hasSize(1),
            () -> assertThat(statusReasonExt).hasSize(1),
            () -> assertThat(statusReason.getText()).isEqualTo("(Ended) Patient no longer requires these")
        );
    }

    @Test void When_MappingDiscontinue_With_CodingOriginalTextAndSameTextPertinentInformation_Expect_DisplayedOnce() {
        var medicationStatementXml = """
            <MedicationStatement xmlns="urn:hl7-org:v3" classCode="SBADM" moodCode="INT">
                <id root="B4D70A6D-2EE4-41B6-B1FB-F9F0AD84C503"/>
                <component typeCode="COMP">
                    <ehrSupplyAuthorise classCode="SPLY" moodCode="INT">
                        <id root="TEST_ID"/>
                    </ehrSupplyAuthorise>
                </component>
                <component typeCode="COMP">
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
                        <pertinentInformation typeCode="PERT">
                            <pertinentSupplyAnnotation classCode="OBS" moodCode="EVN">
                                <text>Ended</text>
                            </pertinentSupplyAnnotation>
                        </pertinentInformation>
                    </ehrSupplyDiscontinue>
                </component>
            </MedicationStatement>
            """;
        var ehrExtract = unmarshallEhrExtractFromMedicationRequestXml(medicationStatementXml);
        var medicationStatement = extractMedicationStatement(ehrExtract);
        var supplyAuthorise = extractSupplyAuthorise(medicationStatement);

        var medicationRequest = medicationRequestPlanMapper.mapToPlanMedicationRequest(
            ehrExtract,
            medicationStatement,
            supplyAuthorise,
            PRACTISE_CODE
        );
        var statusExt = medicationRequest.getExtensionsByUrl(MEDICATION_STATUS_REASON_URL);
        var statusReasonExt = statusExt.get(0).getExtensionsByUrl(STATUS_REASON);
        var statusReason = (CodeableConcept) statusReasonExt.get(0).getValue();

        assertAll(
            () -> assertThat(statusExt).hasSize(1),
            () -> assertThat(statusReasonExt).hasSize(1),
            () -> assertThat(statusReason.getText()).isEqualTo("Ended")
        );
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
        var ehrExtract = unmarshallEhrExtractFromMedicationRequestXml(medicationStatementXml);
        var medicationStatement = extractMedicationStatement(ehrExtract);
        var supplyAuthorise = extractSupplyAuthorise(medicationStatement);

        var medicationRequest = medicationRequestPlanMapper.mapToPlanMedicationRequest(
            ehrExtract,
            medicationStatement,
            supplyAuthorise,
            PRACTISE_CODE
        );

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
        var ehrExtract = unmarshallEhrExtractFromMedicationRequestXml(medicationStatementXml);
        var medicationStatement = extractMedicationStatement(ehrExtract);
        var supplyAuthorise = extractSupplyAuthorise(medicationStatement);

        var medicationRequest = medicationRequestPlanMapper.mapToPlanMedicationRequest(
            ehrExtract,
            medicationStatement,
            supplyAuthorise,
            PRACTISE_CODE
        );

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
        var ehrExtract = unmarshallEhrExtractFromMedicationRequestXml(medicationStatementXml);
        var medicationStatement = extractMedicationStatement(ehrExtract);
        var supplyAuthorise = extractSupplyAuthorise(medicationStatement);

        var medicationRequest = medicationRequestPlanMapper.mapToPlanMedicationRequest(
            ehrExtract,
            medicationStatement,
            supplyAuthorise,
            PRACTISE_CODE
        );
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
        var ehrExtract = unmarshallEhrExtractFromMedicationRequestXml(medicationStatementXml);
        var medicationStatement = extractMedicationStatement(ehrExtract);
        var supplyAuthorise = extractSupplyAuthorise(medicationStatement);

        var medicationRequest = medicationRequestPlanMapper.mapToPlanMedicationRequest(
            ehrExtract,
            medicationStatement,
            supplyAuthorise,
            PRACTISE_CODE
        );
        var statusExt = medicationRequest.getExtensionsByUrl(MEDICATION_STATUS_REASON_URL);

        assertThat(medicationRequest.getStatus()).isEqualTo(COMPLETED);
        assertThat(statusExt).isEmpty();
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
        assertThat(repeatsAllowed).hasSize(ONE);
        assertThat(((UnsignedIntType) repeatsAllowed.get(0).getValue()).getValue()).isEqualTo(new UnsignedIntType(SIX).getValue());

        var repeatsIssued = extension.getExtensionsByUrl(REPEATS_ISSUED_URL);
        assertThat(repeatsIssued).hasSize(ONE);
        assertThat(((UnsignedIntType) repeatsIssued.get(0).getValue()).getValue()).isEqualTo(new UnsignedIntType(ONE).getValue());

        var expiryDate = extension.getExtensionsByUrl(REPEATS_EXPIRY_DATE_URL);
        assertThat(expiryDate).hasSize(ONE);
        var date = expiryDate.get(0).getValue().toString();
        assertThat(date).isEqualTo(DateFormatUtil.parseToDateTimeType("20060427").toString());
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
        return unmarshallString(ehrExtractXml, RCMRMT030101UK04EhrExtract.class);
    }
}
