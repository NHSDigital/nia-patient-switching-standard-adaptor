package uk.nhs.adaptors.pss.translator.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.pss.translator.mhs.MhsRequestBuilder;
import uk.nhs.adaptors.pss.translator.mhs.model.OutboundMessage;
import uk.nhs.adaptors.pss.translator.model.ApplicationAcknowledgmentData;
import uk.nhs.adaptors.pss.translator.service.ApplicationAcknowledgementMessageService;
import uk.nhs.adaptors.pss.translator.service.MhsClientService;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SendNACKMessageHandler {

    private final MhsRequestBuilder requestBuilder;
    private final MhsClientService mhsClientService;
    private final ApplicationAcknowledgementMessageService messageService;

    @SneakyThrows
    public boolean prepareAndSendMessage(ApplicationAcknowledgmentData messageData) {
        String ackMessage = messageService.buildNackMessage(messageData);
        OutboundMessage outboundMessage = new OutboundMessage(ackMessage);

        try {

            var request = requestBuilder.buildSendACKRequest(
                messageData.getConversationId(),
                messageData.getToOdsCode(),
                outboundMessage);

            String response = mhsClientService.send(request);

            LOGGER.debug(response);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Error building NACK message: [{}]", e.getMessage());
            return false;
        } catch (WebClientResponseException e) {
            LOGGER.error("Received an ERROR response from MHS: [{}]", e.getMessage());
            return false;
        }

        LOGGER.info("Got response from MHS - 202 Accepted");
        return true;
    }
}
