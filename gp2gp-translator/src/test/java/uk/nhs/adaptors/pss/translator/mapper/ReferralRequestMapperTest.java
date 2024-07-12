package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.common.util.CodeableConceptUtils.createCodeableConcept;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralRequestStatus;
import org.hl7.v3.RCMRMT030101UKEhrComposition;
import org.hl7.v3.RCMRMT030101UKEhrExtract;
import org.hl7.v3.RCMRMT030101UKRequestStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.util.DegradedCodeableConcepts;

@ExtendWith(MockitoExtension.class)
public class ReferralRequestMapperTest {

    private static final String XML_RESOURCES_BASE = "xml/RequestStatement/";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-ReferralRequest-1";
    private static final String PRACTISE_CODE = "TEST_PRACTISE_CODE";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/TEST_PRACTISE_CODE";
    private static final String EXAMPLE_ID = "B4303C92-4D1C-11E3-A2DD-010000000161";
    private static final String PRACTITIONER_ID = "Practitioner/58341512-03F3-4C8E-B41C-A8FCA3886BBB";
    private static final String EHR_COMPOSITION_PRACTITIONER2_ID = "Practitioner/B1AF3701-4D1C-11E3-9E6B-010000001205";
    private static final String CODING_DISPLAY = "Reason Code 1";
    private static final String ENCOUNTER_ID = "72A39454-299F-432E-993E-5A6232B4E099";
    private static final String PATIENT_ID = "AD99032E-F103-46C0-BA6D-659C7947ECAF";
    private static final String SNOMED_SYSTEM = "http://snomed.info/sct";
    private static final String REASON_CODE_1 = "183885007";
    private static final String UNEXPECTED_PRIORITY_DISPLAY = "Delayed priority";
    private static final String PRIORITY_NOTE_PREPENDAGE = "Priority: ";

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @InjectMocks
    private static ReferralRequestMapper referralRequestMapper;

    @Test
    public void mapReferralRequestWithReferralRequestAsOrganization() {

        var codeableConcept = createCodeableConcept(REASON_CODE_1, SNOMED_SYSTEM, CODING_DISPLAY);
        when(codeableConceptMapper.mapToCodeableConcept(any()))
            .thenReturn(codeableConcept);

        var ehrExtract = unmarshallEhrExtractElement("full_valid_data_with_referral_request.xml");

        var referralRequest = mapReferralRequest(ehrExtract,
                                                 ehrExtract.getComponent().get(0).getEhrFolder().getComponent().get(0).getEhrComposition(),
                                                 composition -> composition.getComponent().get(0).getRequestStatement());

        assertAll(
            () -> assertFixedValues(referralRequest),
            () -> assertEquals(EXAMPLE_ID, referralRequest.getId()),
            () -> assertThat(referralRequest.getAuthoredOn()).isEqualTo("2020-11-17T13:30:32:00"),
            () -> assertEquals(PRACTITIONER_ID, referralRequest.getRequester().getAgent().getReference()),
            () -> assertEquals("Organization/9C3AB881-FCDE-48EC-8028-37B20E0AE893",
                               referralRequest.getRecipient().get(0).getReference()),
            () -> assertEquals("Reason Code 1", referralRequest.getReasonCodeFirstRep().getCoding().get(0).getDisplay())
        );
    }

