package uk.nhs.adaptors.amqp.utils;

import javax.jms.Message;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;

import lombok.SneakyThrows;

public class SubscribeQueue {
    @JmsListener(destination = "${amqp.taskQueueName}")
    @SneakyThrows
    public void receive(Message message) {
        System.out.println("Received message: " + ((TextMessage) message).getText());
    }
}
