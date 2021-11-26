package uk.nhs.adaptors.gpc;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.nhs.adaptors.gpc.amqp.PssQueuePublisher;

@ExtendWith({SpringExtension.class})
@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext
public class PssQueuePublisherTest {
    @Autowired
    private PssQueuePublisher pssQueuePublisher;

    @Test
    public void When_TaskIsSentToPssQueue_Expect_MessageIsSentToQueue() {
        pssQueuePublisher.sendToPssQueue("test message 3 ");
    }
}
