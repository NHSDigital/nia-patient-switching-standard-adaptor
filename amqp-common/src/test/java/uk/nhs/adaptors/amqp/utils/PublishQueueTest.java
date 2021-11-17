package uk.nhs.adaptors.amqp.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import uk.nhs.adaptors.amqp.utils.task.TaskDefinition;
import uk.nhs.adaptors.amqp.utils.task.TaskType;
import uk.nhs.adaptors.amqp.utils.task.TestTask;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PublishQueueTest {

    @Autowired
    private PublishQueue publishQueue;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testSend() {
        publishQueue.sendToQueue("Hello Spring JMS ActiveMQ!");
    }

    @Test
    public void testTaskSend() throws JsonProcessingException {

        TaskDefinition taskDefinition = TestTask.builder()
            .taskId("123")
            .build();

        publishQueue.sendTask(taskDefinition);
    }
}
