package uk.nhs.adaptors.pss.gpc.amqp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PssQueuePublisher {

    @Qualifier("jmsTemplatePssQueue")
    private final JmsTemplate pssJmsTemplate;

    public void sendToPssQueue(String message) {
        pssJmsTemplate.send(session -> session.createTextMessage(message));
    }
}
