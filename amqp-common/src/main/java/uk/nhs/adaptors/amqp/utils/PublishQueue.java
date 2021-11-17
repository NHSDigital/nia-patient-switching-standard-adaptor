package uk.nhs.adaptors.amqp.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.nhs.adaptors.amqp.utils.task.TaskDefinition;

public class PublishQueue {
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${amqp.taskQueueName}")
    private String queueName;

    public void sendToQueue(String messageContent) throws JmsException {
        jmsTemplate.send(queueName, session -> session.createTextMessage(messageContent));
    }

    public void sendTask(TaskDefinition taskDefinition) throws JsonProcessingException {
        String messagePayload = objectMapper.writeValueAsString(taskDefinition);
        sendToQueue(messagePayload);
    }
}
