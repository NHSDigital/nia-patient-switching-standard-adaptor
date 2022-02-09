package uk.nhs.adaptors.pss.translator.task;

import javax.jms.JMSException;
import javax.jms.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.common.model.PssQueueMessage;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class QueueMessageHandler {
    private final SendEhrExtractRequestHandler sendEhrExtractRequestHandler;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public boolean handle(Message message) {
        var messageId = message.getJMSMessageID();
        LOGGER.info("Handling message with message_id=[{}]", messageId);
        try {
            PssQueueMessage pssQueueMessage = objectMapper.readValue(message.getBody(String.class), PssQueueMessage.class);
            return sendEhrExtractRequestHandler.prepareAndSendRequest(pssQueueMessage);
        } catch (JMSException e) {
            LOGGER.error("Error while processing PSSQueue message_id=[{}]", messageId, e);
            return false;
        }
    }
}
