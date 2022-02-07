package uk.nhs.adaptors.pss.translator.amqp;

import javax.jms.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.pss.translator.task.MhsQueueMessageHandler;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MhsQueueConsumer {
    private final MhsQueueMessageHandler mhsQueueMessageHandler;

    @JmsListener(destination = "${amqp.mhs.queueName}", containerFactory = "mhsQueueJmsListenerFactory")
    @SneakyThrows
    public void receive(Message message) {
        String messageId = message.getJMSMessageID();
        LOGGER.debug("Received a message from MSHQueue, message_id=[{}]", messageId);
        if (mhsQueueMessageHandler.handleMessage(message)) {
            message.acknowledge();
            LOGGER.debug("Acknowledged MSHQueue message_id=[{}]", messageId);
            // send continue
        } else {
            LOGGER.debug("Leaving message of message_id=[{}] on the MSHQueue", messageId);
        }
    }
}
