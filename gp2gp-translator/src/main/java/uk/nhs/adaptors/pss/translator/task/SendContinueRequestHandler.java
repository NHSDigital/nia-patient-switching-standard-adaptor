package uk.nhs.adaptors.pss.translator.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.common.enums.MigrationStatus;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.exception.MhsServerErrorException;
import uk.nhs.adaptors.pss.translator.mhs.MhsRequestBuilder;
import uk.nhs.adaptors.pss.translator.mhs.model.OutboundMessage;
import uk.nhs.adaptors.pss.translator.model.ContinueRequestData;
import uk.nhs.adaptors.pss.translator.service.ContinueRequestService;
import uk.nhs.adaptors.pss.translator.service.IdGeneratorService;
import uk.nhs.adaptors.pss.translator.service.MhsClientService;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SendContinueRequestHandler {
    private final MhsRequestBuilder requestBuilder;
    private final MhsClientService mhsClientService;
    private final MigrationStatusLogService migrationStatusLogService;
    private final ContinueRequestService continueRequestService;
    private final IdGeneratorService idGeneratorService;

    @SneakyThrows
    public void prepareAndSendRequest(ContinueRequestData data) {

        String messageId = idGeneratorService.generateUuid().toUpperCase();

        String continueRequest = continueRequestService.buildContinueRequest(data, messageId);
        var outboundMessage = new OutboundMessage(continueRequest);
        var request = requestBuilder.buildSendContinueRequest(
            data.getConversationId(), data.getToOdsCode(), outboundMessage, messageId);

        try {
            mhsClientService.send(request);
        } catch (WebClientResponseException webClientResponseException) {
            LOGGER.error("Received an ERROR response from MHS: [{}]", webClientResponseException.getMessage());
            migrationStatusLogService.addMigrationStatusLog(MigrationStatus.CONTINUE_REQUEST_ERROR, data.getConversationId(), null);

            if (webClientResponseException.getStatusCode().is5xxServerError()) {
                throw new MhsServerErrorException("Unable to sent continue message");
            }

            throw webClientResponseException;
        }

        LOGGER.info("Got response from MHS - 202 Accepted");
        migrationStatusLogService.addMigrationStatusLog(MigrationStatus.CONTINUE_REQUEST_ACCEPTED, data.getConversationId(), null);
    }
}
