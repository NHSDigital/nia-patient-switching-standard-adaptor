package uk.nhs.adaptors.pss.gpc.amqp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import uk.nhs.adaptors.common.model.PssQueueMessage;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PssQueuePublisher {

    @Qualifier("jmsTemplatePssQueue")
    private final JmsTemplate pssJmsTemplate;

    private final ObjectMapper objectMapper;

    public void sendToPssQueue(PssQueueMessage message) {
        pssJmsTemplate.send(session -> session.createTextMessage(getMessageAsString(message)));
    }

    @SneakyThrows
    private String getMessageAsString(PssQueueMessage message) {
        return objectMapper.writeValueAsString(message);
    }
}
