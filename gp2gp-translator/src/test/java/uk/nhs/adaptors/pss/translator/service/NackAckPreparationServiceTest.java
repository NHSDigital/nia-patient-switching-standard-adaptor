package uk.nhs.adaptors.pss.translator.service;

import lombok.SneakyThrows;
import org.hl7.v3.COPCIN000001UK01Message;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.model.NACKMessageData;
import uk.nhs.adaptors.pss.translator.model.NACKReason;
import uk.nhs.adaptors.pss.translator.task.SendNACKMessageHandler;

import javax.xml.bind.JAXBException;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_LRG_MSG_GENERAL_FAILURE;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

@ExtendWith(MockitoExtension.class)
class NackAckPreparationServiceTest {
    private static final String NHS_NUMBER = "123456";
    private static final String CONVERSATION_ID = randomUUID().toString();
    private static final String TEST_TO_ODS = "M85019";
    private static final String TEST_MESSAGE_REF = "31FA3430-6E88-11EA-9384-E83935108FD5";
    private static final String TEST_TO_ASID = "200000000149";
    private static final String TEST_FROM_ASID = "200000001161";
    private static final String TEST_NACK_CODE = "30";

    @Mock
    private MigrationStatusLogService migrationStatusLogService;

    @Mock
    private SendNACKMessageHandler sendNACKMessageHandler;

    @InjectMocks
    private NackAckPreparationService nackAckPreparationService;

    @Captor
    private ArgumentCaptor<NACKMessageData> ackMessageDataCaptor;

    @Captor
    private ArgumentCaptor<MigrationStatus> migrationStatusCaptor;

    @Test
    public void When_SendNackMessageRCMR_WithNoErrors_Expect_ShouldUpdateLog() throws JAXBException {
        RCMRIN030000UK06Message payload = unmarshallString(
                readInboundMessagePayloadFromFile(), RCMRIN030000UK06Message.class);

        when(sendNACKMessageHandler.prepareAndSendMessage(any(NACKMessageData.class))).thenReturn(true);

        assertTrue(nackAckPreparationService.sendNackMessage(NACKReason.LARGE_MESSAGE_GENERAL_FAILURE, payload, CONVERSATION_ID));
        verify(migrationStatusLogService).addMigrationStatusLog(ERROR_LRG_MSG_GENERAL_FAILURE, CONVERSATION_ID);
    }

    @Test
    public void When_SendNackMessageRCMR_WithErrors_Expect_ShouldUpdateLog() throws JAXBException {
        RCMRIN030000UK06Message payload = unmarshallString(
                readInboundMessagePayloadFromFile(), RCMRIN030000UK06Message.class);

        when(sendNACKMessageHandler.prepareAndSendMessage(any(NACKMessageData.class))).thenReturn(false);

        assertFalse(nackAckPreparationService.sendNackMessage(NACKReason.LARGE_MESSAGE_GENERAL_FAILURE, payload, CONVERSATION_ID));
        verify(migrationStatusLogService).addMigrationStatusLog(ERROR_LRG_MSG_GENERAL_FAILURE, CONVERSATION_ID);
    }

    @Test
    public void When_SendNackMessageRCMR_WithValidParameters_Expect_ShouldParseMessageDataCorrectly() throws JAXBException {

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
    public void When_SendNackMessageRCMR_WithReAssemblyFailure_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
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
    public void When_SendNackMessageRCMR_WithAttachmentsNotReceived_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
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
    public void When_SendNackMessageRCMR_WithGeneralFailure_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
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
    public void When_SendNackMessageRCMR_WithTimeoutFailure_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
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
    public void When_SendNackMessageRCMR_WithClinicalSysIntegrationFailure_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
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
    public void When_SendNackMessageRCMR_WithEHRExtractCannotBeProcessed_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
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
    public void When_SendNackMessageRCMR_WithUnexpectedCondition_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
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
    public void When_SendNackMessageRCMR_WithEHRExtractCannotBeProcessed_Expect_AddMigrationStatusLogCalledWithGeneralProcessingError()
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

    @Test
    public void When_SendNackMessageCOPC_WithNoErrors_Expect_ShouldUpdateLog() throws JAXBException {
        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class);

        when(sendNACKMessageHandler.prepareAndSendMessage(any(NACKMessageData.class))).thenReturn(true);

        assertTrue(nackAckPreparationService.sendNackMessage(NACKReason.LARGE_MESSAGE_GENERAL_FAILURE, payload, CONVERSATION_ID));
        verify(migrationStatusLogService).addMigrationStatusLog(ERROR_LRG_MSG_GENERAL_FAILURE, CONVERSATION_ID);
    }

