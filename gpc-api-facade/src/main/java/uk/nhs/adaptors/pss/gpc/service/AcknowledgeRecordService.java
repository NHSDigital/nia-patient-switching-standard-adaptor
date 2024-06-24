package uk.nhs.adaptors.pss.gpc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.common.enums.ConfirmationResponse;
import uk.nhs.adaptors.common.model.AcknowledgeRecordMessage;
import uk.nhs.adaptors.connector.dao.MigrationStatusLogDao;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.common.enums.MigrationStatus;
import uk.nhs.adaptors.pss.gpc.amqp.PssQueuePublisher;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static org.apache.commons.lang3.EnumUtils.getEnumIgnoreCase;
import static uk.nhs.adaptors.common.enums.QueueMessageType.ACKNOWLEDGE_RECORD;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AcknowledgeRecordService {
    private final PatientMigrationRequestDao patientMigrationRequestDao;
    private final MigrationStatusLogDao migrationStatusLogDao;
    private final PssQueuePublisher pssQueuePublisher;

    public Boolean handleAcknowledgeRecord(
            @NotNull @NotEmpty String conversationId,
            @NotNull @NotEmpty String confirmationResponseString) {
        var patientMigrationRequest = patientMigrationRequestDao.getMigrationRequest(conversationId.toLowerCase());
        if (patientMigrationRequest == null) {
            return false;
        }

        var patientMigrationRequestId = patientMigrationRequest.getId();

        var patientMigrationStatusLog = migrationStatusLogDao.getLatestMigrationStatusLog(patientMigrationRequestId);
        if (patientMigrationStatusLog.getMigrationStatus() != MigrationStatus.MIGRATION_COMPLETED) {
            return false;
        }

        var confirmationResponse = getEnumIgnoreCase(ConfirmationResponse.class, confirmationResponseString);
        if (confirmationResponse == null) {
            return false;
        }

        var originalMessage = patientMigrationRequest.getInboundMessage();

        var pssMessage = createAcknowledgeRecordMessage(conversationId, confirmationResponse, originalMessage);

        pssQueuePublisher.sendToPssQueue(pssMessage);

        return true;
    }

    private AcknowledgeRecordMessage createAcknowledgeRecordMessage(
            String conversationId,
            ConfirmationResponse confirmationResponse,
            String originalMessage) {
        return AcknowledgeRecordMessage.builder()
            .conversationId(conversationId)
            .messageType(ACKNOWLEDGE_RECORD)
            .confirmationResponse(confirmationResponse)
            .originalMessage(originalMessage)
            .build();
    }
}
