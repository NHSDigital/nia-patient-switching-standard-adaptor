package uk.nhs.adaptors.pss.translator.task;

import javax.jms.JMSException;
import javax.jms.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.common.model.TransferRequestMessage;
import uk.nhs.adaptors.common.service.MDCService;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class QueueMessageHandler {
    private final SendEhrExtractRequestHandler sendEhrExtractRequestHandler;
    private final ObjectMapper objectMapper;
    private final MDCService mdcService;

    @SneakyThrows
    public boolean handle(Message message) {
        var messageId = message.getJMSMessageID();
        LOGGER.info("Handling message with message_id=[{}]", messageId);
        try {
            TransferRequestMessage transferRequest = objectMapper.readValue(message.getBody(String.class), TransferRequestMessage.class);
            mdcService.applyConversationId(transferRequest.getConversationId());
            return sendEhrExtractRequestHandler.prepareAndSendRequest(transferRequest);
        } catch (JMSException e) {
            LOGGER.error("Error while processing PSSQueue message_id=[{}]", messageId, e);
            return false;
        }
    }
}
