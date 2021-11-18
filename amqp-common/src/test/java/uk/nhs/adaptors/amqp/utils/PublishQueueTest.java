package uk.nhs.adaptors.amqp.utils;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
    private ObjectMapper objectMapper;
    
    @Autowired
    private SubscribeQueue subscribeQueue;

    @Test
    @SneakyThrows
    public void testTaskSend() {
        TaskDefinition taskDefinition = TestTask.builder()
            .taskId("123")
            .build();
        publishQueue.sendToGpcFacadeQueue(taskDefinition);

        taskDefinition = TestTask.builder()
            .taskId("456")
            .build();
        publishQueue.sendToMhsAdaptorQueue(taskDefinition);
    }

    @Test
    @SneakyThrows
    public void When_OneTaskIsSentToGpcFacadeQueue_Expect_MessageIsDequeued() {
        TaskDefinition taskDefinition = TestTask.builder()
            .taskId("123")
            .build();
        
        publishQueue.sendToGpcFacadeQueue(taskDefinition);
        
        verify(subscribeQueue, times(1)).subscribeToQueue(ArgumentMatchers.any());
    }
    
    //send to mhs
}
