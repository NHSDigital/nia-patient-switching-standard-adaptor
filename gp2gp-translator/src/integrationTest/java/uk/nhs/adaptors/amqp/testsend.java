package uk.nhs.adaptors.amqp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.nhs.adaptors.amqp.task.TaskDefinition;
import uk.nhs.adaptors.amqp.task.TestTask;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
public class testsend {
    @Autowired
    private QueuePublisher publishQueue;

    @Test
    public void testTaskSend() {
        TaskDefinition taskDefinition = TestTask.builder()
            .taskName("123")
            .build();
        publishQueue.sendToPssQueue(taskDefinition);

        taskDefinition = TestTask.builder()
            .taskName("456")
            .build();
        publishQueue.sendToMhsQueue(taskDefinition);
    }
}