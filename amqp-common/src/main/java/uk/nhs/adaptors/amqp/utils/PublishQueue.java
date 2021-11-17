package uk.nhs.adaptors.amqp.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

public class PublishQueue {
    @Autowired
    private JmsTemplate jmsTemplate;
    @Value("${amqp.taskQueueName}")
    private String queueName;

    public void sendToQueue(String messageContent) throws JmsException {
        jmsTemplate.send(queueName, session -> session.createTextMessage(messageContent));
    }
}
