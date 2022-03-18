package uk.nhs.adaptors.pss.translator.task;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.MhsRequestBuilder;
import uk.nhs.adaptors.pss.translator.mhs.model.OutboundMessage;
import uk.nhs.adaptors.pss.translator.model.ACKMessageData;
import uk.nhs.adaptors.pss.translator.service.ACKMessageService;
import uk.nhs.adaptors.pss.translator.service.MhsClientService;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SendACKRequestHandler {

    private final MhsRequestBuilder requestBuilder;
    private final MhsClientService mhsClientService;
    private final MigrationStatusLogService migrationStatusLogService;
    private final ACKMessageService messageService;

    @SneakyThrows
    public boolean prepareAndSendMessage(ACKMessageData messageData) {
        String ackMessage = messageService.buildMessage(messageData);
        OutboundMessage outboundMessage = new OutboundMessage(ackMessage);
        var request = requestBuilder.buildSendACKRequest(
                messageData.getConversationId(),
                messageData.getToOdsCode(),
                outboundMessage);

        try {
            String response = mhsClientService.send(request);
            LOGGER.debug(response);
        } catch (WebClientResponseException e) {
            LOGGER.error("Received an ERROR response from MHS: [{}]", e.getMessage());
            return false;
        }

        LOGGER.info("Got response from MHS - 202 Accepted");
        return true;
    }


}
