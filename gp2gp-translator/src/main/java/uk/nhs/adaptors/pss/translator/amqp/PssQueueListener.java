package uk.nhs.adaptors.pss.translator.amqp;

import javax.jms.Message;
import javax.jms.TextMessage;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.amqp.task.TaskDefinition;

@Component
@AllArgsConstructor
public class PssQueueListener {
    private final ObjectMapper objectMapper;

    @JmsListener(destination = "${amqp.pss.queueName}", containerFactory = "pssQueueJmsListenerFactory")
    @SneakyThrows
    public void listenToPssQueue(Message message) {
        //        TODO: Do Something
        //        eg.
                String payload = ((TextMessage) message).getText();
                TaskDefinition testTask = objectMapper.readValue(payload, TaskDefinition.class);
                System.out.println("Pss Queue Received message = TaskName:" + testTask.getTaskName());
    }
}
