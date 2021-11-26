package uk.nhs.adaptors.pss.translator.amqp;

import javax.jms.IllegalStateException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PssQueueConsumer {
    @JmsListener(destination = "${amqp.pss.queueName}", containerFactory = "pssQueueJmsListenerFactory", concurrency = "1")
    @SneakyThrows
    public void receive(Message message) {
        LOGGER.info(((TextMessage) message).getText());
        throw new IllegalStateException("test exception");
    }
}
