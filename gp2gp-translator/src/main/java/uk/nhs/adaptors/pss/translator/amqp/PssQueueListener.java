package uk.nhs.adaptors.pss.translator.amqp;

import javax.jms.Message;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Component
@AllArgsConstructor
public class PssQueueListener {
    private final ObjectMapper objectMapper;

    @JmsListener(destination = "${amqp.pss.queueName}", containerFactory = "pssQueueJmsListenerFactory")
    @SneakyThrows
    public void listenToPssQueue(Message message) {
        //        TODO: Do Something
        //        eg.
        //        String payload = ((TextMessage) message).getText();
        //        TestTask testTask = objectMapper.readValue(payload, TestTask.class);
        //        System.out.println("Pss Queue Received message = TaskName:" + testTask.getTaskName());
    }
}
