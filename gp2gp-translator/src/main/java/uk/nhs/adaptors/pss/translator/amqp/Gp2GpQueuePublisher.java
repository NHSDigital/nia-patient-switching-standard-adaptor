package uk.nhs.adaptors.pss.translator.amqp;

import jakarta.jms.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@ConditionalOnProperty(value = "amqp.daisyChaining", havingValue = "true")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Gp2GpQueuePublisher {
    @Qualifier("jmsTemplateGp2GpAdaptorQueue")
    private final JmsTemplate jmsTemplate;

    public void sendToGp2GpAdaptor(Message message) {

        jmsTemplate.execute((session, producer) -> {
            producer.send(message);
            return true;
        });
    }
}
