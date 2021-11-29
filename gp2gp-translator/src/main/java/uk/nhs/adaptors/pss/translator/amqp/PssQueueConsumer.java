package uk.nhs.adaptors.pss.translator.amqp;

import javax.jms.Message;
import javax.jms.TextMessage;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PssQueueConsumer {
    @JmsListener(destination = "${amqp.pss.queueName}", containerFactory = "pssQueueJmsListenerFactory")
    @SneakyThrows
    public void receive(Message message) {
        LOGGER.info(((TextMessage) message).getText());
    }
}
