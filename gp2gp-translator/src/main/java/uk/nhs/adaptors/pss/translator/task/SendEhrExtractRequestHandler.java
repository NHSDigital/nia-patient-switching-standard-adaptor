package uk.nhs.adaptors.pss.translator.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.common.enums.MigrationStatus;
import uk.nhs.adaptors.common.model.TransferRequestMessage;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.MhsRequestBuilder;
import uk.nhs.adaptors.pss.translator.mhs.model.OutboundMessage;
import uk.nhs.adaptors.pss.translator.service.EhrExtractRequestService;
import uk.nhs.adaptors.pss.translator.service.IdGeneratorService;
import uk.nhs.adaptors.pss.translator.service.MhsClientService;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SendEhrExtractRequestHandler {

    private final EhrExtractRequestService ehrExtractRequestService;
    private final MhsRequestBuilder requestBuilder;
    private final MhsClientService mhsClientService;
    private final MigrationStatusLogService migrationStatusLogService;
    private final IdGeneratorService idGeneratorService;

    @SneakyThrows
    public boolean prepareAndSendRequest(TransferRequestMessage message) {
        String conversationId = message.getConversationId();
        String toOdsCode = message.getToOds();
        String messageId = idGeneratorService.generateUuid().toUpperCase();

        String ehrExtractRequest = ehrExtractRequestService.buildEhrExtractRequest(message, messageId);

        var outboundMessage = new OutboundMessage(ehrExtractRequest);
        var request = requestBuilder.buildSendEhrExtractRequest(conversationId, toOdsCode, outboundMessage, messageId);

        try {
            var response = mhsClientService.send(request);
            LOGGER.debug(response);
        } catch (WebClientResponseException wcre) {
            LOGGER.error("Received an ERROR response from MHS: [{}]", wcre.getMessage());
            migrationStatusLogService.addMigrationStatusLog(MigrationStatus.EHR_EXTRACT_REQUEST_ERROR, conversationId, null, "2");
            return false;
        }

        LOGGER.info("Got response from MHS - 202 Accepted");
        migrationStatusLogService.addMigrationStatusLog(MigrationStatus.EHR_EXTRACT_REQUEST_ACCEPTED, conversationId, null, null);
        return true;
    }
}
