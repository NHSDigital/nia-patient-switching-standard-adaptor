package uk.nhs.adaptors.pss.translator.task;

import static java.util.UUID.randomUUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_LRG_MSG_GENERAL_FAILURE;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import java.text.ParseException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationException;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.v3.COPCIN000001UK01Message;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.parser.DataFormatException;
import lombok.SneakyThrows;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.model.MigrationStatusLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.exception.AttachmentNotFoundException;
import uk.nhs.adaptors.pss.translator.exception.BundleMappingException;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.exception.SkeletonEhrProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.NACKMessageData;
import uk.nhs.adaptors.pss.translator.model.NACKReason;
import uk.nhs.adaptors.pss.translator.service.AttachmentHandlerService;
import uk.nhs.adaptors.pss.translator.service.AttachmentReferenceUpdaterService;
import uk.nhs.adaptors.pss.translator.service.BundleMapperService;
import uk.nhs.adaptors.pss.translator.service.NackAckPreparationService;

@ExtendWith(MockitoExtension.class)
public class EhrExtractMessageHandlerTest {
    private static final String NHS_NUMBER = "123456";
    private static final String CONVERSATION_ID = randomUUID().toString();
    private static final String INBOUND_MESSAGE_STRING = "{hi i'm inbound message}";
    private static final String BUNDLE_STRING = "{bundle}";
    private static final String LOSING_ODE_CODE = "G543";
    private static final String WINNING_ODE_CODE = "B943";
    private static final String TEST_TO_ODS = "M85019";
    private static final String TEST_MESSAGE_REF = "31FA3430-6E88-11EA-9384-E83935108FD5";
    private static final String TEST_TO_ASID = "200000000149";
    private static final String TEST_FROM_ASID = "200000001161";
    private static final String TEST_NACK_CODE = "30";

    @Captor
    private ArgumentCaptor<NACKMessageData> ackMessageDataCaptor;

    @Captor
    private ArgumentCaptor<MigrationStatus> migrationStatusCaptor;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MigrationStatusLogService migrationStatusLogService;

    @Mock
    private PatientMigrationRequestDao migrationRequestDao;

    @Mock
    private AttachmentHandlerService attachmentHandlerService;

    @Mock
    private AttachmentReferenceUpdaterService attachmentReferenceUpdaterService;

    @Mock
    private FhirParser fhirParser;

    @Mock
    private BundleMapperService bundleMapperService;

    @Mock
    private SendACKMessageHandler sendACKMessageHandler;

    @Mock
    private SendNACKMessageHandler sendNACKMessageHandler;

    @Mock
    private PatientAttachmentLogService patientAttachmentLogService;

    @Mock
    private SendContinueRequestHandler sendContinueRequestHandler;

    @InjectMocks
    private EhrExtractMessageHandler ehrExtractMessageHandler;

    @InjectMocks
    private NackAckPreparationService nackAckPreparationService;

    @Test
    public void When_HandleMessageWithValidDataIsCalled_Expect_CallsMigrationStatusLogServiceAddMigrationStatusLog()
            throws JsonProcessingException, JAXBException,
            InlineAttachmentProcessingException, BundleMappingException, AttachmentNotFoundException, ParseException, SkeletonEhrProcessingException {

        InboundMessage inboundMessage = new InboundMessage();
        prepareMocks(inboundMessage);

        ehrExtractMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(migrationStatusLogService).addMigrationStatusLog(EHR_EXTRACT_RECEIVED, CONVERSATION_ID);
        verify(migrationStatusLogService).updatePatientMigrationRequestAndAddMigrationStatusLog(
            CONVERSATION_ID, BUNDLE_STRING, INBOUND_MESSAGE_STRING, EHR_EXTRACT_TRANSLATED);
    }

