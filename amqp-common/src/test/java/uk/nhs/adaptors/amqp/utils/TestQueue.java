package uk.nhs.adaptors.pss.translator.amqp.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import lombok.SneakyThrows;

import uk.nhs.adaptors.pss.translator.amqp.PublishQueue;
import uk.nhs.adaptors.pss.translator.amqp.task.TaskDefinition;
import uk.nhs.adaptors.pss.translator.amqp.task.TestTask;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class TestQueue {
    @Autowired
    private PublishQueue publishQueue;
    
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