    @Test
    public void mapReferralRequestWithValidData() {

        var codeableConcept = createCodeableConcept(REASON_CODE_1, SNOMED_SYSTEM, CODING_DISPLAY);
        when(codeableConceptMapper.mapToCodeableConcept(any()))
                .thenReturn(codeableConcept);

        var ehrExtract = unmarshallEhrExtractElement("full_valid_data_example.xml");

        var referralRequest = mapReferralRequest(ehrExtract,
                                                 ehrExtract.getComponent().get(0).getEhrFolder().getComponent().get(0).getEhrComposition(),
                                                 composition -> composition.getComponent().get(0).getRequestStatement());

        assertAll(
                () -> assertFixedValues(referralRequest),
                () -> assertEquals(EXAMPLE_ID, referralRequest.getId()),
                () -> assertEquals("Priority: Routine", referralRequest.getNote().get(0).getText()),
                () -> assertEquals("Action Date: 2005-04-06", referralRequest.getNote().get(1).getText()),
                () -> assertEquals("Test request statement text\nNew line", referralRequest.getNote().get(2).getText()),
                () -> assertThat(referralRequest.getAuthoredOn()).isEqualTo("2010-01-01T12:30:00+00:00"),
                () -> assertEquals(PRACTITIONER_ID, referralRequest.getRequester().getAgent().getReference()),
                () -> assertEquals("Practitioner/B8CA3710-4D1C-11E3-9E6B-010000001205",
                                   referralRequest.getRecipient().get(0).getReference()),
                () -> assertEquals("Reason Code 1", referralRequest.getReasonCodeFirstRep().getCoding().get(0).getDisplay()),
                () -> assertEquals(ENCOUNTER_ID, referralRequest.getContext().getResource().getIdElement().getValue())
        );
    }

    @Test
    public void mapReferralRequestWithNoOptionalData() {
        var inputXml = """
                <ehrComposition xmlns="urn:hl7-org:v3" classCode="COMPOSITION" moodCode="EVN">
                    <id root="72A39454-299F-432E-993E-5A6232B4E099" />
                    <availabilityTime value="20190708143500"/>
                    <component typeCode="COMP" >
                        <RequestStatement classCode="OBS" moodCode="RQO">
                            <id root="B4303C92-4D1C-11E3-A2DD-010000000161"/>
                            <statusCode code="COMPLETE"/>
                        </RequestStatement>
                    </component>
                </ehrComposition>
                """;
        var ehrComposition = unmarshallStringToEhrCompositionElement(inputXml);

        var referralRequest = mapReferralRequest(null, ehrComposition,
            composition -> composition.getComponent().get(0).getRequestStatement());

        assertAll(
                () -> assertEquals(EXAMPLE_ID, referralRequest.getId()),
                () -> assertThat(referralRequest.getNote()).isEmpty(),
                () -> assertNull(referralRequest.getAuthoredOn()),
                () -> assertNull(referralRequest.getRequester().getAgent().getReference()),
                () -> assertThat(referralRequest.getRecipient()).isEmpty(),
                () -> assertThat(referralRequest.getReasonCode()).isEmpty()
        );
    }

    @Test
    public void mapReferralRequestWithNoReferencedEncounter() {
        var inputXml = """
                <ehrComposition xmlns="urn:hl7-org:v3" classCode="COMPOSITION" moodCode="EVN">
                    <id root="83A39454-299F-432E-993E-5A6232B4E099" />
                    <availabilityTime value="20190708143500"/>
                    <component typeCode="COMP" >
                        <RequestStatement classCode="OBS" moodCode="RQO">
                            <id root="B4303C92-4D1C-11E3-A2DD-010000000161"/>
                            <statusCode code="COMPLETE"/>
                        </RequestStatement>
                    </component>
                </ehrComposition>
                """;
        var ehrComposition = unmarshallStringToEhrCompositionElement(inputXml);

        ReferralRequest referralRequest = mapReferralRequest(null, ehrComposition,
            composition -> composition.getComponent().get(0).getRequestStatement());

        assertNull(referralRequest.getContext().getResource());
    }

