package uk.nhs.adaptors.pss.translator.task;

import static java.util.UUID.randomUUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;
import static uk.nhs.adaptors.pss.translator.model.NACKReason.UNEXPECTED_CONDITION;

import java.text.ParseException;
import java.util.Locale;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import uk.nhs.adaptors.common.service.MDCService;
import uk.nhs.adaptors.common.enums.MigrationStatus;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.connector.service.PatientMigrationRequestService;
import uk.nhs.adaptors.pss.translator.amqp.JmsReader;
import uk.nhs.adaptors.pss.translator.exception.AttachmentNotFoundException;
import uk.nhs.adaptors.pss.translator.exception.BundleMappingException;
import uk.nhs.adaptors.pss.translator.exception.ConversationIdNotFoundException;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.service.XPathService;

@ExtendWith(MockitoExtension.class)
public class MhsQueueMessageHandlerTest {

    private static final String NHS_NUMBER = "123456";
    private static final String INBOUND_MESSAGE_STRING = "{hi i'm inbound message}";
    private static final String EHR_EXTRACT_INTERACTION_ID = "RCMR_IN030000UK06";
    private static final String ACKNOWLEDGEMENT_INTERACTION_ID = "MCCI_IN010000UK13";
    private static final String COPC_INTERACTION_ID = "COPC_IN000001UK01";
    private static final String EHR_EXTRACT_REQUEST_INTERACTION_ID = "RCMR_IN010000UK05";
    private static final String UNKNOWN_INTERACTION_ID = "RANDOM_IN000001UK01";
    private static final String CONVERSATION_ID_PATH = "/Envelope/Header/MessageHeader/ConversationId";
    private static final String INTERACTION_ID_PATH = "/Envelope/Header/MessageHeader/Action";
    private static final String CONVERSATION_ID = randomUUID().toString();
    private static final String CONVERSATION_ID_UPPER = CONVERSATION_ID.toUpperCase(Locale.ROOT);

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JmsReader jmsReader;

    @Mock
    private XPathService xPathService;

    @Mock
    private Message message;

    @Mock
    private Document ebXmlDocument;

    @Mock
    private MDCService mdcService;

    @Mock
    private EhrExtractMessageHandler ehrExtractMessageHandler;

    @Mock
    private AcknowledgmentMessageHandler acknowledgmentMessageHandler;

    @Mock
    private MigrationStatusLogService migrationStatusLogService;

    @Mock
    private PatientMigrationRequestService migrationRequestService;

    @InjectMocks
    private MhsQueueMessageHandler mhsQueueMessageHandler;

    private InboundMessage inboundMessage;

    @Test
    public void handleEhrExtractMessageWithoutErrorsShouldReturnTrue()
        throws
        JsonProcessingException,
        JAXBException,
        InlineAttachmentProcessingException,
        BundleMappingException,
        AttachmentNotFoundException,
        ParseException,
        SAXException, TransformerException {

        inboundMessage = new InboundMessage();
        prepareMocks(EHR_EXTRACT_INTERACTION_ID);
        when(migrationRequestService.hasMigrationRequest(any())).thenReturn(true);

        boolean result = mhsQueueMessageHandler.handleMessage(message);

        assertTrue(result);
        verify(mdcService).applyConversationId(CONVERSATION_ID_UPPER);
        verify(ehrExtractMessageHandler).handleMessage(inboundMessage, CONVERSATION_ID_UPPER);
        verifyNoInteractions(acknowledgmentMessageHandler);
    }

    @Test
    public void handleEhrExtractMessageWhenEhrExtractMessageHandlerThrowsErrorShouldReturnFalse()
        throws
        JAXBException,
        JsonProcessingException,
        InlineAttachmentProcessingException,
        BundleMappingException,
        AttachmentNotFoundException,
        ParseException,
        SAXException, TransformerException {

        inboundMessage = new InboundMessage();
        prepareMocks(EHR_EXTRACT_INTERACTION_ID);
        when(migrationRequestService.hasMigrationRequest(any())).thenReturn(true);
        doThrow(new JAXBException("Nobody expects the spanish inquisition!"))
            .when(ehrExtractMessageHandler).handleMessage(inboundMessage, CONVERSATION_ID_UPPER);

        boolean result = mhsQueueMessageHandler.handleMessage(message);

        assertFalse(result);
        verify(mdcService).applyConversationId(CONVERSATION_ID_UPPER);
        verifyNoInteractions(acknowledgmentMessageHandler);
    }

    @Test
    public void handleEhrExtractMessageWhenEhrExtractMessageHandlerThrowsErrorShouldLogAMigrationStatusGeneralError()
        throws
        JAXBException,
        JsonProcessingException,
        InlineAttachmentProcessingException,
        BundleMappingException,
        AttachmentNotFoundException,
        ParseException,
        SAXException, TransformerException {

        inboundMessage = new InboundMessage();
        prepareMocks(EHR_EXTRACT_INTERACTION_ID);
        when(migrationRequestService.hasMigrationRequest(any())).thenReturn(true);
        doThrow(new JAXBException("Nobody expects the spanish inquisition!"))
            .when(ehrExtractMessageHandler).handleMessage(inboundMessage, CONVERSATION_ID_UPPER);

        boolean result = mhsQueueMessageHandler.handleMessage(message);

        assertFalse(result);
        verify(migrationStatusLogService)
            .addMigrationStatusLog(MigrationStatus.EHR_GENERAL_PROCESSING_ERROR,
                                   CONVERSATION_ID_UPPER,
                                   null,
                                   UNEXPECTED_CONDITION.getCode());

    }

