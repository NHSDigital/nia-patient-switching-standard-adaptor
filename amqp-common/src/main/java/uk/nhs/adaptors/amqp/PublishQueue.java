package uk.nhs.adaptors.amqp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.nhs.adaptors.amqp.task.TaskDefinition;
import uk.nhs.adaptors.amqp.task.TaskHandlerException;

public class PublishQueue {
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${amqp.gpcFacadeQueue}")
    private String gpcFacadeQueue;
    @Value("${amqp.mhsAdaptorQueue}")
    private String mhsAdaptorQueue;

    private void sendToQueue(String destination, TaskDefinition taskDefinition) throws JmsException {
        try {
            String messagePayload = objectMapper.writeValueAsString(taskDefinition);
            jmsTemplate.send(destination, session -> session.createTextMessage(messagePayload));
        } catch (JsonProcessingException e) {
            throw new TaskHandlerException("Unable to serialise task definition to JSON", e);
        }
    }

    public void sendToGpcFacadeQueue(TaskDefinition taskDefinition) {
        sendToQueue(gpcFacadeQueue, taskDefinition);
    }

    public void sendToMhsAdaptorQueue(TaskDefinition taskDefinition) {
        sendToQueue(mhsAdaptorQueue, taskDefinition);
    }
}
