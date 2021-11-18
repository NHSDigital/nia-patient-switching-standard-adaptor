package uk.nhs.adaptors.amqp.utils;

import javax.jms.Message;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

import uk.nhs.adaptors.amqp.utils.task.TestTask;

public class SubscribeQueue {
    @Autowired
    private ObjectMapper objectMapper;
    
    @JmsListener(destination = "${amqp.gpcFacadeQueue}")
    @JmsListener(destination = "${amqp.mhsAdaptorQueue}")
    @SneakyThrows
    private void subscribeToQueue(Message message) {
        String payload = ((TextMessage) message).getText();
        TestTask testTask = objectMapper.readValue(payload, TestTask.class);
        System.out.println("Received message = TaskID:" + testTask.getTaskId());
    }
}
