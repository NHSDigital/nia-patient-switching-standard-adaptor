package uk.nhs.adaptors.pss.translator.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.MhsRequestBuilder;
import uk.nhs.adaptors.pss.translator.mhs.model.OutboundMessage;
import uk.nhs.adaptors.pss.translator.model.ContinueRequestData;
import uk.nhs.adaptors.pss.translator.service.ContinueRequestService;
import uk.nhs.adaptors.pss.translator.service.MhsClientService;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SendContinueRequestHandler {

    private final MhsRequestBuilder requestBuilder;
    private final MhsClientService mhsClientService;
    private final MigrationStatusLogService migrationStatusLogService;
    private final ContinueRequestService continueRequestService;

    @SneakyThrows
    public boolean prepareAndSendRequest(ContinueRequestData data) {
        String continueRequest = continueRequestService.buildContinueRequest(data.getFromAsid(), data.getToAsid());

        var outboundMessage = new OutboundMessage(continueRequest);

        var request = requestBuilder.buildSendEhrExtractRequest(data.getConversationId(), data.getToOdsCode(), outboundMessage);

        try {
            mhsClientService.send(request);
        } catch (WebClientResponseException wcre) {
            LOGGER.error("Received an ERROR response from MHS: [{}]", wcre.getMessage());
            migrationStatusLogService.addMigrationStatusLog(MigrationStatus.CONTINUE_REQUEST_ACCEPTED, data.getNhsNumber());
            return false;
        }

        LOGGER.info("Got response from MHS - 202 Accepted");
        migrationStatusLogService.addMigrationStatusLog(MigrationStatus.CONTINUE_REQUEST_ERROR, data.getNhsNumber());
        return true;
    }
}
