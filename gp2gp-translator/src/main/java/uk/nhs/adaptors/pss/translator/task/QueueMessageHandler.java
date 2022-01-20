package uk.nhs.adaptors.pss.translator.task;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import javax.jms.Message;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.connector.dao.MigrationStatusLogDao;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.RequestStatus;
import uk.nhs.adaptors.pss.translator.config.PssConfiguration;
import uk.nhs.adaptors.pss.translator.mhs.MhsRequestBuilder;
import uk.nhs.adaptors.pss.translator.model.OutboundMessage;
import uk.nhs.adaptors.pss.translator.service.EhrExtractRequestService;
import uk.nhs.adaptors.pss.translator.service.MhsClientService;
import uk.nhs.adaptors.pss.translator.utils.FhirParser;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class QueueMessageHandler {

    private final FhirParser fhirParser;
    private final EhrExtractRequestService ehrExtractRequestService;
    private final MhsRequestBuilder requestBuilder;
    private final MhsClientService mhsClientService;
    private final PssConfiguration pssConfiguration;
    private final PatientMigrationRequestDao patientMigrationRequestDao;
    private final MigrationStatusLogDao migrationStatusLogDao;

    @SneakyThrows
    public boolean handle(Message message) {
        LOGGER.info("Handling message with message_id=[{}]", message.getJMSMessageID());

        var parsed = fhirParser.parseResource(message.getBody(String.class), Parameters.class);

        String conversationId = UUID.randomUUID().toString();
        String nhsNumber = ((Identifier) parsed.getParameterFirstRep().getValue()).getValue();
        String fromOdsCode = pssConfiguration.getFromOdsCode();

        String ehrExtractRequest = ehrExtractRequestService.buildEhrExtractRequest(
            conversationId,
            nhsNumber,
            fromOdsCode
        );

        var outboundMessage = OutboundMessage.builder()
            .payload(ehrExtractRequest)
            .build();

        var request = requestBuilder.buildSendEhrExtractRequest(conversationId, fromOdsCode, outboundMessage);
        int migrationRequestId = patientMigrationRequestDao.getMigrationRequestId(nhsNumber);

        try{
            mhsClientService.send(request);
        } catch (WebClientResponseException wcre) {
            LOGGER.error("Received an ERROR in response from MHS: [{}]", wcre.getMessage());
            handleResponse(migrationRequestId, RequestStatus.MHS_BAD_REQUEST);
            return false;
        }

        LOGGER.info("Got response from MHS - 202 Accepted");
        LOGGER.debug("RequestStatus of PatientMigrationRequest with id=[{}] : [{}]", migrationRequestId, RequestStatus.MHS_ACCEPTED.name());
        handleResponse(migrationRequestId, RequestStatus.MHS_ACCEPTED);

        return true;
    }

    private void handleResponse(int migrationRequestId, RequestStatus requestStatus) {
        migrationStatusLogDao.addMigrationStatusLog(
            requestStatus.name(),
            OffsetDateTime.now(ZoneOffset.UTC),
            migrationRequestId
        );
    }

}
