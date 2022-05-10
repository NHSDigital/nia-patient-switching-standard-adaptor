package uk.nhs.adaptors.pss.translator.task.scheduled;

import static uk.nhs.adaptors.connector.model.MigrationStatus.CONTINUE_REQUEST_ACCEPTED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_GENERAL_PROCESSING_ERROR;
import static uk.nhs.adaptors.pss.translator.model.NACKReason.LARGE_MESSAGE_TIMEOUT;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.hl7.v3.RCMRIN030000UK06Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.common.service.MDCService;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.connector.service.PatientMigrationRequestService;
import uk.nhs.adaptors.pss.translator.config.TimeoutProperties;
import uk.nhs.adaptors.pss.translator.exception.SdsRetrievalException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.NACKMessageData;
import uk.nhs.adaptors.pss.translator.service.PersistDurationService;
import uk.nhs.adaptors.pss.translator.task.SendNACKMessageHandler;
import uk.nhs.adaptors.pss.translator.util.InboundMessageUtil;
import uk.nhs.adaptors.pss.translator.util.OutboundMessageUtil;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class EHRTimeoutHandler {

    private static final String EHR_EXTRACT_MESSAGE_NAME = "RCMR_IN030000UK06";
    private static final String COPC_MESSAGE_NAME = "COPC_IN000001UK01";
    private final PersistDurationService persistDurationService;
    private final PatientMigrationRequestService migrationRequestService;
    private final MDCService mdcService;
    private final TimeoutProperties timeoutProperties;
    private final SendNACKMessageHandler sendNACKMessageHandler;
    private final OutboundMessageUtil outboundMessageUtil;
    private final InboundMessageUtil inboundMessageUtil;
    private final MigrationStatusLogService migrationStatusLogService;
    private final PatientAttachmentLogService patientAttachmentLogService;

    @Scheduled(cron = "${timeout.cronTime}")
    public void checkForTimeouts() {
        LOGGER.info("running scheduled task to check for timeouts");

        List<PatientMigrationRequest> extractReceivedRequests =
            migrationRequestService.getMigrationRequestByCurrentMigrationStatus(EHR_EXTRACT_TRANSLATED);
        List<PatientMigrationRequest> largeMessageRequests =
            migrationRequestService.getMigrationRequestByCurrentMigrationStatus(CONTINUE_REQUEST_ACCEPTED);

        extractReceivedRequests.forEach(this::handleMigrationTimeout);

        largeMessageRequests.forEach(this::handleMigrationTimeout);
    }

    public void handleMigrationTimeout(PatientMigrationRequest migrationRequest) {

        String conversationId = migrationRequest.getConversationId();
        mdcService.applyConversationId(conversationId);

        try {
            long timeout;
            Duration ehrPersistDuration = persistDurationService.getPersistDurationFor(migrationRequest, EHR_EXTRACT_MESSAGE_NAME);
            InboundMessage message = inboundMessageUtil.readMessage(migrationRequest.getInboundMessage());
            ZonedDateTime messageTimestamp = inboundMessageUtil.parseMessageTimestamp(message.getEbXML());
            ZonedDateTime currentTime = ZonedDateTime.now(messageTimestamp.getZone());
            long numberCOPCMessages = patientAttachmentLogService.countAttachmentsForMigrationRequest(migrationRequest.getId());

            if (numberCOPCMessages > 0) {
                Duration copcPersistDuration = persistDurationService.getPersistDurationFor(migrationRequest, COPC_MESSAGE_NAME);

                timeout = (timeoutProperties.getEhrExtractWeighting() * ehrPersistDuration.getSeconds())
                    + (timeoutProperties.getCopcWeighting() * numberCOPCMessages * copcPersistDuration.getSeconds());

                LOGGER.debug("Large message timeout calculated as [{}] seconds", timeout);
            } else {
                timeout = timeoutProperties.getEhrExtractWeighting() * ehrPersistDuration.getSeconds();

                LOGGER.debug("Non large message timeout calculated as [{}] seconds", timeout);
            }

            ZonedDateTime timeoutDateTime = messageTimestamp.plusSeconds(timeout);
            LOGGER.debug("Timeout datetime calculated as [{}]", timeoutDateTime);

            if (timeoutDateTime.isBefore(currentTime)) {
                LOGGER.info("Migration timed out at [{}]", timeoutDateTime);
                sendNackMessage(message, conversationId);
            }
        } catch (SdsRetrievalException e) {
            LOGGER.error("Error retrieving persist duration: [{}]", e.getMessage());
        } catch (JsonProcessingException | SAXException | DateTimeParseException | JAXBException e) {
            LOGGER.error("Error parsing inbound message from database");
            migrationStatusLogService.addMigrationStatusLog(EHR_GENERAL_PROCESSING_ERROR, conversationId);
        }

        mdcService.applyConversationId("");
    }

    private void sendNackMessage(InboundMessage inboundMessage, String conversationId) throws JAXBException {

        RCMRIN030000UK06Message payload = unmarshallString(inboundMessage.getPayload(), RCMRIN030000UK06Message.class);

        NACKMessageData messageData = NACKMessageData
            .builder()
            .nackCode(LARGE_MESSAGE_TIMEOUT.getCode())
            .fromAsid(outboundMessageUtil.parseFromAsid(payload))
            .toAsid(outboundMessageUtil.parseToAsid(payload))
            .toOdsCode(outboundMessageUtil.parseToOdsCode(payload))
            .messageRef(outboundMessageUtil.parseMessageRef(payload))
            .conversationId(conversationId)
            .build();

        LOGGER.debug("EHR Extract message timed out: sending NACK message");
        migrationStatusLogService.addMigrationStatusLog(LARGE_MESSAGE_TIMEOUT.getMigrationStatus(), conversationId);

        sendNACKMessageHandler.prepareAndSendMessage(messageData);
    }
}
