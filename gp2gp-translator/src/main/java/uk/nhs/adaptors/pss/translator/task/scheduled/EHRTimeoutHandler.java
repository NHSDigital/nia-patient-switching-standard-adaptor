package uk.nhs.adaptors.pss.translator.task.scheduled;

import static uk.nhs.adaptors.connector.model.MigrationStatus.CONTINUE_REQUEST_ACCEPTED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.COPC_ACKNOWLEDGED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.COPC_FAILED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.COPC_MESSAGE_PROCESSING;
import static uk.nhs.adaptors.connector.model.MigrationStatus.COPC_MESSAGE_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_PROCESSING;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_ACCEPTED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_ACKNOWLEDGED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_GENERAL_PROCESSING_ERROR;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_REQUEST_TIMEOUT;
import static uk.nhs.adaptors.connector.model.MigrationStatus.REQUEST_RECEIVED;
import static uk.nhs.adaptors.pss.translator.model.NACKReason.LARGE_MESSAGE_ATTACHMENTS_NOT_RECEIVED;
import static uk.nhs.adaptors.pss.translator.model.NACKReason.LARGE_MESSAGE_TIMEOUT;
import static uk.nhs.adaptors.pss.translator.model.NACKReason.UNEXPECTED_CONDITION;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

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
import uk.nhs.adaptors.common.util.DateUtils;
import uk.nhs.adaptors.connector.model.MigrationStatusLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.connector.service.PatientMigrationRequestService;
import uk.nhs.adaptors.pss.translator.config.TimeoutProperties;
import uk.nhs.adaptors.pss.translator.exception.SdsRetrievalException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.NACKMessageData;
import uk.nhs.adaptors.pss.translator.model.NACKReason;
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
    private final DateUtils dateUtils;

    @Scheduled(cron = "${timeout.cronTime}")
    public void checkForTimeouts() {
        LOGGER.info("running scheduled task to check for timeouts");

        //TODO: improve the SQL query to reduce calls

        List<PatientMigrationRequest> preEhrParsedRequests = Stream.of(
                migrationRequestService.getMigrationRequestByCurrentMigrationStatus(REQUEST_RECEIVED),
                migrationRequestService.getMigrationRequestByCurrentMigrationStatus(EHR_EXTRACT_REQUEST_ACCEPTED),
                migrationRequestService.getMigrationRequestByCurrentMigrationStatus(EHR_EXTRACT_REQUEST_ACKNOWLEDGED),
                migrationRequestService.getMigrationRequestByCurrentMigrationStatus(EHR_EXTRACT_RECEIVED))
            .flatMap(Collection::stream)
            .toList();

        preEhrParsedRequests.forEach(this::handleRequestTimeout);

        // Ehr Extract Translated is not the final state for an EHR, but we cannot guarantee it has attachments
        List<PatientMigrationRequest> translatedRequests =
            migrationRequestService.getMigrationRequestByCurrentMigrationStatus(EHR_EXTRACT_TRANSLATED);

        translatedRequests.forEach(migrationRequest -> handleMigrationTimeout(migrationRequest, UNEXPECTED_CONDITION));

        List<PatientMigrationRequest> requestsWithAttachments = Stream.of(
                migrationRequestService.getMigrationRequestByCurrentMigrationStatus(EHR_EXTRACT_PROCESSING),
                migrationRequestService.getMigrationRequestByCurrentMigrationStatus(CONTINUE_REQUEST_ACCEPTED),
                migrationRequestService.getMigrationRequestByCurrentMigrationStatus(COPC_MESSAGE_RECEIVED),
                migrationRequestService.getMigrationRequestByCurrentMigrationStatus(COPC_MESSAGE_PROCESSING),
                migrationRequestService.getMigrationRequestByCurrentMigrationStatus(COPC_ACKNOWLEDGED),
                migrationRequestService.getMigrationRequestByCurrentMigrationStatus(COPC_FAILED))
            .flatMap(Collection::stream)
            .toList();

        requestsWithAttachments.forEach(migrationRequest -> handleMigrationTimeout(migrationRequest,
            LARGE_MESSAGE_ATTACHMENTS_NOT_RECEIVED));
    }

    private void handleRequestTimeout(PatientMigrationRequest migrationRequest) {
        String conversationId = migrationRequest.getConversationId();
        mdcService.applyConversationId(conversationId);

        try {
            long timeout;
            Duration ehrPersistDuration = persistDurationService.getPersistDurationFor(migrationRequest, EHR_EXTRACT_MESSAGE_NAME);
            OffsetDateTime currentTime = dateUtils.getCurrentOffsetDateTime();

            timeout = timeoutProperties.getEhrExtractWeighting() * ehrPersistDuration.getSeconds();

            LOGGER.debug("Request timeout calculated as [{}] seconds", timeout);

            MigrationStatusLog migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);
            var timeoutDateTime = migrationStatusLog.getDate().plusSeconds(timeout);
            LOGGER.debug("Timeout datetime calculated as [{}]", timeoutDateTime);

            if (timeoutDateTime.isBefore(currentTime)) {
                LOGGER.info("Migration Request timed out at [{}]", timeoutDateTime);
                migrationStatusLogService.addMigrationStatusLog(ERROR_REQUEST_TIMEOUT, conversationId, null);
            }

        } catch (SdsRetrievalException e) {
            LOGGER.error("Error retrieving persist duration: [{}]", e.getMessage());
        } finally {
            mdcService.applyConversationId("");
        }
    }

    private void handleMigrationTimeout(PatientMigrationRequest migrationRequest, NACKReason reason) {

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
                sendNackMessage(message, conversationId, reason);
            }
        } catch (SdsRetrievalException e) {
            LOGGER.error("Error retrieving persist duration: [{}]", e.getMessage());
        } catch (JsonProcessingException | SAXException | DateTimeParseException | JAXBException e) {
            LOGGER.error("Error parsing inbound message from database");
            migrationStatusLogService.addMigrationStatusLog(EHR_GENERAL_PROCESSING_ERROR, conversationId, null);
        } finally {
            mdcService.applyConversationId("");
        }
    }

    private void sendNackMessage(InboundMessage inboundMessage, String conversationId, NACKReason reason) throws JAXBException {

        RCMRIN030000UK06Message payload = unmarshallString(inboundMessage.getPayload(), RCMRIN030000UK06Message.class);

        NACKMessageData messageData = NACKMessageData
            .builder()
            .nackCode(reason.getCode())
            .fromAsid(outboundMessageUtil.parseFromAsid(payload))
            .toAsid(outboundMessageUtil.parseToAsid(payload))
            .toOdsCode(outboundMessageUtil.parseToOdsCode(payload))
            .messageRef(outboundMessageUtil.parseMessageRef(payload))
            .conversationId(conversationId)
            .build();

        LOGGER.debug("EHR Extract message timed out: sending NACK message");
        if (sendNACKMessageHandler.prepareAndSendMessage(messageData)) {

            if (reason == LARGE_MESSAGE_ATTACHMENTS_NOT_RECEIVED) {
                migrationStatusLogService
                    .addMigrationStatusLog(LARGE_MESSAGE_TIMEOUT.getMigrationStatus(), conversationId, null);
            } else {
                migrationStatusLogService.addMigrationStatusLog(reason.getMigrationStatus(), conversationId, null);
            }
        }
    }
}
