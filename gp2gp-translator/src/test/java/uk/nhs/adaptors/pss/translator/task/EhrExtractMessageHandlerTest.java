package uk.nhs.adaptors.pss.translator.task;

import static java.util.UUID.randomUUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_LRG_MSG_GENERAL_FAILURE;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.ApplicationAcknowledgmentData;
import uk.nhs.adaptors.pss.translator.model.NACKReason;
import uk.nhs.adaptors.pss.translator.service.BundleMapperService;

@ExtendWith(MockitoExtension.class)
public class EhrExtractMessageHandlerTest {
    private static final String NHS_NUMBER = "123456";
    private static final String CONVERSATION_ID = randomUUID().toString();
    private static final String INBOUND_MESSAGE_STRING = "{hi i'm inbound message}";
    private static final String BUNDLE_STRING = "{bundle}";
    private static final String TEST_TO_ODS = "M85019";
    private static final String TEST_MESSAGE_REF = "31FA3430-6E88-11EA-9384-E83935108FD5";
    private static final String TEST_TO_ASID = "200000000149";
    private static final String TEST_FROM_ASID = "200000001161";
    private static final String TEST_NACK_CODE = "30";

    @Captor
    ArgumentCaptor<ApplicationAcknowledgmentData> ackMessageDataCaptor;

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private MigrationStatusLogService migrationStatusLogService;

    @Mock
    private FhirParser fhirParser;

    @Mock
    private BundleMapperService bundleMapperService;

    @Mock
    private SendNACKMessageHandler sendNACKMessageHandler;

    @InjectMocks
    private EhrExtractMessageHandler ehrExtractMessageHandler;

    @Test
    public void handleMessageWithoutErrorsShouldReturnTrue() throws JsonProcessingException, JAXBException {
        InboundMessage inboundMessage = new InboundMessage();
        prepareMocks(inboundMessage);

        ehrExtractMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(migrationStatusLogService).addMigrationStatusLog(EHR_EXTRACT_RECEIVED, CONVERSATION_ID);
        verify(migrationStatusLogService).updatePatientMigrationRequestAndAddMigrationStatusLog(
            CONVERSATION_ID, BUNDLE_STRING, INBOUND_MESSAGE_STRING, EHR_EXTRACT_TRANSLATED);
    }

    @SneakyThrows
    private void prepareMocks(InboundMessage inboundMessage) {
        Bundle bundle = new Bundle();
        bundle.setId("Test");
        inboundMessage.setPayload(readInboundMessagePayloadFromFile());
        when(bundleMapperService.mapToBundle(any(RCMRIN030000UK06Message.class))).thenReturn(bundle);
        when(fhirParser.encodeToJson(bundle)).thenReturn(BUNDLE_STRING);
        when(objectMapper.writeValueAsString(inboundMessage)).thenReturn(INBOUND_MESSAGE_STRING);
    }

    @SneakyThrows
    private String readInboundMessagePayloadFromFile() {
        return readResourceAsString("/xml/inbound_message_payload.xml").replace("{{nhsNumber}}", NHS_NUMBER);
    }

    @Test
    public void whenSendNackMessage_withNoErrors_thenShouldUpdateLog() throws JAXBException {
        RCMRIN030000UK06Message payload = unmarshallString(
            readInboundMessagePayloadFromFile(), RCMRIN030000UK06Message.class);

        when(sendNACKMessageHandler.prepareAndSendMessage(any(ApplicationAcknowledgmentData.class))).thenReturn(true);

        assertTrue(ehrExtractMessageHandler.sendNackMessage(NACKReason.LARGE_MESSAGE_GENERAL_FAILURE, payload, CONVERSATION_ID));
        verify(migrationStatusLogService).addMigrationStatusLog(ERROR_LRG_MSG_GENERAL_FAILURE, CONVERSATION_ID);
    }

    @Test
    public void whenSendNackMessage_withErrors_thenShouldUpdateLog() throws JAXBException {
        RCMRIN030000UK06Message payload = unmarshallString(
            readInboundMessagePayloadFromFile(), RCMRIN030000UK06Message.class);

        when(sendNACKMessageHandler.prepareAndSendMessage(any(ApplicationAcknowledgmentData.class))).thenReturn(false);

        assertFalse(ehrExtractMessageHandler.sendNackMessage(NACKReason.LARGE_MESSAGE_GENERAL_FAILURE, payload, CONVERSATION_ID));
        verify(migrationStatusLogService).addMigrationStatusLog(ERROR_LRG_MSG_GENERAL_FAILURE, CONVERSATION_ID);
    }

    @Test
    public void whenSendNackMessage_withValidParameters_thenShouldParseMessageDataCorrectly() throws JAXBException {

        ApplicationAcknowledgmentData expectedMessageData = ApplicationAcknowledgmentData.builder()
            .nackCode(TEST_NACK_CODE)
            .toOdsCode(TEST_TO_ODS)
            .toAsid(TEST_TO_ASID)
            .fromAsid(TEST_FROM_ASID)
            .conversationId(CONVERSATION_ID)
            .messageRef(TEST_MESSAGE_REF)
            .build();

        RCMRIN030000UK06Message payload = unmarshallString(
            readInboundMessagePayloadFromFile(), RCMRIN030000UK06Message.class);

        ehrExtractMessageHandler.sendNackMessage(
            NACKReason.LARGE_MESSAGE_GENERAL_FAILURE,
            payload,
            CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals(expectedMessageData, ackMessageDataCaptor.getValue());
    }

    @Test
    public void whenSendNackMessage_withReAssemblyFailure_thenShouldHaveCorrectNackCode() throws JAXBException {
        RCMRIN030000UK06Message payload = unmarshallString(
            readInboundMessagePayloadFromFile(), RCMRIN030000UK06Message.class);

        ehrExtractMessageHandler.sendNackMessage(
            NACKReason.LARGE_MESSAGE_REASSEMBLY_FAILURE,
            payload,
            CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals(Optional.of("29"), ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void whenSendNackMessage_withAttachmentsNotReceived_thenShouldHaveCorrectNackCode() throws JAXBException {
        RCMRIN030000UK06Message payload = unmarshallString(
            readInboundMessagePayloadFromFile(), RCMRIN030000UK06Message.class);

        ehrExtractMessageHandler.sendNackMessage(
            NACKReason.LARGE_MESSAGE_ATTACHMENTS_NOT_RECEIVED,
            payload,
            CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals(Optional.of("31"), ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void whenSendNackMessage_withGeneralFailure_thenShouldHaveCorrectNackCode() throws JAXBException {
        RCMRIN030000UK06Message payload = unmarshallString(
            readInboundMessagePayloadFromFile(), RCMRIN030000UK06Message.class);

        ehrExtractMessageHandler.sendNackMessage(
            NACKReason.LARGE_MESSAGE_GENERAL_FAILURE,
            payload,
            CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals(Optional.of("30"), ackMessageDataCaptor.getValue().getNackCode());
    }

    @Test
    public void whenSendNackMessage_withTimeoutFailure_thenShouldHaveCorrectNackCode() throws JAXBException {
        RCMRIN030000UK06Message payload = unmarshallString(
            readInboundMessagePayloadFromFile(), RCMRIN030000UK06Message.class);

        ehrExtractMessageHandler.sendNackMessage(
            NACKReason.LARGE_MESSAGE_TIMEOUT,
            payload,
            CONVERSATION_ID);

        verify(sendNACKMessageHandler).prepareAndSendMessage(ackMessageDataCaptor.capture());
        assertEquals(Optional.of("25"), ackMessageDataCaptor.getValue().getNackCode());
    }
}