    @Test
    public void When_HandleMessageWithValidDataIsCalled_Expect_CallsBundleMapperServiceMapToBundle()
            throws JsonProcessingException, JAXBException,
            InlineAttachmentProcessingException, BundleMappingException, AttachmentNotFoundException, ParseException, SkeletonEhrProcessingException {
        InboundMessage inboundMessage = new InboundMessage();
        prepareMocks(inboundMessage);

        ehrExtractMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);
        verify(bundleMapperService).mapToBundle(any(), any()); // mapped item is private to the class, so we cannot test an exact object
    }

    @Test
    public void When_HandleMessageWithValidDataIsCalled_Expect_CallsAttachmentHandlerServiceStoreAttachments()
            throws JsonProcessingException, JAXBException,
            InlineAttachmentProcessingException, BundleMappingException, AttachmentNotFoundException, ParseException, SkeletonEhrProcessingException {

        InboundMessage inboundMessage = new InboundMessage();
        prepareMocks(inboundMessage);

        ehrExtractMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(attachmentHandlerService).storeAttachments(inboundMessage.getAttachments(), CONVERSATION_ID);
    }

    @Test
    public void When_HandleMessageWithValidDataIsCalled_Expect_CallsAttachmentReferenceUpdaterServiceUpdateReferences()
            throws JsonProcessingException, JAXBException,
            InlineAttachmentProcessingException, BundleMappingException, AttachmentNotFoundException, ParseException, SkeletonEhrProcessingException {

        InboundMessage inboundMessage = new InboundMessage();
        prepareMocks(inboundMessage);

        ehrExtractMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(attachmentReferenceUpdaterService).updateReferenceToAttachment(
                inboundMessage.getAttachments(), CONVERSATION_ID, inboundMessage.getPayload());
    }

    @Test
    public void When_HandleLargeMessageWithValidDataIsCalled_Expect_CallSendContinueRequest()
            throws JsonProcessingException, JAXBException,
            InlineAttachmentProcessingException, BundleMappingException, AttachmentNotFoundException, ParseException, SkeletonEhrProcessingException {

        Bundle bundle = new Bundle();
        bundle.setId("Test");

        InboundMessage inboundMessage = new InboundMessage();
        List<InboundMessage.ExternalAttachment> externalAttachmentsTestList = new ArrayList<>();
        externalAttachmentsTestList.add(
                new InboundMessage.ExternalAttachment(
                        "68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs",
                        "66B41202-C358-4B4C-93C6-7A10803F9584",
                        "68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs.ukExample1",
                        "Filename=\"68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs.ukExample1.gzip\" "
                                + "ContentType=text/xml Compressed=Yes LargeAttachment=No OriginalBase64=Yes "
                                + "DomainData=\"X-GP2GP-Skeleton: Yes\"")
        );

        inboundMessage.setPayload(readLargeInboundMessagePayloadFromFile());
        inboundMessage.setEbXML(readLargeInboundMessageEbXmlFromFile());
        inboundMessage.setExternalAttachments(externalAttachmentsTestList);

        prepareMigrationRequestAndMigrationStatusMocks();

        EhrExtractMessageHandler ehrExtractMessageHandlerSpy = Mockito.spy(ehrExtractMessageHandler);
        ehrExtractMessageHandlerSpy.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(ehrExtractMessageHandlerSpy).sendContinueRequest(
            any(RCMRIN030000UK06Message.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(Instant.class)
        );
    }


    @Test
    public void When_HandleMessageWithValidDataIsCalled_Expect_CallsStatusLogServiceUpdatePatientMigrationRequestAndAddMigrationStatusLog()
            throws JsonProcessingException, JAXBException,
            InlineAttachmentProcessingException, BundleMappingException, AttachmentNotFoundException, ParseException, SkeletonEhrProcessingException {

        InboundMessage inboundMessage = new InboundMessage();
        prepareMocks(inboundMessage);

        ehrExtractMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(migrationStatusLogService).updatePatientMigrationRequestAndAddMigrationStatusLog(
            CONVERSATION_ID, BUNDLE_STRING, INBOUND_MESSAGE_STRING, EHR_EXTRACT_TRANSLATED);
    }

    @Test
    public void When_HandleMessage_WithStoreAttachmentsThrows_Expect_InlineAttachmentProcessingException() throws JAXBException,
        InlineAttachmentProcessingException {
        InboundMessage inboundMessage = new InboundMessage();
        Bundle bundle = new Bundle();
        bundle.setId("Test");
        inboundMessage.setPayload(readInboundMessagePayloadFromFile());
        inboundMessage.setExternalAttachments(new ArrayList<>());

        PatientMigrationRequest migrationRequest =
            PatientMigrationRequest.builder()
                .losingPracticeOdsCode(LOSING_ODE_CODE)
                .winningPracticeOdsCode(WINNING_ODE_CODE)
                .build();

        when(migrationRequestDao.getMigrationRequest(CONVERSATION_ID)).thenReturn(migrationRequest);

        doThrow(new InlineAttachmentProcessingException("Test Exception"))
            .when(attachmentHandlerService).storeAttachments(any(), any());

        assertThrows(InlineAttachmentProcessingException.class, () ->
                        ehrExtractMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID));
    }

    @Test
    public void When_HandleMessage_WithMapToBundleThrows_Expect_BundleMappingException()
            throws BundleMappingException, AttachmentNotFoundException, ValidationException, InlineAttachmentProcessingException {
        InboundMessage inboundMessage = new InboundMessage();
        inboundMessage.setPayload(readInboundMessagePayloadFromFile());
        inboundMessage.setExternalAttachments(new ArrayList<>());


        PatientMigrationRequest migrationRequest =
            PatientMigrationRequest.builder()
                .losingPracticeOdsCode(LOSING_ODE_CODE)
                .winningPracticeOdsCode(WINNING_ODE_CODE)
                .build();

        when(migrationRequestDao.getMigrationRequest(CONVERSATION_ID)).thenReturn(migrationRequest);
        when(attachmentReferenceUpdaterService
                .updateReferenceToAttachment(inboundMessage.getAttachments(), CONVERSATION_ID, inboundMessage.getPayload()))
                .thenReturn(inboundMessage.getPayload());

        doThrow(new BundleMappingException("Test Exception"))
            .when(bundleMapperService).mapToBundle(any(), any());

        assertThrows(BundleMappingException.class, () -> ehrExtractMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID));
    }

    @Test
    public void When_HandleMessage_WithEncodeToJsonThrows_Expect_DataFormatException()
            throws BundleMappingException, AttachmentNotFoundException, ValidationException, InlineAttachmentProcessingException {
        InboundMessage inboundMessage = new InboundMessage();
        inboundMessage.setPayload(readInboundMessagePayloadFromFile());
        Bundle bundle = new Bundle();
        bundle.setId("Test");
        inboundMessage.setPayload(readInboundMessagePayloadFromFile());
        inboundMessage.setExternalAttachments(new ArrayList<>());

        PatientMigrationRequest migrationRequest =
            PatientMigrationRequest.builder()
                .losingPracticeOdsCode(LOSING_ODE_CODE)
                .winningPracticeOdsCode(WINNING_ODE_CODE)
                .build();

        when(bundleMapperService.mapToBundle(any(RCMRIN030000UK06Message.class), eq(LOSING_ODE_CODE))).thenReturn(bundle);
        when(migrationRequestDao.getMigrationRequest(CONVERSATION_ID)).thenReturn(migrationRequest);
        when(attachmentReferenceUpdaterService
                .updateReferenceToAttachment(inboundMessage.getAttachments(), CONVERSATION_ID, inboundMessage.getPayload()))
                .thenReturn(inboundMessage.getPayload());

        doThrow(new DataFormatException()).when(fhirParser).encodeToJson(bundle);

        assertThrows(DataFormatException.class, () -> ehrExtractMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID));
    }

    @Test
    public void When_SendNackMessageRCMRIN030000UK06_WithNoErrors_Expect_ShouldUpdateLog() throws JAXBException {
        RCMRIN030000UK06Message payload = unmarshallString(
            readInboundMessagePayloadFromFile(), RCMRIN030000UK06Message.class);

        when(sendNACKMessageHandler.prepareAndSendMessage(any(NACKMessageData.class))).thenReturn(true);

        assertTrue(nackAckPreparationService.sendNackMessage(NACKReason.LARGE_MESSAGE_GENERAL_FAILURE, payload, CONVERSATION_ID));
        verify(migrationStatusLogService).addMigrationStatusLog(ERROR_LRG_MSG_GENERAL_FAILURE, CONVERSATION_ID);
    }

    @Test
    public void When_SendNackMessageRCMRIN030000UK06_WithErrors_Expect_ShouldUpdateLog() throws JAXBException {
        RCMRIN030000UK06Message payload = unmarshallString(
            readInboundMessagePayloadFromFile(), RCMRIN030000UK06Message.class);

        when(sendNACKMessageHandler.prepareAndSendMessage(any(NACKMessageData.class))).thenReturn(false);

        assertFalse(nackAckPreparationService.sendNackMessage(NACKReason.LARGE_MESSAGE_GENERAL_FAILURE, payload, CONVERSATION_ID));
        verify(migrationStatusLogService).addMigrationStatusLog(ERROR_LRG_MSG_GENERAL_FAILURE, CONVERSATION_ID);
    }

    @Test
    public void When_SendNackMessageRCMRIN030000UK06_WithValidParameters_Expect_ShouldParseMessageDataCorrectly() throws JAXBException {

        NACKMessageData expectedMessageData = NACKMessageData.builder()
            .nackCode(TEST_NACK_CODE)
            .toOdsCode(TEST_TO_ODS)
            .toAsid(TEST_TO_ASID)
            .fromAsid(TEST_FROM_ASID)
            .conversationId(CONVERSATION_ID)
            .messageRef(TEST_MESSAGE_REF)
            .build();

        RCMRIN030000UK06Message payload = unmarshallString(
            readInboundMessagePayloadFromFile(), RCMRIN030000UK06Message.class);

        nackAckPreparationService.sendNackMessage(
            NACKReason.LARGE_MESSAGE_GENERAL_FAILURE,
            payload,
            CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals(expectedMessageData, ackMessageDataCaptor.getValue());
    }

    @Test
    public void When_SendNackMessageRCMRIN030000UK06_WithReAssemblyFailure_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        RCMRIN030000UK06Message payload = unmarshallString(
            readInboundMessagePayloadFromFile(), RCMRIN030000UK06Message.class);

        nackAckPreparationService.sendNackMessage(
            NACKReason.LARGE_MESSAGE_REASSEMBLY_FAILURE,
            payload,
            CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("29", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageRCMRIN030000UK06_WithAttachmentsNotReceived_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        RCMRIN030000UK06Message payload = unmarshallString(
            readInboundMessagePayloadFromFile(), RCMRIN030000UK06Message.class);

        nackAckPreparationService.sendNackMessage(
            NACKReason.LARGE_MESSAGE_ATTACHMENTS_NOT_RECEIVED,
            payload,
            CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("31", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageRCMRIN030000UK06_WithGeneralFailure_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        RCMRIN030000UK06Message payload = unmarshallString(
            readInboundMessagePayloadFromFile(), RCMRIN030000UK06Message.class);

        nackAckPreparationService.sendNackMessage(
            NACKReason.LARGE_MESSAGE_GENERAL_FAILURE,
            payload,
            CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("30", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageRCMRIN030000UK06_WithTimeoutFailure_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        RCMRIN030000UK06Message payload = unmarshallString(
            readInboundMessagePayloadFromFile(), RCMRIN030000UK06Message.class);

        nackAckPreparationService.sendNackMessage(
            NACKReason.LARGE_MESSAGE_TIMEOUT,
            payload,
            CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("25", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageRCMRIN030000UK06_WithClinicalSysIntegrationFailure_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        RCMRIN030000UK06Message payload = unmarshallString(
            readInboundMessagePayloadFromFile(), RCMRIN030000UK06Message.class);

        nackAckPreparationService.sendNackMessage(
            NACKReason.CLINICAL_SYSTEM_INTEGRATION_FAILURE,
            payload,
            CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("11", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageRCMRIN030000UK06_WithEHRExtractCannotBeProcessed_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        RCMRIN030000UK06Message payload = unmarshallString(
            readInboundMessagePayloadFromFile(), RCMRIN030000UK06Message.class);

        nackAckPreparationService.sendNackMessage(
            NACKReason.EHR_EXTRACT_CANNOT_BE_PROCESSED,
            payload,
            CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("21", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageRCMRIN030000UK06_WithUnexpectedCondition_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        RCMRIN030000UK06Message payload = unmarshallString(
            readInboundMessagePayloadFromFile(), RCMRIN030000UK06Message.class);

        nackAckPreparationService.sendNackMessage(
            NACKReason.UNEXPECTED_CONDITION,
            payload,
            CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("99", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageRCMRIN030000UK06_WithEHRExtractCannotBeProcessed_Expect_AddMigrationStatusLogCalledWithGeneralProcessingError()
        throws JAXBException {
        RCMRIN030000UK06Message payload = unmarshallString(
            readInboundMessagePayloadFromFile(), RCMRIN030000UK06Message.class);

        nackAckPreparationService.sendNackMessage(
            NACKReason.EHR_EXTRACT_CANNOT_BE_PROCESSED,
            payload,
            CONVERSATION_ID);

        verify(migrationStatusLogService).addMigrationStatusLog(migrationStatusCaptor.capture(), any());

        assertEquals(MigrationStatus.EHR_GENERAL_PROCESSING_ERROR, migrationStatusCaptor.getValue());
    }

    /////////////

    @Test
    public void When_SendNackMessageCOPCIN000001UK01_WithNoErrors_Expect_ShouldUpdateLog() throws JAXBException {
        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class); //change

        when(sendNACKMessageHandler.prepareAndSendMessage(any(NACKMessageData.class))).thenReturn(true);

        assertTrue(nackAckPreparationService.sendNackMessage(NACKReason.LARGE_MESSAGE_GENERAL_FAILURE, payload, CONVERSATION_ID));
        verify(migrationStatusLogService).addMigrationStatusLog(ERROR_LRG_MSG_GENERAL_FAILURE, CONVERSATION_ID);
    }

    @Test
    public void When_SendNackMessageCOPCIN000001UK01_WithErrors_Expect_ShouldUpdateLog() throws JAXBException {
        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class); //change

        when(sendNACKMessageHandler.prepareAndSendMessage(any(NACKMessageData.class))).thenReturn(false);

        assertFalse(nackAckPreparationService.sendNackMessage(NACKReason.LARGE_MESSAGE_GENERAL_FAILURE, payload, CONVERSATION_ID));
        verify(migrationStatusLogService).addMigrationStatusLog(ERROR_LRG_MSG_GENERAL_FAILURE, CONVERSATION_ID);
    }

    @Test
    public void When_SendNackMessageCOPCIN000001UK01_WithValidParameters_Expect_ShouldParseMessageDataCorrectly() throws JAXBException {

        NACKMessageData expectedMessageData = NACKMessageData.builder()
                .nackCode(TEST_NACK_CODE)
                .toOdsCode(TEST_TO_ODS)
                .toAsid(TEST_TO_ASID)
                .fromAsid(TEST_FROM_ASID)
                .conversationId(CONVERSATION_ID)
                .messageRef(TEST_MESSAGE_REF)
                .build();

        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class);//change

        nackAckPreparationService.sendNackMessage(
                NACKReason.LARGE_MESSAGE_GENERAL_FAILURE,
                payload,
                CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals(expectedMessageData, ackMessageDataCaptor.getValue());
    }

    @Test
    public void When_SendNackMessageCOPCIN000001UK01_WithReAssemblyFailure_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class); //change

        nackAckPreparationService.sendNackMessage(
                NACKReason.LARGE_MESSAGE_REASSEMBLY_FAILURE,
                payload,
                CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("29", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageCOPCIN000001UK01_WithAttachmentsNotReceived_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class); //change

        nackAckPreparationService.sendNackMessage(
                NACKReason.LARGE_MESSAGE_ATTACHMENTS_NOT_RECEIVED,
                payload,
                CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("31", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageCOPCIN000001UK01_WithGeneralFailure_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class); //change

        nackAckPreparationService.sendNackMessage(
                NACKReason.LARGE_MESSAGE_GENERAL_FAILURE,
                payload,
                CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("30", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageCOPCIN000001UK01_WithTimeoutFailure_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class);//change

        nackAckPreparationService.sendNackMessage(
                NACKReason.LARGE_MESSAGE_TIMEOUT,
                payload,
                CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("25", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageCOPCIN000001UK01_WithClinicalSysIntegrationFailure_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class); //change

        nackAckPreparationService.sendNackMessage(
                NACKReason.CLINICAL_SYSTEM_INTEGRATION_FAILURE,
                payload,
                CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("11", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageCOPCIN000001UK01_WithEHRExtractCannotBeProcessed_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class); //change

        nackAckPreparationService.sendNackMessage(
                NACKReason.EHR_EXTRACT_CANNOT_BE_PROCESSED,
                payload,
                CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("21", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageCOPCIN000001UK01_WithUnexpectedCondition_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class); //change

        nackAckPreparationService.sendNackMessage(
                NACKReason.UNEXPECTED_CONDITION,
                payload,
                CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("99", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageCOPCIN000001UK01_WithEHRExtractCannotBeProcessed_Expect_AddMigrationStatusLogCalledWithGeneralProcessingError()
            throws JAXBException {
        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class);//change

        nackAckPreparationService.sendNackMessage(
                NACKReason.EHR_EXTRACT_CANNOT_BE_PROCESSED,
                payload,
                CONVERSATION_ID);

        verify(migrationStatusLogService).addMigrationStatusLog(migrationStatusCaptor.capture(), any());

        assertEquals(MigrationStatus.EHR_GENERAL_PROCESSING_ERROR, migrationStatusCaptor.getValue());
    }



    @Test
    public void When_HandleSingleMessageWithValidDataIsCalled_Expect_NotToCallSendContinueRequest()
            throws JsonProcessingException, JAXBException,
            InlineAttachmentProcessingException, BundleMappingException, AttachmentNotFoundException, ParseException, SkeletonEhrProcessingException {

        Bundle bundle = new Bundle();
        bundle.setId("Test");

        InboundMessage inboundMessage = new InboundMessage();
        List<InboundMessage.ExternalAttachment> externalAttachmentsTestList = new ArrayList<>();

        inboundMessage.setPayload(readInboundSingleMessagePayloadFromFile());
        inboundMessage.setEbXML(readInboundSingleMessageEbXmlFromFile());
        inboundMessage.setExternalAttachments(externalAttachmentsTestList);

        prepareMigrationRequestAndMigrationStatusMocks();

        when(attachmentReferenceUpdaterService
                .updateReferenceToAttachment(
                        inboundMessage.getAttachments(), CONVERSATION_ID, inboundMessage.getPayload()
                )).thenReturn(inboundMessage.getPayload());

        EhrExtractMessageHandler ehrExtractMessageHandlerSpy = Mockito.spy(ehrExtractMessageHandler);
        ehrExtractMessageHandlerSpy.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(ehrExtractMessageHandlerSpy, times(0)).sendContinueRequest(
                any(RCMRIN030000UK06Message.class),
                any(String.class),
                any(String.class),
                any(String.class),
                any(Instant.class)
        );
    }

    @Test
    public void When_HandleLargeMessageWithValidDataIsCalled_Expect_ItShouldNotTranslate()
            throws JAXBException, BundleMappingException, AttachmentNotFoundException,
            ParseException, JsonProcessingException, InlineAttachmentProcessingException, SkeletonEhrProcessingException {

        Bundle bundle = new Bundle();
        bundle.setId("Test");

        InboundMessage inboundMessage = new InboundMessage();
        List<InboundMessage.ExternalAttachment> externalAttachmentsTestList = new ArrayList<>();
        externalAttachmentsTestList.add(
                new InboundMessage.ExternalAttachment(
                        "68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs",
                        "66B41202-C358-4B4C-93C6-7A10803F9584",
                        "68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs.ukExample1",
                        "Filename=\"68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs.ukExample1.gzip\" "
                                + "ContentType=text/xml Compressed=Yes LargeAttachment=No OriginalBase64=Yes "
                                + "DomainData=\"X-GP2GP-Skeleton: Yes\"")
        );
        inboundMessage.setPayload(readLargeInboundMessagePayloadFromFile());
        inboundMessage.setEbXML(readLargeInboundMessageEbXmlFromFile());
        inboundMessage.setExternalAttachments(externalAttachmentsTestList);

        prepareMigrationRequestAndMigrationStatusMocks();

        ehrExtractMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);
        verify(bundleMapperService, times(0)).mapToBundle(any(), any());
    }

    @Test
    public void When_HandleLargeMessageWithValidDataIsCalled_Expect_StoreMessagePayload()
            throws JsonProcessingException, JAXBException,
            InlineAttachmentProcessingException, BundleMappingException, AttachmentNotFoundException, ParseException, SkeletonEhrProcessingException {

        Bundle bundle = new Bundle();
        bundle.setId("Test");

        InboundMessage inboundMessage = new InboundMessage();
        List<InboundMessage.ExternalAttachment> externalAttachmentsTestList = new ArrayList<>();
        externalAttachmentsTestList.add(
                new InboundMessage.ExternalAttachment(
                        "68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs",
                        "66B41202-C358-4B4C-93C6-7A10803F9584",
                        "68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs.ukExample1",
                        "Filename=\"68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs.ukExample1.gzip\" "
                                + "ContentType=text/xml Compressed=Yes LargeAttachment=No OriginalBase64=Yes "
                                + "DomainData=\"X-GP2GP-Skeleton: Yes\"")
        );

        inboundMessage.setPayload(readLargeInboundMessagePayloadFromFile());
        inboundMessage.setEbXML(readLargeInboundMessageEbXmlFromFile());
        inboundMessage.setExternalAttachments(externalAttachmentsTestList);

        prepareMigrationRequestAndMigrationStatusMocks();

        ehrExtractMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);
        verify(attachmentHandlerService, times(1)).storeEhrExtract(any(), any(), any(), any());
    }

    @Test
    public void When_HandleLargeMessageWithValidDataIsCalled_Expect_AddAttachmentExactNumerOfTimesAsExternalAttachmentsList()
            throws JsonProcessingException, JAXBException,
            InlineAttachmentProcessingException, BundleMappingException, AttachmentNotFoundException, ParseException, SkeletonEhrProcessingException {

        Bundle bundle = new Bundle();
        bundle.setId("Test");

        InboundMessage inboundMessage = new InboundMessage();

        List<InboundMessage.ExternalAttachment> externalAttachmentsTestList = new ArrayList<>();
        externalAttachmentsTestList.add(
                new InboundMessage.ExternalAttachment(
                        "68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs",
                        "66B41202-C358-4B4C-93C6-7A10803F9584",
                        "68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs.ukExample1",
                        "Filename=\"68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs.ukExample1.gzip\" "
                                + "ContentType=text/xml Compressed=Yes LargeAttachment=No OriginalBase64=Yes "
                                + "DomainData=\"X-GP2GP-Skeleton: Yes\"")
        );
        externalAttachmentsTestList.add(
                new InboundMessage.ExternalAttachment(
                        "68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs",
                        "66B41202-C358-4B4C-93C6-7A10803F9584",
                        "68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs.ukExample1",
                        "Filename=\"68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs.ukExample1.gzip\" "
                                + "ContentType=text/xml Compressed=Yes LargeAttachment=No OriginalBase64=Yes "
                                + "DomainData=\"X-GP2GP-Skeleton: Yes\"")
        );
        externalAttachmentsTestList.add(
                new InboundMessage.ExternalAttachment(
                        "68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs",
                        "66B41202-C358-4B4C-93C6-7A10803F9584",
                        "68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs.ukExample1",
                        "Filename=\"68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs.ukExample1.gzip\" "
                                + "ContentType=text/xml Compressed=Yes LargeAttachment=No OriginalBase64=Yes")
        );
        externalAttachmentsTestList.add(
                new InboundMessage.ExternalAttachment(
                        "68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs",
                        "66B41202-C358-4B4C-93C6-7A10803F9584",
                        "68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs.ukExample1",
                        "Filename=\"68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs.ukExample1.gzip\" "
                                + "ContentType=text/xml Compressed=Yes LargeAttachment=No OriginalBase64=Yes")
        );

        inboundMessage.setPayload(readLargeInboundMessagePayloadFromFile());
        inboundMessage.setEbXML(readLargeInboundMessageEbXmlFromFile());
        inboundMessage.setExternalAttachments(externalAttachmentsTestList);

        prepareMigrationRequestAndMigrationStatusMocks();

        ehrExtractMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);
        verify(patientAttachmentLogService, times(externalAttachmentsTestList.size())).addAttachmentLog(any());
    }

    @SneakyThrows
    private void prepareMocks(InboundMessage inboundMessage) {
        inboundMessage.setPayload("payload");
        Bundle bundle = new Bundle();
        bundle.setId("Test");

        inboundMessage.setPayload(readInboundMessagePayloadFromFile());
        inboundMessage.setEbXML(readInboundMessageEbXmlFromFile());
        inboundMessage.setExternalAttachments(new ArrayList<>());

        prepareMigrationRequestAndMigrationStatusMocks();

        // imported from main on merge
        when(fhirParser.encodeToJson(bundle)).thenReturn(BUNDLE_STRING);
        when(objectMapper.writeValueAsString(inboundMessage)).thenReturn(INBOUND_MESSAGE_STRING);
        when(bundleMapperService.mapToBundle(any(RCMRIN030000UK06Message.class), eq(LOSING_ODE_CODE))).thenReturn(bundle);
        when(sendACKMessageHandler.prepareAndSendMessage(any())).thenReturn(true);
        when(attachmentReferenceUpdaterService
                .updateReferenceToAttachment(
                        inboundMessage.getAttachments(), CONVERSATION_ID, inboundMessage.getPayload()
                )).thenReturn(inboundMessage.getPayload());
    }

    @SneakyThrows
    private void prepareMigrationRequestAndMigrationStatusMocks() {

        PatientMigrationRequest migrationRequest =
                PatientMigrationRequest.builder()
                        .losingPracticeOdsCode(LOSING_ODE_CODE)
                        .winningPracticeOdsCode(WINNING_ODE_CODE)
                        .build();

        MigrationStatusLog migrationStatusLog =
                MigrationStatusLog.builder()
                        .date(OffsetDateTime.ofInstant(
                                Instant.now(),
                                ZoneId.systemDefault()))
                        .build();

        when(migrationRequestDao.getMigrationRequest(CONVERSATION_ID)).thenReturn(migrationRequest);
        when(migrationStatusLogService.getLatestMigrationStatusLog(CONVERSATION_ID)).thenReturn(migrationStatusLog);
    }

    @SneakyThrows
    private String readInboundMessagePayloadFromFile() {
        return readResourceAsString("/xml/inbound_message_payload.xml").replace("{{nhsNumber}}", NHS_NUMBER);
    }

    @SneakyThrows
    private String readInboundMessageEbXmlFromFile() {
        return readResourceAsString("/xml/inbound_message_ebxml.xml");
    }

    private String readInboundSingleMessageEbXmlFromFile() {
        return readResourceAsString("/xml/RCMRIN030000UK06/ebxmlSmallMessage.xml").replace("{{nhsNumber}}", NHS_NUMBER);
    }

    private String readInboundSingleMessagePayloadFromFile() {
        return readResourceAsString("/xml/RCMRIN030000UK06/payloadSmallMessage.xml").replace("{{nhsNumber}}", NHS_NUMBER);
    }

    @SneakyThrows
    private String readLargeInboundMessagePayloadFromFile() {
        return readResourceAsString("/xml/RCMRIN030000UK06_LARGE_MSG/payload.xml");
    }

    @SneakyThrows
    private String readLargeInboundMessageEbXmlFromFile() {
        return readResourceAsString("/xml/RCMRIN030000UK06_LARGE_MSG/ebxml.xml");
    }

    @SneakyThrows
    private String readSubsequentInboundMessagePayloadFromFile() {
        return readResourceAsString("/xml/COPC_IN000001UK01_subsequent_message/payload.xml");
    }

}
