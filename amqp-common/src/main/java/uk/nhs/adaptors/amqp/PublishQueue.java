package uk.nhs.adaptors.amqp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.nhs.adaptors.amqp.task.TaskDefinition;

public class PublishQueue {
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${amqp.gpcFacadeQueue}")
    private String gpcFacadeQueue;
    @Value("${amqp.mhsAdaptorQueue}")
    private String mhsAdaptorQueue;

    private void sendToQueue(String destination, String messageContent) throws JmsException {
        jmsTemplate.send(destination, session -> session.createTextMessage(messageContent));
    }

    public void sendToGpcFacadeQueue(TaskDefinition taskDefinition) throws JsonProcessingException {
        String messagePayload = objectMapper.writeValueAsString(taskDefinition);
        sendToQueue(gpcFacadeQueue, messagePayload);
    }

    public void sendToMhsAdaptorQueue(TaskDefinition taskDefinition) throws JsonProcessingException {
        String messagePayload = objectMapper.writeValueAsString(taskDefinition);
        sendToQueue(mhsAdaptorQueue, messagePayload);
    }
}
