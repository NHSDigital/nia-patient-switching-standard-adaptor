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
    private final MhsDlqPublisher mhsDlqPublisher;

    @JmsListener(destination = "${amqp.mhs.queueName}", containerFactory = "mhsQueueJmsListenerFactory")
    @SneakyThrows
    public void receive(Message message) {
        String messageId = message.getJMSMessageID();
        int deliveryCount = message.getIntProperty("JMSXDeliveryCount");
        LOGGER.debug("Received a message from MHSQueue, message_id=[{}], delivery_count=[{}]", messageId, deliveryCount);
        if (mhsQueueMessageHandler.handleMessage(message)) {
            message.acknowledge();
            LOGGER.debug("Acknowledged MHSQueue message_id=[{}]", messageId);
        } else {
            LOGGER.debug("Sending message_id=[{}] to the dead letter queue", messageId);
            mhsDlqPublisher.sendToMhsDlq(message);
        }
    }
}
