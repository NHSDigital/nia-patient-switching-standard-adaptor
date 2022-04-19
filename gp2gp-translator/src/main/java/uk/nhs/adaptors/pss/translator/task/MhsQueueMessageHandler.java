package uk.nhs.adaptors.pss.translator.task;

import ca.uhn.fhir.parser.DataFormatException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import uk.nhs.adaptors.common.service.MDCService;
import uk.nhs.adaptors.pss.translator.amqp.JmsReader;
import uk.nhs.adaptors.pss.translator.exception.BundleMappingException;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.service.XPathService;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.bind.JAXBException;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MhsQueueMessageHandler {
    private static final String EHR_EXTRACT_INTERACTION_ID = "RCMR_IN030000UK06";
    private static final String ACKNOWLEDGEMENT_INTERACTION_ID = "MCCI_IN010000UK13";
    private static final String CONTINUE_ATTACHMENT_INTERACTION_ID = "COPC_IN000001UK01";
    private static final String CONVERSATION_ID_PATH = "/Envelope/Header/MessageHeader/ConversationId";
    private static final String INTERACTION_ID_PATH = "/Envelope/Header/MessageHeader/Action";

    private final ObjectMapper objectMapper;
    private final JmsReader jmsReader;
    private final XPathService xPathService;
    private final MDCService mdcService;
    private final EhrExtractMessageHandler ehrExtractMessageHandler;
    private final AcknowledgmentMessageHandler acknowledgmentMessageHandler;
    private final AttachmentMessageHandler continueMessageHandler;

    public boolean handleMessage(Message message) {
        try {
            InboundMessage inboundMessage = readMessage(message);
            Document ebXmlDocument = xPathService.parseDocumentFromXml(inboundMessage.getEbXML());
            String conversationId = xPathService.getNodeValue(ebXmlDocument, CONVERSATION_ID_PATH);
            applyConversationId(conversationId);
            String interactionId = xPathService.getNodeValue(ebXmlDocument, INTERACTION_ID_PATH);

            if (ACKNOWLEDGEMENT_INTERACTION_ID.equals(interactionId)) {
                acknowledgmentMessageHandler.handleMessage(inboundMessage, conversationId);
            } else if (EHR_EXTRACT_INTERACTION_ID.equals(interactionId)) {
                ehrExtractMessageHandler.handleMessage(inboundMessage, conversationId);
            } else if(CONTINUE_ATTACHMENT_INTERACTION_ID.equals(interactionId)){
                continueMessageHandler.handleMessage(inboundMessage, conversationId);
            }
            else {
                LOGGER.info("Handling message with [{}] interaction id not implemented", interactionId);
            }
            return true;
        } catch (JMSException | JAXBException | SAXException e) {
            LOGGER.error("Unable to read the content of the inbound MHS message", e);
            return false;
        } catch (JsonProcessingException | DataFormatException e) {
            LOGGER.error("Unable to parse messages for migration status log", e);
            return false;
        } catch (InlineAttachmentProcessingException e) {
            LOGGER.error("Unable to process inline attachments", e);
            return false;
        } catch (BundleMappingException e) {
            LOGGER.error("Unable to map EHR Extract to FHIR bundle", e);
            return false;
        }
    }

    private InboundMessage readMessage(Message message) throws JMSException, JsonProcessingException {
        var body = jmsReader.readMessage(message);
        return objectMapper.readValue(body, InboundMessage.class);
    }

    private void applyConversationId(String conversationId) {
        mdcService.applyConversationId(conversationId);
    }
}
