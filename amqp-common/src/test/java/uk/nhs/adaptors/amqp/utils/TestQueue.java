package uk.nhs.adaptors.amqp.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

import uk.nhs.adaptors.amqp.PublishQueue;
import uk.nhs.adaptors.amqp.task.TaskDefinition;
import uk.nhs.adaptors.amqp.task.TestTask;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class TestQueue {

    @Autowired
    private PublishQueue publishQueue;
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    
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
}