    @Test
    public void mapReferralRequestWithRequesterPrfParticipant() {
        var inputXml = """
                <ehrComposition xmlns="urn:hl7-org:v3" classCode="COMPOSITION" moodCode="EVN">
                    <id root="72A39454-299F-432E-993E-5A6232B4E099" />
                    <availabilityTime value="20190708143500"/>
                    <Participant2 typeCode="PRF" contextControlCode="OP">
                        <agentRef classCode="AGNT">
                            <id root="B1AF3701-4D1C-11E3-9E6B-010000001205"/>
                        </agentRef>
                    </Participant2>
                    <component typeCode="COMP" >
                        <RequestStatement classCode="OBS" moodCode="RQO">
                            <id root="B4303C92-4D1C-11E3-A2DD-010000000161"/>
                            <statusCode code="COMPLETE"/>
                            <Participant typeCode="PRF" contextControlCode="OP">
                                <agentRef classCode="AGNT">
                                    <id root="58341512-03F3-4C8E-B41C-A8FCA3886BBB"/>
                                </agentRef>
                            </Participant>
                        </RequestStatement>
                    </component>
                </ehrComposition>
                """;
        var ehrComposition = unmarshallStringToEhrCompositionElement(inputXml);

        ReferralRequest referralRequest = mapReferralRequest(null, ehrComposition,
            composition -> composition.getComponent().get(0).getRequestStatement());

        assertAll(
                () -> assertEquals(EXAMPLE_ID, referralRequest.getId()),
                () -> assertThat(referralRequest.getNote()).isEmpty(),
                () -> assertNull(referralRequest.getAuthoredOn()),
                () -> assertEquals(PRACTITIONER_ID, referralRequest.getRequester().getAgent().getReference()),
                () -> assertThat(referralRequest.getRecipient()).isEmpty(),
                () -> assertThat(referralRequest.getReasonCode()).isEmpty()
        );
    }

    @Test
    public void mapReferralRequestWithRequesterEhrCompositionParticipant2() {
        var inputXml = """
                <ehrComposition xmlns="urn:hl7-org:v3" classCode="COMPOSITION" moodCode="EVN">
                    <id root="72A39454-299F-432E-993E-5A6232B4E099" />
                    <availabilityTime value="20190708143500"/>
                    <Participant2 typeCode="PRF" contextControlCode="OP">
                        <agentRef classCode="AGNT">
                            <id root="B1AF3701-4D1C-11E3-9E6B-010000001205"/>
                        </agentRef>
                    </Participant2>
                    <component typeCode="COMP" >
                        <RequestStatement classCode="OBS" moodCode="RQO">
                            <id root="B4303C92-4D1C-11E3-A2DD-010000000161"/>
                            <statusCode code="COMPLETE"/>
                            <Participant contextControlCode="OP">
                                <agentRef classCode="AGNT">
                                    <id root="58341512-03F3-4C8E-B41C-A8FCA3886BBB"/>
                                </agentRef>
                            </Participant>
                        </RequestStatement>
                    </component>
                </ehrComposition>
                """;
        var ehrComposition = unmarshallStringToEhrCompositionElement(inputXml);

        ReferralRequest referralRequest = mapReferralRequest(null, ehrComposition,
            composition -> composition.getComponent().get(0).getRequestStatement());

        assertAll(
                () -> assertEquals(EXAMPLE_ID, referralRequest.getId()),
                () -> assertThat(referralRequest.getNote()).isEmpty(),
                () -> assertNull(referralRequest.getAuthoredOn()),
                () -> assertEquals(EHR_COMPOSITION_PRACTITIONER2_ID, referralRequest.getRequester().getAgent().getReference()),
                () -> assertThat(referralRequest.getRecipient()).isEmpty(),
                () -> assertThat(referralRequest.getReasonCode()).isEmpty()
        );
    }

