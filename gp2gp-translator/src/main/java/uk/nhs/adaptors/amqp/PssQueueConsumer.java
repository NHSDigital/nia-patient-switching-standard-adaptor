package uk.nhs.adaptors.amqp;

import javax.jms.Message;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.pss.translator.task.QueueMessageHandler;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PssQueueConsumer {

    private final QueueMessageHandler queueMessageHandler;

    @JmsListener(destination = "${amqp.pss.queueName}", containerFactory = "pssQueueJmsListenerFactory")
    @SneakyThrows
    public void receive(Message message) {
        String messageId = message.getJMSMessageID();
        LOGGER.debug("Received a message from PSSQueue, message_id=[{}], body=[{}]", messageId, ((TextMessage) message).getText());
        if (queueMessageHandler.handle(message)) {
            message.acknowledge();
            LOGGER.debug("Acknowledged PSSQueue message_id=[{}]", messageId);
        } else {
            LOGGER.debug("Leaving Message of message_id=[{}] on the PSSQueue", messageId);
        }
    }
}