    @Test
    public void handleAcknowledgeMessageWithoutErrorsShouldReturnTrue() throws SAXException {
        inboundMessage = new InboundMessage();
        prepareMocks(ACKNOWLEDGEMENT_INTERACTION_ID);
        when(migrationRequestService.hasMigrationRequest(any())).thenReturn(true);

        boolean result = mhsQueueMessageHandler.handleMessage(message);

        assertTrue(result);
        verify(mdcService).applyConversationId(CONVERSATION_ID_UPPER);
        verify(acknowledgmentMessageHandler).handleMessage(inboundMessage, CONVERSATION_ID_UPPER);
        verifyNoInteractions(ehrExtractMessageHandler);
    }

    @Test
    public void handleAcknowledgeMessageWhenAcknowledgmentMessageHandlerThrowsErrorShouldReturnFalse() throws SAXException {
        inboundMessage = new InboundMessage();
        prepareMocks(ACKNOWLEDGEMENT_INTERACTION_ID);
        when(migrationRequestService.hasMigrationRequest(any())).thenReturn(true);
        doThrow(new SAXException("Nobody expects the spanish inquisition!"))
            .when(acknowledgmentMessageHandler).handleMessage(inboundMessage, CONVERSATION_ID_UPPER);

        boolean result = mhsQueueMessageHandler.handleMessage(message);

        assertFalse(result);
        verify(mdcService).applyConversationId(CONVERSATION_ID_UPPER);
        verify(acknowledgmentMessageHandler).handleMessage(inboundMessage, CONVERSATION_ID_UPPER);
        verifyNoInteractions(ehrExtractMessageHandler);
    }

    @Test
    public void handleMessageWithUnsupportedInteractionIdShouldReturnTrue() {
        inboundMessage = new InboundMessage();
        prepareMocks(UNKNOWN_INTERACTION_ID);
        when(migrationRequestService.hasMigrationRequest(any())).thenReturn(true);

        boolean result = mhsQueueMessageHandler.handleMessage(message);

        assertTrue(result);
        verify(mdcService).applyConversationId(CONVERSATION_ID_UPPER);
        verifyNoInteractions(acknowledgmentMessageHandler);
        verifyNoInteractions(ehrExtractMessageHandler);
    }

    @Test
    public void handleMessageWhenObjectMapperThrowsErrorShouldReturnFalse() throws JMSException, JsonProcessingException {
        when(jmsReader.readMessage(message)).thenReturn(INBOUND_MESSAGE_STRING);
        when(objectMapper.readValue(INBOUND_MESSAGE_STRING, InboundMessage.class)).thenThrow(new JsonMappingException(null, "hello"));

        boolean result = mhsQueueMessageHandler.handleMessage(message);

        assertFalse(result);
        verifyNoInteractions(acknowledgmentMessageHandler);
        verifyNoInteractions(ehrExtractMessageHandler);
        verifyNoInteractions(mdcService);
    }

    @Test
    public void When_HandleMessage_WithConversationIdNotFoundAndAckInteractionId_Expect_ExceptionThrown() {
        inboundMessage = new InboundMessage();
        prepareMocks(ACKNOWLEDGEMENT_INTERACTION_ID);

        when(migrationRequestService.hasMigrationRequest(any())).thenReturn(false);

        assertThatThrownBy(() -> mhsQueueMessageHandler.handleMessage(message))
            .isInstanceOf(ConversationIdNotFoundException.class);
    }

    @Test
    public void When_HandleMessage_WithConversationIdNotFoundAndCopcInteractionId_Expect_ExceptionThrown() {
        inboundMessage = new InboundMessage();
        prepareMocks(COPC_INTERACTION_ID);

        when(migrationRequestService.hasMigrationRequest(any())).thenReturn(false);

        assertThatThrownBy(() -> mhsQueueMessageHandler.handleMessage(message))
            .isInstanceOf(ConversationIdNotFoundException.class);
    }

    @Test
    public void When_HandleMessage_WithConversationIdNotFoundAndRequestInteractionId_Expect_ExceptionThrown() {
        inboundMessage = new InboundMessage();
        prepareMocks(EHR_EXTRACT_REQUEST_INTERACTION_ID);

        when(migrationRequestService.hasMigrationRequest(any())).thenReturn(false);

        assertThatThrownBy(() -> mhsQueueMessageHandler.handleMessage(message))
            .isInstanceOf(ConversationIdNotFoundException.class);
    }

    @SneakyThrows
    private void prepareMocks(String interactionId) {
        String ebXmlString = "<xml>";
        inboundMessage.setPayload(readInboundMessagePayloadFromFile());
        inboundMessage.setEbXML(ebXmlString);
        when(jmsReader.readMessage(message)).thenReturn(INBOUND_MESSAGE_STRING);
        when(objectMapper.readValue(INBOUND_MESSAGE_STRING, InboundMessage.class)).thenReturn(inboundMessage);
        when(xPathService.parseDocumentFromXml(ebXmlString)).thenReturn(ebXmlDocument);
        when(xPathService.getNodeValue(ebXmlDocument, CONVERSATION_ID_PATH)).thenReturn(CONVERSATION_ID);
        when(xPathService.getNodeValue(ebXmlDocument, INTERACTION_ID_PATH)).thenReturn(interactionId);
    }

    @SneakyThrows
    private String readInboundMessagePayloadFromFile() {
        return readResourceAsString("/xml/inbound_message_payload.xml").replace("{{nhsNumber}}", NHS_NUMBER);
    }
}
