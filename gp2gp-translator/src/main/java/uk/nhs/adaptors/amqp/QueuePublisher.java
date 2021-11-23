package uk.nhs.adaptors.amqp;

import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.amqp.task.TaskDefinition;
import uk.nhs.adaptors.amqp.task.TaskHandlerException;

@Component
@AllArgsConstructor
public class QueuePublisher {
    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final PssQueueProperties pssQueueProperties;
    private final MhsQueueProperties mhsQueueProperties;

    private void sendToQueue(String destination, TaskDefinition taskDefinition) throws JmsException {
        try {
            String messagePayload = objectMapper.writeValueAsString(taskDefinition);
            jmsTemplate.send(destination, session -> session.createTextMessage(messagePayload));
        } catch (JsonProcessingException e) {
            throw new TaskHandlerException("Unable to serialise task definition to JSON", e);
        }
    }

    public void sendToPssQueue(TaskDefinition taskDefinition) {
        sendToQueue(pssQueueProperties.getQueueName(), taskDefinition);
    }

    public void sendToMhsQueue(TaskDefinition taskDefinition) {
        sendToQueue(mhsQueueProperties.getQueueName(), taskDefinition);
    }
}
