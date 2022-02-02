package uk.nhs.adaptors.pss.translator.task;

import javax.jms.JMSException;
import javax.jms.Message;

import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.common.util.fhir.ParametersUtils;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class QueueMessageHandler {
    private final SendEhrExtractRequestHandler sendEhrExtractRequestHandler;
    private final FhirParser fhirParser;

    @SneakyThrows
    public boolean handle(Message message) {
        var messageId = message.getJMSMessageID();
        LOGGER.info("Handling message with message_id=[{}]", messageId);
        try {
            var parsedParameters = fhirParser.parseResource(message.getBody(String.class), Parameters.class);
            String nhsNumber = ParametersUtils.getNhsNumberFromParameters(parsedParameters).get().getValue();
            return sendEhrExtractRequestHandler.prepareAndSendRequest(nhsNumber);
        } catch (JMSException e) {
            LOGGER.error("Error while processing PSSQueue message_id=[{}]", messageId, e);
            return false;
        }
    }
}
