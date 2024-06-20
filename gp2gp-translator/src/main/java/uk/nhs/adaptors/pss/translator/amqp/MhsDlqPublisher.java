package uk.nhs.adaptors.pss.translator.amqp;

import jakarta.jms.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MhsDlqPublisher {

    @Qualifier("jmsTemplateMhsDLQ")
    private final JmsTemplate jmsTemplate;

    public void sendToMhsDlq(Message message) {

        jmsTemplate.execute((session, producer) -> {
            producer.send(message);
            return true;
        });
    }
}
