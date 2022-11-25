package uk.nhs.adaptors.pss.translator.amqp;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.pss.translator.config.MhsQueueProperties;
import uk.nhs.adaptors.pss.translator.task.MhsQueueMessageHandler;

import org.jdbi.v3.core.ConnectionException;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MhsQueueConsumer {
    private final MhsQueueMessageHandler mhsQueueMessageHandler;
    private final MhsQueueProperties queueProperties;

    @JmsListener(destination = "${amqp.mhs.queueName}", containerFactory = "mhsQueueJmsListenerFactory")
    @SneakyThrows
    public void receive(Message message, Session session) {
        String messageId = message.getJMSMessageID();
        int deliveryCount = message.getIntProperty("JMSXDeliveryCount");
        LOGGER.debug("Received a message from MHSQueue, message_id=[{}], delivery_count=[{}]", messageId, deliveryCount);
        try {
            if (mhsQueueMessageHandler.handleMessage(message)) {
                message.acknowledge();
                LOGGER.debug("Acknowledged MHSQueue message_id=[{}]", messageId);
            } else {
                rollbackSession(session, messageId, deliveryCount);
            }
        } catch (ConnectionException e) {
            LOGGER.trace("Caught exception of type [{}] and re-throwing for error handler", e.getClass().toString());
            throw e;
        } catch (Exception e) {
            LOGGER.error("Caught unexpected exception of type [{}]", e.getClass().toString());
            rollbackSession(session, messageId, deliveryCount);
            e.printStackTrace();
        }
    }

    private void rollbackSession(Session session, String messageId, int deliveryCount) throws JMSException {

        if (deliveryCount > queueProperties.getMaxRedeliveries()) {
            LOGGER.error("Unable to handle message_id=[{}], max redeliveries reached", messageId);
        } else {
            LOGGER.error("Unable to handle message_id=[{}], rolling back JMS session", messageId);
        }

        session.rollback();
    }
}
