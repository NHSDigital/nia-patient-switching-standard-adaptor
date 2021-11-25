package uk.nhs.adaptors.gpc.amqp;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jms.Session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.gpc.GpcFacadeApplication;
import uk.nhs.adaptors.gpc.config.PssQueueProperties;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {GpcFacadeApplication.class})
public class PssQueuePublisherTest {
    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private Session session;

    @Mock
    private PssQueueProperties pssQueueProperties;

    @InjectMocks
    private PssQueuePublisher pssQueuePublisher;

    @Test
    @SneakyThrows
    public void When_TaskIsSentToPssQueue_Expect_MessageIsSentToQueue() {
        String message = "Test Message";
        String queueName = "testQueue";

        when(pssQueueProperties.getQueueName()).thenReturn(queueName);

        pssQueuePublisher.sendToPssQueue(message);

        var messageCreatorArgumentCaptor = ArgumentCaptor.forClass(MessageCreator.class);
        verify(jmsTemplate).send(messageCreatorArgumentCaptor.capture());

        messageCreatorArgumentCaptor.getValue().createMessage(session);

        verify(session, times(1)).createTextMessage(message);
    }
}