    @Test
    public void mapReferralRequestWithRecipientResponsiblePartyNoValidTypeCode() {
        var inputXml = """
            <EhrExtract xmlns="urn:hl7-org:v3" classCode="EXTRACT" moodCode="EVN">
                <component typeCode="COMP">
                    <ehrFolder classCode="FOLDER" moodCode="EVN">
                        <component typeCode="COMP">
                            <ehrComposition xmlns="urn:hl7-org:v3" classCode="COMPOSITION" moodCode="EVN">
                                 <id root="72A39454-299F-432E-993E-5A6232B4E099" />
                                 <availabilityTime value="20190708143500"/>
                                 <component typeCode="COMP" >
                                     <RequestStatement classCode="OBS" moodCode="RQO">
                                         <id root="B4303C92-4D1C-11E3-A2DD-010000000161"/>
                                         <statusCode code="COMPLETE"/>
                                         <responsibleParty typeCode="TEST">
                                             <agentRef classCode="AGNT">
                                                 <id root="B8CA3710-4D1C-11E3-9E6B-010000001205"/>
                                             </agentRef>
                                         </responsibleParty>
                                     </RequestStatement>
                                 </component>
                             </ehrComposition>
                        </component>
                    </ehrFolder>
                </component>
            </EhrExtract>
                """;

        var ehrExtract = unmarshallStringToEhrExtractElement(inputXml);

        var referralRequest = mapReferralRequest(ehrExtract,
                                                 ehrExtract.getComponent().get(0).getEhrFolder().getComponent().get(0).getEhrComposition(),
                                                 composition -> composition.getComponent().get(0).getRequestStatement());

        assertAll(
                () -> assertEquals(EXAMPLE_ID, referralRequest.getId()),
                () -> assertThat(referralRequest.getNote()).isEmpty(),
                () -> assertNull(referralRequest.getAuthoredOn()),
                () -> assertNull(referralRequest.getRequester().getAgent().getReference()),
                () -> assertThat(referralRequest.getRecipient()).isEmpty(),
                () -> assertThat(referralRequest.getReasonCode()).isEmpty()
        );
    }

    @Test
    public void mapReferralRequestWithPriorityNoteWithUsingDisplayName() {
        var inputXml = """
                <ehrComposition xmlns="urn:hl7-org:v3" classCode="COMPOSITION" moodCode="EVN">
                    <id root="72A39454-299F-432E-993E-5A6232B4E099" />
                    <availabilityTime value="20190708143500"/>
                    <component typeCode="COMP" >
                        <RequestStatement classCode="OBS" moodCode="RQO">
                            <id root="B4303C92-4D1C-11E3-A2DD-010000000161"/>
                            <statusCode code="COMPLETE"/>
                            <priorityCode code="394848005"
                            displayName="Normal"
                            codeSystem="2.16.840.1.113883.2.1.3.2.4.15" />
                        </RequestStatement>
                    </component>
                </ehrComposition>
                """;
        var ehrComposition = unmarshallStringToEhrCompositionElement(inputXml);

        ReferralRequest referralRequest = mapReferralRequest(null, ehrComposition,
            composition -> composition.getComponent().get(0).getRequestStatement());

        assertAll(
                () -> assertEquals(EXAMPLE_ID, referralRequest.getId()),
                () -> assertEquals("Priority: Normal", referralRequest.getNote().get(0).getText()),
                () -> assertNull(referralRequest.getAuthoredOn()),
                () -> assertNull(referralRequest.getRequester().getAgent().getReference()),
                () -> assertThat(referralRequest.getRecipient()).isEmpty(),
                () -> assertThat(referralRequest.getReasonCode()).isEmpty()
        );
    }

    @Test
    public void mapDegradedReferralRequest() {
        when(codeableConceptMapper.mapToCodeableConcept(any()))
                .thenReturn(new CodeableConcept().addCoding(new Coding()));

        var inputXml = """
                <ehrComposition xmlns="urn:hl7-org:v3" classCode="COMPOSITION" moodCode="EVN">
                    <id root="72A39454-299F-432E-993E-5A6232B4E099" />
                    <availabilityTime value="20190708143500"/>
                    <component typeCode="COMP" >
                        <RequestStatement classCode="OBS" moodCode="RQO">
                            <code code="8HV6." codeSystem="2.16.840.1.113883.2.1.3.2.4.14" displayName="Reason Code" />
                            <id root="B4303C92-4D1C-11E3-A2DD-010000000161"/>
                            <statusCode code="COMPLETE"/>
                        </RequestStatement>
                    </component>
                </ehrComposition>
                """;
        var ehrComposition = unmarshallStringToEhrCompositionElement(inputXml);

        var referralRequest = mapReferralRequest(null, ehrComposition,
            composition -> composition.getComponent().get(0).getRequestStatement());

        assertEquals(DegradedCodeableConcepts.DEGRADED_REFERRAL, referralRequest.getReasonCode().get(0).getCoding().get(0));
    }

