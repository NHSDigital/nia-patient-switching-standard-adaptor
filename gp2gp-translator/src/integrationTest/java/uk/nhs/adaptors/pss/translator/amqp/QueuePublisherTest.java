package uk.nhs.adaptors.pss.translator.amqp;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.nhs.adaptors.pss.translator.amqp.task.TaskDefinition;
import uk.nhs.adaptors.pss.translator.amqp.task.TestTask;

@ExtendWith({SpringExtension.class})
@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext
public class QueuePublisherTest {
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