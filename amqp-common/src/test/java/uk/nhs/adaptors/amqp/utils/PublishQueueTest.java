package uk.nhs.adaptors.amqp.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PublishQueueTest {

    @Autowired
    private PublishQueue publishQueue;

    @Test
    public void testReceive() throws Exception {
        publishQueue.sendToQueue("Hello Spring JMS ActiveMQ!");
    }
}
