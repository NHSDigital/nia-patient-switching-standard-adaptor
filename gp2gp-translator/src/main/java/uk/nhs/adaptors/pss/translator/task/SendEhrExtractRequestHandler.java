package uk.nhs.adaptors.pss.translator.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.common.model.TransferRequestMessage;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.MhsRequestBuilder;
import uk.nhs.adaptors.pss.translator.mhs.model.OutboundMessage;
import uk.nhs.adaptors.pss.translator.service.EhrExtractRequestService;
import uk.nhs.adaptors.pss.translator.service.MhsClientService;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SendEhrExtractRequestHandler {

    private final EhrExtractRequestService ehrExtractRequestService;
    private final MhsRequestBuilder requestBuilder;
    private final MhsClientService mhsClientService;
    private final MigrationStatusLogService migrationStatusLogService;

    @SneakyThrows
    public boolean prepareAndSendRequest(TransferRequestMessage message) {
        String conversationId = message.getConversationId();
        String toOdsCode = message.getToOds();

        String ehrExtractRequest = ehrExtractRequestService.buildEhrExtractRequest(message);//a

        var outboundMessage = new OutboundMessage(ehrExtractRequest);
        var request = requestBuilder.buildSendEhrExtractRequest(conversationId, toOdsCode, outboundMessage);

        try {
            var response = mhsClientService.send(request);
            LOGGER.debug(response);
        } catch (WebClientResponseException wcre) {
            LOGGER.error("Received an ERROR response from MHS: [{}]", wcre.getMessage());
            migrationStatusLogService.addMigrationStatusLog(MigrationStatus.EHR_EXTRACT_REQUEST_ERROR, conversationId);
            return false;
        }

        LOGGER.info("Got response from MHS - 202 Accepted");
        migrationStatusLogService.addMigrationStatusLog(MigrationStatus.EHR_EXTRACT_REQUEST_ACCEPTED, conversationId);
        return true;
    }
}
