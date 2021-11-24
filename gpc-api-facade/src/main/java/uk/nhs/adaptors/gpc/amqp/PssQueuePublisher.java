package uk.nhs.adaptors.gpc.amqp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.gpc.amqp.task.TaskDefinition;
import uk.nhs.adaptors.gpc.amqp.task.TaskHandlerException;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PssQueuePublisher {

    @Qualifier("jmsTemplatePssQueue")
    private final JmsTemplate pssJmsTemplate;
    private final ObjectMapper objectMapper;

    public void sendToPssQueue(TaskDefinition taskDefinition) {
        try {
            String messagePayload = objectMapper.writeValueAsString(taskDefinition);
            pssJmsTemplate.send(session -> session.createTextMessage(messagePayload));
        } catch (JsonProcessingException e) {
            throw new TaskHandlerException("Unable to serialise task definition to JSON", e);
        }
    }
}
