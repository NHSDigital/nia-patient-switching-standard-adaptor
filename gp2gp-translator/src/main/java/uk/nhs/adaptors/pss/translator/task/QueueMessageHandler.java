package uk.nhs.adaptors.pss.translator.task;

import javax.jms.Message;

import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class QueueMessageHandler {

    @SneakyThrows
    public boolean handle(Message message) {
        LOGGER.info("Handling message with message_id=[{}]", message.getJMSMessageID());

        //TODO: Add tasks here in the future?

        //TODO: Invoke a mapper from FHIR to HL7 [Map a transfer request to EhrExtractRequest]
        //       Make sure everything is mapped correctly
        //       Send stuff to the MHS

        return true;
    }

}
