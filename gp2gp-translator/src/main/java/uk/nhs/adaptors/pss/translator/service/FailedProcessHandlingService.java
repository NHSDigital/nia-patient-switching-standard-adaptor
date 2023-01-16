package uk.nhs.adaptors.pss.translator.service;

import static uk.nhs.adaptors.connector.model.MigrationStatus.CONTINUE_REQUEST_ERROR;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_ERROR;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_GENERAL_PROCESSING_ERROR;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_LRG_MSG_ATTACHMENTS_NOT_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_LRG_MSG_GENERAL_FAILURE;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_LRG_MSG_REASSEMBLY_FAILURE;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_LRG_MSG_TIMEOUT;
import static uk.nhs.adaptors.pss.translator.model.NACKReason.LARGE_MESSAGE_GENERAL_FAILURE;
import static uk.nhs.adaptors.pss.translator.model.NACKReason.LARGE_MESSAGE_TIMEOUT;
import static uk.nhs.adaptors.pss.translator.model.NACKReason.UNEXPECTED_CONDITION;

import java.util.List;

import org.hl7.v3.COPCIN000001UK01Message;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.model.NACKReason;
import uk.nhs.adaptors.pss.translator.task.SendNACKMessageHandler;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class FailedProcessHandlingService {

    private final MigrationStatusLogService migrationStatusLogService;
    private final NackAckPreparationService nackAckPreparationService;

    private final SendNACKMessageHandler sendNACKMessageHandler;

    private static final List<MigrationStatus> FAILED_MIGRATION_STATUSES = List.of(
        EHR_EXTRACT_REQUEST_ERROR,
        EHR_EXTRACT_REQUEST_NEGATIVE_ACK,
        CONTINUE_REQUEST_ERROR,
        ERROR_LRG_MSG_REASSEMBLY_FAILURE,
        ERROR_LRG_MSG_ATTACHMENTS_NOT_RECEIVED,
        ERROR_LRG_MSG_GENERAL_FAILURE,
        ERROR_LRG_MSG_TIMEOUT,
        EHR_GENERAL_PROCESSING_ERROR
    );


    public boolean hasProcessFailed(String conversationId) {
        MigrationStatus migrationStatus = getMigrationStatus(conversationId);

        return FAILED_MIGRATION_STATUSES.contains(migrationStatus);
    }

    public void handleFailedProcess(RCMRIN030000UK06Message ehrExtractMessage, String conversationId) {
        LOGGER.info("Received EHR Extract [Message ID: {}] but the transfer process has already failed. "
            + "Responding with NACK for unexpected condition.", ehrExtractMessage.getId());

        var nackMessageData = nackAckPreparationService
            .prepareNackMessageData(UNEXPECTED_CONDITION, ehrExtractMessage, conversationId);

        sendNACKMessageHandler.prepareAndSendMessage(nackMessageData);

    }

    public void handleFailedProcess(COPCIN000001UK01Message copcMessage, String conversationId) {

        var migrationStatus = getMigrationStatus(conversationId);

        NACKReason nackReason = switch (migrationStatus) {
            case EHR_EXTRACT_REQUEST_NEGATIVE_ACK -> UNEXPECTED_CONDITION;
            case ERROR_LRG_MSG_TIMEOUT -> LARGE_MESSAGE_TIMEOUT;
            default -> LARGE_MESSAGE_GENERAL_FAILURE;
        };

        LOGGER.info("Received COPC Message [Message ID: {}], but the transfer process has already failed. Responding with NACK code {}",
            copcMessage.getId(), nackReason.getCode());

        var nackMessageData = nackAckPreparationService
            .prepareNackMessageData(nackReason, copcMessage, conversationId);

        sendNACKMessageHandler.prepareAndSendMessage(nackMessageData);
    }

    private MigrationStatus getMigrationStatus(String conversationId) {
        return migrationStatusLogService.getLatestMigrationStatusLog(conversationId)
            .getMigrationStatus();
    }
}


