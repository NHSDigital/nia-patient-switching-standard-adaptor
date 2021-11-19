package uk.nhs.adaptors.amqp.utils;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
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

import uk.nhs.adaptors.amqp.AmqpProperties;
import uk.nhs.adaptors.amqp.PublishQueue;
import uk.nhs.adaptors.amqp.task.TaskDefinition;
import uk.nhs.adaptors.amqp.task.TaskHandlerException;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PublishQueueTest {
    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private Session session;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AmqpProperties amqpProperties;

    @InjectMocks
    private PublishQueue publishQueue;

    private final String TEST_PAYLOAD = "{\"taskId\":\"123\",\"requestId\":null,\"conversationId\":null," +
        "\"toAsid\":null,\"fromAsid\":null,\"fromOdsCode\":null,\"toOdsCode\":null," +
        "\"taskType\":\"TEST_TASK\"}";

    @Test
    @SneakyThrows
    public void When_TaskIsSentToGpcFacadeQueue_Expect_MessageIsSentToQueue() {
        TaskDefinition taskDefinition = mock(TaskDefinition.class);
        String queueName = "testQueue";

        when(objectMapper.writeValueAsString(taskDefinition)).thenReturn(TEST_PAYLOAD);
        when(amqpProperties.getGpcFacadeQueue()).thenReturn(queueName);

        publishQueue.sendToGpcFacadeQueue(taskDefinition);

        var messageCreatorArgumentCaptor = ArgumentCaptor.forClass(MessageCreator.class);
        verify(jmsTemplate).send(eq(queueName), messageCreatorArgumentCaptor.capture());

        messageCreatorArgumentCaptor.getValue().createMessage(session);

        verify(session, times(1)).createTextMessage(TEST_PAYLOAD);
    }

    @Test
    @SneakyThrows
    public void When_GpcFacadeTaskNotParsed_Expect_ExceptionThrown() {
        TaskDefinition taskDefinition = mock(TaskDefinition.class);
        String queueName = "testQueue";

        doThrow(JsonProcessingException.class)
            .when(objectMapper).writeValueAsString(taskDefinition);
        when(amqpProperties.getGpcFacadeQueue()).thenReturn(queueName);

        assertThatExceptionOfType(TaskHandlerException.class)
            .isThrownBy(() -> publishQueue.sendToGpcFacadeQueue(taskDefinition))
            .withMessageContaining("Unable to serialise task definition to JSON");

        verifyNoInteractions(jmsTemplate);
    }

    @Test
    @SneakyThrows
    public void When_TaskIsSentToMhsAdaptorQueue_Expect_MessageIsSentToQueue() {
        TaskDefinition taskDefinition = mock(TaskDefinition.class);
        String queueName = "testQueue";

        when(objectMapper.writeValueAsString(taskDefinition)).thenReturn(TEST_PAYLOAD);
        when(amqpProperties.getMhsAdaptorQueue()).thenReturn(queueName);

        publishQueue.sendToMhsAdaptorQueue(taskDefinition);

        var messageCreatorArgumentCaptor = ArgumentCaptor.forClass(MessageCreator.class);
        verify(jmsTemplate).send(eq(queueName), messageCreatorArgumentCaptor.capture());

        messageCreatorArgumentCaptor.getValue().createMessage(session);

        verify(session, times(1)).createTextMessage(TEST_PAYLOAD);
    }

    @Test
    @SneakyThrows
    public void When_MhsAdaptorTaskNotParsed_Expect_ExceptionThrown() {
        TaskDefinition taskDefinition = mock(TaskDefinition.class);
        String queueName = "testQueue";

        doThrow(JsonProcessingException.class)
            .when(objectMapper).writeValueAsString(taskDefinition);
        when(amqpProperties.getMhsAdaptorQueue()).thenReturn(queueName);

        assertThatExceptionOfType(TaskHandlerException.class)
            .isThrownBy(() -> publishQueue.sendToMhsAdaptorQueue(taskDefinition))
            .withMessageContaining("Unable to serialise task definition to JSON");

        verifyNoInteractions(jmsTemplate);
    }
}
