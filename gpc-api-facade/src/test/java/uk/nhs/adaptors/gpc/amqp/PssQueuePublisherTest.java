package uk.nhs.adaptors.gpc.amqp;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import uk.nhs.adaptors.gpc.GpcFacadeApplication;
import uk.nhs.adaptors.gpc.amqp.task.TaskDefinition;
import uk.nhs.adaptors.gpc.amqp.task.TaskHandlerException;
import uk.nhs.adaptors.gpc.config.PssQueueProperties;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {GpcFacadeApplication.class})
public class PssQueuePublisherTest {
    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private Session session;

    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private PssQueueProperties pssQueueProperties;

    @InjectMocks
    private PssQueuePublisher pssQueuePublisher;

    private final String TEST_PAYLOAD = "{\"taskName\":\"123\"}";

    @Test
    @SneakyThrows
    public void When_TaskIsSentToPssQueue_Expect_MessageIsSentToQueue() {
        TaskDefinition taskDefinition = mock(TaskDefinition.class);
        String queueName = "testQueue";

        when(objectMapper.writeValueAsString(taskDefinition)).thenReturn(TEST_PAYLOAD);
        when(pssQueueProperties.getQueueName()).thenReturn(queueName);

        pssQueuePublisher.sendToPssQueue(taskDefinition);

        var messageCreatorArgumentCaptor = ArgumentCaptor.forClass(MessageCreator.class);
        verify(jmsTemplate).send(messageCreatorArgumentCaptor.capture());

        messageCreatorArgumentCaptor.getValue().createMessage(session);

        verify(session, times(1)).createTextMessage(TEST_PAYLOAD);
    }

    @Test
    @SneakyThrows
    public void When_PssQueueTaskNotParsed_Expect_ExceptionThrown() {
        TaskDefinition taskDefinition = mock(TaskDefinition.class);
        String queueName = "testQueue";

        doThrow(JsonProcessingException.class)
            .when(objectMapper).writeValueAsString(taskDefinition);
        when(pssQueueProperties.getQueueName()).thenReturn(queueName);

        assertThatExceptionOfType(TaskHandlerException.class)
            .isThrownBy(() -> pssQueuePublisher.sendToPssQueue(taskDefinition))
            .withMessageContaining("Unable to serialise task definition to JSON");

        verifyNoInteractions(jmsTemplate);
    }
}
