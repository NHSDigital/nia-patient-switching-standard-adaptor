package uk.nhs.adaptors.pss.translator.amqp;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import jakarta.jms.Message;
import jakarta.jms.Session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.config.MhsQueueProperties;
import uk.nhs.adaptors.pss.translator.exception.ConversationIdNotFoundException;
import uk.nhs.adaptors.pss.translator.task.MhsQueueMessageHandler;

@ExtendWith(MockitoExtension.class)
public class MhsDaisyChainingQueueConsumerTest {

    private static final int MAX_REDELIVERIES = 3;
    private static final int FIRST_DELIVERY = 1;

    private static final String DELIVERY_COUNT_PROPERTY = "JMSXDeliveryCount";
    @Mock
    private MhsQueueMessageHandler mhsQueueMessageHandler;
    @Mock
    private MhsDlqPublisher mhsDlqPublisher;
    @Mock
    private MhsQueueProperties mhsQueueProperties;
    @Mock
    private Gp2GpQueuePublisher gp2GpQueuePublisher;
    @Mock
    private Message message;
    @Mock
    private Session session;
    @InjectMocks
    private MhsDaisyChainingQueueConsumer mhsQueueConsumer;

    @Test
    @SneakyThrows
    public void When_Receive_WithSuccess_Expect_MessageAcknowledged() {
        when(message.getJMSMessageID()).thenReturn(UUID.randomUUID().toString());
        when(message.getIntProperty(DELIVERY_COUNT_PROPERTY)).thenReturn(FIRST_DELIVERY);
        when(mhsQueueMessageHandler.handleMessage(message)).thenReturn(true);

        mhsQueueConsumer.receive(message, session);

        verify(message, times(1)).acknowledge();
        verify(mhsDlqPublisher, times(0)).sendToMhsDlq(message);
    }

    @Test
    @SneakyThrows
    public void When_Receive_WithFailure_Expect_MessageSentToDLQ() {
        when(message.getJMSMessageID()).thenReturn(UUID.randomUUID().toString());
        when(message.getIntProperty(DELIVERY_COUNT_PROPERTY)).thenReturn(FIRST_DELIVERY);
        when(mhsQueueMessageHandler.handleMessage(message)).thenReturn(false);

        mhsQueueConsumer.receive(message, session);

        verify(message, times(0)).acknowledge();
        verify(mhsDlqPublisher, times(1)).sendToMhsDlq(message);
    }

    @Test
    @SneakyThrows
    public void When_ConversationIdNotFound_WithDaisyChainingAndMaxRedeliveriesReached_Expect_MessageSentToGp2GpQueue() {
        when(mhsQueueMessageHandler.handleMessage(message)).thenThrow(ConversationIdNotFoundException.class);
        when(message.getJMSMessageID()).thenReturn(UUID.randomUUID().toString());
        when(message.getIntProperty(DELIVERY_COUNT_PROPERTY)).thenReturn(MAX_REDELIVERIES + 1);
        when(mhsQueueProperties.getMaxRedeliveries()).thenReturn(MAX_REDELIVERIES);

        mhsQueueConsumer.receive(message, session);

        verify(gp2GpQueuePublisher, times(1)).sendToGp2GpAdaptor(message);
    }


    @Test
    @SneakyThrows
    public void When_ConversationIdNotFound_WithDaisyChainingAndMaxRedeliveriesNotReached_Expect_SessionRolledBack() {
        when(mhsQueueMessageHandler.handleMessage(message)).thenThrow(ConversationIdNotFoundException.class);
        when(message.getJMSMessageID()).thenReturn(UUID.randomUUID().toString());
        when(message.getIntProperty(DELIVERY_COUNT_PROPERTY)).thenReturn(MAX_REDELIVERIES);
        when(mhsQueueProperties.getMaxRedeliveries()).thenReturn(MAX_REDELIVERIES);

        mhsQueueConsumer.receive(message, session);

        verify(gp2GpQueuePublisher, times(0)).sendToGp2GpAdaptor(message);
        verify(session, times(1)).rollback();
    }
}
