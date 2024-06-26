package uk.nhs.adaptors.pss.translator.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.pss.translator.exception.MhsServerErrorException;
import uk.nhs.adaptors.pss.translator.mhs.MhsRequestBuilder;
import uk.nhs.adaptors.pss.translator.mhs.model.OutboundMessage;
import uk.nhs.adaptors.pss.translator.model.ACKMessageData;
import uk.nhs.adaptors.pss.translator.service.ApplicationAcknowledgementMessageService;
import uk.nhs.adaptors.pss.translator.service.IdGeneratorService;
import uk.nhs.adaptors.pss.translator.service.MhsClientService;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SendACKMessageHandler {

    private final MhsRequestBuilder requestBuilder;
    private final MhsClientService mhsClientService;
    private final ApplicationAcknowledgementMessageService messageService;
    private final IdGeneratorService idGeneratorService;

    @SneakyThrows
    public boolean prepareAndSendMessage(ACKMessageData messageData) {
        String messageId = idGeneratorService.generateUuid().toUpperCase();

        String ackMessage = messageService.buildAckMessage(messageData, messageId);
        OutboundMessage outboundMessage = new OutboundMessage(ackMessage);

        var request = requestBuilder.buildSendACKRequest(
            messageData.getConversationId(),
            messageData.getToOdsCode(),
            outboundMessage,
            messageId);

        try {
            mhsClientService.send(request);
        } catch (WebClientResponseException e) {
            LOGGER.error("Received an ERROR response from MHS: [{}]", e.getMessage());

            if (e.getStatusCode().is5xxServerError()) {
                throw new MhsServerErrorException("Unable to send ACK message");
            }

            return false;
        }
        return true;
    }
}