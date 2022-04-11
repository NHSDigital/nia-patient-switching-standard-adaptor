package uk.nhs.adaptors.pss.translator.task;

import static java.util.UUID.randomUUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.bind.JAXBException;

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
import uk.nhs.adaptors.pss.translator.amqp.JmsReader;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.service.XPathService;

@ExtendWith(MockitoExtension.class)
public class MhsQueueMessageHandlerTest {
    private static final String NHS_NUMBER = "123456";
    private static final String INBOUND_MESSAGE_STRING = "{hi i'm inbound message}";
    private static final String EHR_EXTRACT_INTERACTION_ID = "RCMR_IN030000UK06";
    private static final String ACKNOWLEDGEMENT_INTERACTION_ID = "MCCI_IN010000UK13";
    private static final String OTHER_INTERACTION_ID = "COPC_IN000001UK01";
    private static final String CONVERSATION_ID_PATH = "/Envelope/Header/MessageHeader/ConversationId";
    private static final String INTERACTION_ID_PATH = "/Envelope/Header/MessageHeader/Action";
    private static final String CONVERSATION_ID = randomUUID().toString();

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

    @InjectMocks
    private MhsQueueMessageHandler mhsQueueMessageHandler;

    private InboundMessage inboundMessage;

    @Test
    public void handleEhrExtractMessageWithoutErrorsShouldReturnTrue() throws JsonProcessingException, JAXBException,
        SAXException, InlineAttachmentProcessingException {
        inboundMessage = new InboundMessage();
        prepareMocks(EHR_EXTRACT_INTERACTION_ID);

        boolean result = mhsQueueMessageHandler.handleMessage(message);

        assertTrue(result);
        verify(mdcService).applyConversationId(CONVERSATION_ID);
        verify(ehrExtractMessageHandler).handleMessage(inboundMessage, CONVERSATION_ID);
        verifyNoInteractions(acknowledgmentMessageHandler);
    }

    @Test
    public void handleEhrExtractMessageWhenEhrExtractMessageHandlerThrowsErrorShouldReturnFalse() throws JAXBException,
        JsonProcessingException, SAXException, InlineAttachmentProcessingException {

        inboundMessage = new InboundMessage();
        prepareMocks(EHR_EXTRACT_INTERACTION_ID);
        doThrow(new JAXBException("Nobody expects the spanish inquisition!"))
            .when(ehrExtractMessageHandler).handleMessage(inboundMessage, CONVERSATION_ID);

        boolean result = mhsQueueMessageHandler.handleMessage(message);

        assertFalse(result);
        verify(mdcService).applyConversationId(CONVERSATION_ID);
        verifyNoInteractions(acknowledgmentMessageHandler);
    }

    @Test
    public void handleAcknowledgeMessageWithoutErrorsShouldReturnTrue() throws SAXException {
        inboundMessage = new InboundMessage();
        prepareMocks(ACKNOWLEDGEMENT_INTERACTION_ID);

        boolean result = mhsQueueMessageHandler.handleMessage(message);

        assertTrue(result);
        verify(mdcService).applyConversationId(CONVERSATION_ID);
        verify(acknowledgmentMessageHandler).handleMessage(inboundMessage, CONVERSATION_ID);
        verifyNoInteractions(ehrExtractMessageHandler);
    }

    @Test
    public void handleAcknowledgeMessageWhenAcknowledgmentMessageHandlerThrowsErrorShouldReturnFalse() throws SAXException {
        inboundMessage = new InboundMessage();
        prepareMocks(ACKNOWLEDGEMENT_INTERACTION_ID);
        doThrow(new SAXException("Nobody expects the spanish inquisition!"))
            .when(acknowledgmentMessageHandler).handleMessage(inboundMessage, CONVERSATION_ID);

        boolean result = mhsQueueMessageHandler.handleMessage(message);

        assertFalse(result);
        verify(mdcService).applyConversationId(CONVERSATION_ID);
        verify(acknowledgmentMessageHandler).handleMessage(inboundMessage, CONVERSATION_ID);
        verifyNoInteractions(ehrExtractMessageHandler);
    }

    @Test
    public void handleMessageWithUnsupportedInteractionIdShouldReturnTrue() {
        inboundMessage = new InboundMessage();
        prepareMocks(OTHER_INTERACTION_ID);

        boolean result = mhsQueueMessageHandler.handleMessage(message);

        assertTrue(result);
        verify(mdcService).applyConversationId(CONVERSATION_ID);
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
