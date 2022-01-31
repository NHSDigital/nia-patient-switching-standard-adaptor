package uk.nhs.adaptors.pss.translator.task;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.config.GeneralProperties;
import uk.nhs.adaptors.pss.translator.mhs.MhsRequestBuilder;
import uk.nhs.adaptors.pss.translator.model.OutboundMessage;
import uk.nhs.adaptors.pss.translator.service.EhrExtractRequestService;
import uk.nhs.adaptors.pss.translator.service.MhsClientService;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SendEhrExtractRequestHandler {

    private final EhrExtractRequestService ehrExtractRequestService;
    private final MhsRequestBuilder requestBuilder;
    private final MhsClientService mhsClientService;
    private final GeneralProperties generalProperties;
    private final MigrationStatusLogService migrationStatusLogService;

    @SneakyThrows
    public boolean prepareAndSendRequest(String nhsNumber) {
        String conversationId = UUID.randomUUID().toString();
        String fromOdsCode = generalProperties.getFromOdsCode();

        String ehrExtractRequest = ehrExtractRequestService.buildEhrExtractRequest(
            nhsNumber,
            fromOdsCode
        );

        var outboundMessage = OutboundMessage.builder()
            .payload(ehrExtractRequest)
            .build();

        var request = requestBuilder.buildSendEhrExtractRequest(conversationId, fromOdsCode, outboundMessage);

        try {
            mhsClientService.send(request);
        } catch (WebClientResponseException wcre) {
            LOGGER.error("Received an ERROR response from MHS: [{}]", wcre.getMessage());
            migrationStatusLogService.addMigrationStatusLog(MigrationStatus.EHR_EXTRACT_REQUEST_ERROR, nhsNumber);
            return false;
        }

        LOGGER.info("Got response from MHS - 202 Accepted");
        migrationStatusLogService.addMigrationStatusLog(MigrationStatus.EHR_EXTRACT_REQUEST_ACCEPTED, nhsNumber);
        return true;
    }
}
