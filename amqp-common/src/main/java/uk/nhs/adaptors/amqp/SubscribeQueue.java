package uk.nhs.adaptors.amqp;

import javax.jms.Message;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import uk.nhs.adaptors.amqp.task.TestTask;

@Component
@AllArgsConstructor
public class SubscribeQueue {
    private final ObjectMapper objectMapper;

    @JmsListener(destination = "${amqp.gpcFacadeQueue}")
    @JmsListener(destination = "${amqp.mhsAdaptorQueue}")
    @SneakyThrows
    public void subscribeToQueue(Message message) {
        /* Do Something
            example.
            String payload = ((TextMessage) message).getText();
            TestTask testTask = objectMapper.readValue(payload, TestTask.class);
            System.out.println("Received message = TaskID:" + testTask.getTaskId());
         */
    }
}