    @ParameterizedTest
    @MethodSource("priorityCodes")
    public void mapReferralRequestPriority(String code, String display, String expectedDisplay) {
        var inputXml = """
                <ehrComposition xmlns="urn:hl7-org:v3" classCode="COMPOSITION" moodCode="EVN">
                    <id root="72A39454-299F-432E-993E-5A6232B4E099" />
                    <availabilityTime value="20190708143500"/>
                    <component typeCode="COMP" >
                        <RequestStatement classCode="OBS" moodCode="RQO">
                            <id root="B4303C92-4D1C-11E3-A2DD-010000000161"/>
                            <statusCode code="COMPLETE"/>
                            <priorityCode code="{{code}}"
                            displayName="{{display}}"
                            codeSystem="2.16.840.1.113883.2.1.3.2.4.15" />
                        </RequestStatement>
                    </component>
                </ehrComposition>
                """
                .replace("{{code}}", code)
                .replace("{{display}}", display);

        var ehrComposition = unmarshallStringToEhrCompositionElement(inputXml);

        var referralRequest = mapReferralRequest(null, ehrComposition,
            composition -> composition.getComponent().get(0).getRequestStatement());

        assertThat(referralRequest.getPriority().getDisplay()).isEqualTo(expectedDisplay);
    }

    private static Stream<Arguments> priorityCodes() {
        return Stream.of(
                arguments("394848005", "routine", "Routine"),
                arguments("394849002", "urgent", "Urgent"),
                arguments("88694003", "asap", "ASAP"));
    }

    @Test
    public void When_MapToReferralRequest_With_NestedRequestStatement_Expect_PriorityCodeMapped() {
        var ehrComposition =  unmarshallEhrCompositionElement("nested_request_statement.xml");

        var referralRequest = mapReferralRequest(null, ehrComposition, this::getNestedRequestStatement);

        assertEquals(ReferralRequest.ReferralPriority.ROUTINE, referralRequest.getPriority());
    }

    @Test
    public void When_MapToReferralRequest_With_MissingPriorityCode_Expect_PriorityCodeNotMapped() {

        var ehrExtract = unmarshallEhrExtractElement("request_statement_missing_priority_code.xml");

        var referralRequest = mapReferralRequest(ehrExtract,
                                                 ehrExtract.getComponent().get(0).getEhrFolder().getComponent().get(0).getEhrComposition(),
                                                 composition -> composition.getComponent().get(0).getRequestStatement());

        assertNull(referralRequest.getPriority());
    }

    @Test
    public void When_MapToReferralRequest_With_TwoNestedRequestStatements_Expect_CorrectPriorityCodeMapped() {
        var ehrComposition = unmarshallEhrCompositionElement("two_nested_request_statements.xml");

        var referralRequest = mapReferralRequest(null, ehrComposition, this::getNestedRequestStatement);

        assertEquals(ReferralRequest.ReferralPriority.ASAP, referralRequest.getPriority());
    }

    @Test
    public void When_MapToReferralRequest_With_UnexpectedPriorityCode_Expect_PriorityFieldNotPopulated() {

        var ehrExtract = unmarshallEhrExtractElement("request_statement_unexpected_priority_code.xml");

        var referralRequest = mapReferralRequest(ehrExtract,
                                                 ehrExtract.getComponent().get(0).getEhrFolder().getComponent().get(0).getEhrComposition(),
                                                 composition -> composition.getComponent().get(0).getRequestStatement());

        assertNull(referralRequest.getPriority());
    }