    @Test
    public void When_SendNackMessageCOPC_WithErrors_Expect_ShouldUpdateLog() throws JAXBException {
        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class);

        when(sendNACKMessageHandler.prepareAndSendMessage(any(NACKMessageData.class))).thenReturn(false);

        assertFalse(nackAckPreparationService.sendNackMessage(NACKReason.LARGE_MESSAGE_GENERAL_FAILURE, payload, CONVERSATION_ID));
        verify(migrationStatusLogService).addMigrationStatusLog(ERROR_LRG_MSG_GENERAL_FAILURE, CONVERSATION_ID);
    }

    @Test
    public void When_SendNackMessageCOPC_WithValidParameters_Expect_ShouldParseMessageDataCorrectly() throws JAXBException {

        NACKMessageData expectedMessageData = NACKMessageData.builder()
                .nackCode(TEST_NACK_CODE)
                .toOdsCode(TEST_TO_ODS)
                .toAsid(TEST_TO_ASID)
                .fromAsid(TEST_FROM_ASID)
                .conversationId(CONVERSATION_ID)
                .messageRef(TEST_MESSAGE_REF)
                .build();

        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class);

        nackAckPreparationService.sendNackMessage(
                NACKReason.LARGE_MESSAGE_GENERAL_FAILURE,
                payload,
                CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals(expectedMessageData, ackMessageDataCaptor.getValue());
    }

    @Test
    public void When_SendNackMessageCOPC_WithReAssemblyFailure_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class);

        nackAckPreparationService.sendNackMessage(
                NACKReason.LARGE_MESSAGE_REASSEMBLY_FAILURE,
                payload,
                CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("29", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageCOPC_WithAttachmentsNotReceived_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class);

        nackAckPreparationService.sendNackMessage(
                NACKReason.LARGE_MESSAGE_ATTACHMENTS_NOT_RECEIVED,
                payload,
                CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("31", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageCOPC_WithGeneralFailure_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class);

        nackAckPreparationService.sendNackMessage(
                NACKReason.LARGE_MESSAGE_GENERAL_FAILURE,
                payload,
                CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("30", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageCOPC_WithTimeoutFailure_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class);

        nackAckPreparationService.sendNackMessage(
                NACKReason.LARGE_MESSAGE_TIMEOUT,
                payload,
                CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("25", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageCOPC_WithClinicalSysIntegrationFailure_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class);

        nackAckPreparationService.sendNackMessage(
                NACKReason.CLINICAL_SYSTEM_INTEGRATION_FAILURE,
                payload,
                CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("11", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageCOPC_WithEHRExtractCannotBeProcessed_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class);

        nackAckPreparationService.sendNackMessage(
                NACKReason.EHR_EXTRACT_CANNOT_BE_PROCESSED,
                payload,
                CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("21", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageCOPC_WithUnexpectedCondition_Expect_ShouldHaveCorrectNackCode() throws JAXBException {
        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class);

        nackAckPreparationService.sendNackMessage(
                NACKReason.UNEXPECTED_CONDITION,
                payload,
                CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals("99", ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void When_SendNackMessageCOPC_WithEHRExtractCannotBeProcessed_Expect_AddMigrationStatusLogCalledWithGeneralProcessingError()
            throws JAXBException {
        COPCIN000001UK01Message payload = unmarshallString(
                readSubsequentInboundMessagePayloadFromFile(), COPCIN000001UK01Message.class);

        nackAckPreparationService.sendNackMessage(
                NACKReason.EHR_EXTRACT_CANNOT_BE_PROCESSED,
                payload,
                CONVERSATION_ID);

        verify(migrationStatusLogService).addMigrationStatusLog(migrationStatusCaptor.capture(), any());

        assertEquals(MigrationStatus.EHR_GENERAL_PROCESSING_ERROR, migrationStatusCaptor.getValue());
    }

    @SneakyThrows
    private String readInboundMessagePayloadFromFile() {
        return readResourceAsString("/xml/inbound_message_payload.xml").replace("{{nhsNumber}}", NHS_NUMBER);
    }

    @SneakyThrows
    private String readSubsequentInboundMessagePayloadFromFile() {
        return readResourceAsString("/xml/COPC_IN000001UK01_subsequent_message/payload.xml");
    }
}