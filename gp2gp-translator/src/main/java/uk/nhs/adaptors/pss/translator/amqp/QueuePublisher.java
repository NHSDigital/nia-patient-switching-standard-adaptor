package uk.nhs.adaptors.pss.translator.amqp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.amqp.task.TaskDefinition;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class QueuePublisher {

    @Qualifier("jmsTemplatePssQueue")
    private final JmsTemplate pssJmsTemplate;
    @Qualifier("jmsTemplateMhsQueue")
    private final JmsTemplate mhsJmsTemplate;

    public void sendToPssQueue(TaskDefinition taskDefinition) {
        pssJmsTemplate.send(session -> session.createTextMessage("test text pss"));
    }
    //TODO to be removed - this is just an example - we are not going to publish anything to MhsQueue - only consume
    public void sendToMhsQueue(TaskDefinition taskDefinition) {
        mhsJmsTemplate.send(session -> session.createTextMessage("test text mhs"));
    }
}
