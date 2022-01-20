package uk.nhs.adaptors.pss.translator.task;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import javax.jms.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class QueueMessageHandler {

    private final SendEhrExtractRequestHandler sendEhrExtractRequestHandler;

    @SneakyThrows
    public boolean handle(Message message) {
        LOGGER.info("Handling message with message_id=[{}]", message.getJMSMessageID());
        return sendEhrExtractRequestHandler.prepareAndSendRequest(message);
    }

}
