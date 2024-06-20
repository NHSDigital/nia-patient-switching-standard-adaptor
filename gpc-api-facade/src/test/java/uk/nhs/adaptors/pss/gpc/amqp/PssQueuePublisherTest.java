package uk.nhs.adaptors.pss.gpc.amqp;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import jakarta.jms.Session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import uk.nhs.adaptors.common.model.PssQueueMessage;

@ExtendWith(MockitoExtension.class)
public class PssQueuePublisherTest {
    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private Session session;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PssQueuePublisher pssQueuePublisher;

    @Test
    @SneakyThrows
    public void When_TaskIsSentToPssQueue_Expect_MessageIsSentToQueue() {
        String conversationId = UUID.randomUUID().toString();
        PssQueueMessage pssQueueMessage = PssQueueMessage.builder().conversationId(conversationId).build();
        String message = "message";
        when(objectMapper.writeValueAsString(pssQueueMessage)).thenReturn(message);

        pssQueuePublisher.sendToPssQueue(pssQueueMessage);

        var messageCreatorArgumentCaptor = ArgumentCaptor.forClass(MessageCreator.class);
        verify(jmsTemplate).send(messageCreatorArgumentCaptor.capture());

        messageCreatorArgumentCaptor.getValue().createMessage(session);

        verify(session, times(1)).createTextMessage(message);
    }
}
