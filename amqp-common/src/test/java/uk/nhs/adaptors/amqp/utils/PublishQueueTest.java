package uk.nhs.adaptors.amqp.utils;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

import uk.nhs.adaptors.amqp.PublishQueue;
import uk.nhs.adaptors.amqp.SubscribeQueue;
import uk.nhs.adaptors.amqp.task.TaskDefinition;
import uk.nhs.adaptors.amqp.task.TestTask;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PublishQueueTest {

    @Autowired
    private PublishQueue publishQueue;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private SubscribeQueue subscribeQueue;
    
    @Mock
    private JmsTemplate jmsTemplate;
    
    @Mock
    private Session session;
    
    @Mock
    private TextMessage textMessage;
    
    private final String TEST_PAYLOAD = "{\"taskId\":\"123\",\"requestId\":null,\"conversationId\":null," +
        "\"toAsid\":null,\"fromAsid\":null,\"fromOdsCode\":null,\"toOdsCode\":null," +
        "\"taskType\":\"TEST_TASK\"}";

    @Test
    @SneakyThrows
    public void When_TaskIsSentToGpcFacadeQueue_Expect_MessageIsSentToQueue() {
        TaskDefinition taskDefinition = TestTask.builder()
            .taskId("123")
            .build();
        publishQueue.sendToGpcFacadeQueue(taskDefinition);
        when(session.createTextMessage(TEST_PAYLOAD)).thenReturn(textMessage);

        var messageCreatorArgumentCaptor = ArgumentCaptor.forClass(MessageCreator.class);
        verify(jmsTemplate, times(1))
            .send(ArgumentMatchers.anyString(), messageCreatorArgumentCaptor.capture());
        messageCreatorArgumentCaptor.getValue().createMessage(session);
        verify(textMessage).setStringProperty(TEST_PAYLOAD, "TEST");
    }
    
    //send to mhs
}
