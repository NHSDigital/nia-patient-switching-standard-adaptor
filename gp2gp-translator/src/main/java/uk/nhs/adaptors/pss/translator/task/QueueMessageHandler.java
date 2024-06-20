package uk.nhs.adaptors.pss.translator.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.common.model.AcknowledgeRecordMessage;
import uk.nhs.adaptors.common.model.PssQueueMessage;
import uk.nhs.adaptors.common.model.TransferRequestMessage;
import uk.nhs.adaptors.common.service.MDCService;
import uk.nhs.adaptors.pss.translator.service.AcknowledgeRecordService;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class QueueMessageHandler {
    private final SendEhrExtractRequestHandler sendEhrExtractRequestHandler;
    private final AcknowledgeRecordService acknowledgeRecordService;

    private final ObjectMapper objectMapper;
    private final MDCService mdcService;

    @SneakyThrows
    public boolean handle(Message message) {
        var messageId = message.getJMSMessageID();
        LOGGER.info("Handling message with message_id=[{}]", messageId);

        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            var messageBody = message.getBody(String.class);

            var pssQueueMessage = objectMapper.readValue(messageBody, PssQueueMessage.class);
            var conversationId = pssQueueMessage.getConversationId().toUpperCase(Locale.ROOT);
            mdcService.applyConversationId(conversationId);

            switch (pssQueueMessage.getMessageType()) {
                case TRANSFER_REQUEST:
                    var transferRequest = objectMapper.readValue(messageBody, TransferRequestMessage.class);
                    return sendEhrExtractRequestHandler.prepareAndSendRequest(transferRequest);
                case ACKNOWLEDGE_RECORD:
                    var acknowledgeRequest = objectMapper.readValue(messageBody, AcknowledgeRecordMessage.class);
                    return acknowledgeRecordService.prepareAndSendAcknowledgementMessage(acknowledgeRequest);
                default:
                    throw new RuntimeException("Invalid enum value");
            }
        } catch (JMSException e) {
            LOGGER.error("Error while processing PSSQueue message_id=[{}]", messageId, e);
            return false;
        }
    }
}