    @Test
    public void When_MapToReferralRequest_With_UnexpectedPriorityCode_Expect_PriorityAddedToNotes() {

        var ehrExtract = unmarshallEhrExtractElement("request_statement_unexpected_priority_code.xml");

        var referralRequest = mapReferralRequest(ehrExtract,
                                                 ehrExtract.getComponent().get(0).getEhrFolder().getComponent().get(0).getEhrComposition(),
                                                 composition -> composition.getComponent().get(0).getRequestStatement());

        var priorityNotes = referralRequest.getNote()
            .stream()
            .filter(note -> note.hasText() && note.getText().contains(PRIORITY_NOTE_PREPENDAGE + UNEXPECTED_PRIORITY_DISPLAY))
            .toList();

        assertThat(priorityNotes)
            .withFailMessage("Expected priority to have one entry in notes")
            .hasSize(1);
    }

    private RCMRMT030101UKRequestStatement getNestedRequestStatement(RCMRMT030101UKEhrComposition ehrComposition) {
        return ehrComposition.getComponent()
            .get(0)
            .getCompoundStatement()
            .getComponent()
            .get(1)
            .getCompoundStatement()
            .getComponent()
            .get(0)
            .getRequestStatement();
    }



    private void assertFixedValues(ReferralRequest referralRequest) {
        assertAll(
                () -> assertThat(referralRequest.getMeta().getProfile().get(0).getValue())
                        .isEqualTo(META_PROFILE),
                () -> assertThat(referralRequest.getIdentifier().get(0).getSystem())
                        .isEqualTo(IDENTIFIER_SYSTEM),
                () -> assertThat(referralRequest.getIdentifier().get(0).getValue())
                        .isEqualTo(EXAMPLE_ID),
                () -> assertThat(referralRequest.getIntent())
                        .isEqualTo(ReferralRequest.ReferralCategory.ORDER),
                () -> assertThat(referralRequest.getStatus())
                        .isEqualTo(ReferralRequestStatus.UNKNOWN),
                () -> assertThat(referralRequest.getSubject().getResource().getIdElement().getValue())
                        .isEqualTo(PATIENT_ID)
        );
    }

    private static ReferralRequest mapReferralRequest(RCMRMT030101UKEhrExtract ehrExtract,
                                                      RCMRMT030101UKEhrComposition ehrComposition,
                                                      Function<RCMRMT030101UKEhrComposition,
                                                      RCMRMT030101UKRequestStatement> extractRequestStatement) {

        var requestStatement = extractRequestStatement.apply(ehrComposition);

        Patient patient = new Patient();
        patient.setId(PATIENT_ID);

        var encounter = new Encounter();
        encounter.setId(ENCOUNTER_ID);

        return referralRequestMapper.mapToReferralRequest(
                ehrExtract,
                ehrComposition,
                requestStatement,
                patient,
                List.of(encounter),
                PRACTISE_CODE);
    }

    @SneakyThrows
    private RCMRMT030101UKEhrComposition unmarshallEhrCompositionElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UKEhrComposition.class);
    }

    @SneakyThrows
    private RCMRMT030101UKEhrComposition unmarshallStringToEhrCompositionElement(String inputXml) {
        return unmarshallString(inputXml, RCMRMT030101UKEhrComposition.class);
    }

    @SneakyThrows
    private RCMRMT030101UKEhrExtract unmarshallEhrExtractElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UKEhrExtract.class);
    }

    @SneakyThrows
    private RCMRMT030101UKEhrExtract unmarshallStringToEhrExtractElement(String inputXml) {
        return unmarshallString(inputXml, RCMRMT030101UKEhrExtract.class);
    }

}
