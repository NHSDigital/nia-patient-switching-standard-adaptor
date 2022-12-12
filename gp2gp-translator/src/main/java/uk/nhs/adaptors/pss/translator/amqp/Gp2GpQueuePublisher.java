package uk.nhs.adaptors.pss.translator.amqp;

import javax.jms.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
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
