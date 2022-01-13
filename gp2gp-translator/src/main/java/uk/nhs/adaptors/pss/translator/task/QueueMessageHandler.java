package uk.nhs.adaptors.pss.translator.task;
import java.util.UUID;

import javax.jms.Message;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.pss.translator.service.EhrExtractRequestService;
import uk.nhs.adaptors.pss.translator.utils.FhirParser;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class QueueMessageHandler {

    private final FhirParser fhirParser;
    private final EhrExtractRequestService ehrExtractRequestService;

    @SneakyThrows
    public boolean handle(Message message) {
        LOGGER.info("Handling message with message_id=[{}]", message.getJMSMessageID());

        var parsed = fhirParser.parseResource(message.getBody(String.class), Parameters.class);
        String conversationId = UUID.randomUUID().toString();
        String nhsNumber = ((Identifier) parsed.getParameterFirstRep().getValue()).getValue();
        String fromODSCode = "ODS_CODE_HERE"; //TODO: Figure later

        String ehrExtractRequest = ehrExtractRequestService.buildEhrExtractRequest(
            conversationId,
            nhsNumber,
            fromODSCode
        );

        return true;
    }

}
