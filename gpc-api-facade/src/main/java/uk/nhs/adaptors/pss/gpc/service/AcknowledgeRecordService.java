package uk.nhs.adaptors.pss.gpc.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.common.enums.ConfirmationResponse;
import uk.nhs.adaptors.common.enums.QueueMessageType;
import uk.nhs.adaptors.common.model.AcknowledgeRecordMessage;
import uk.nhs.adaptors.common.util.DateUtils;
import uk.nhs.adaptors.connector.dao.MigrationStatusLogDao;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.pss.gpc.amqp.PssQueuePublisher;

import javax.validation.constraints.NotNull;
import java.util.Locale;
import java.util.Map;

import static uk.nhs.adaptors.connector.model.MigrationStatus.*;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.CONFIRMATION_RESPONSE;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.CONVERSATION_ID;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AcknowledgeRecordService {
    private final PatientMigrationRequestDao patientMigrationRequestDao;
    private final MigrationStatusLogDao migrationStatusLogDao;
    private final PssQueuePublisher pssQueuePublisher;
    private final DateUtils dateUtils;

    public Boolean handleAcknowledgeRecord(@NotNull Map<String, String> headers) {

        var conversationIdHeaderValue = headers.get(CONVERSATION_ID);
        var confirmationResponseHeaderValue = headers.get(CONFIRMATION_RESPONSE);

        if(StringUtils.isBlank(conversationIdHeaderValue) || StringUtils.isBlank(confirmationResponseHeaderValue)) return false;

        confirmationResponseHeaderValue = confirmationResponseHeaderValue.toUpperCase(Locale.ROOT);

        var patientMigrationRequest = patientMigrationRequestDao.getMigrationRequest(conversationIdHeaderValue);
        if (patientMigrationRequest == null) return false;

        var patientMigrationRequestId = patientMigrationRequest.getId();

        var patientMigrationStatusLog = migrationStatusLogDao.getLatestMigrationStatusLog(patientMigrationRequestId);
        if(patientMigrationStatusLog.getMigrationStatus() != MigrationStatus.MIGRATION_COMPLETED) return false;

        ConfirmationResponse confirmationResponse;
        try {
            confirmationResponse = ConfirmationResponse.valueOf(confirmationResponseHeaderValue);
        }
        catch(Exception e) {
            //TODO: Log Error somewhere here?
            return false;
        }

        var patientNhsNumber = patientMigrationRequest.getPatientNhsNumber();
        var pssMessage = createAcknowledgeRecordMessage(
                patientNhsNumber,
                conversationIdHeaderValue,
                confirmationResponse);

        pssQueuePublisher.sendToPssQueue(pssMessage);

        var migrationStatus = confirmationResponse == ConfirmationResponse.ACCEPTED
                ? EHR_EXTRACT_REQUEST_ACKNOWLEDGED
                : EHR_EXTRACT_REQUEST_NEGATIVE_ACK;

        migrationStatusLogDao.addMigrationStatusLog(
                migrationStatus,
                dateUtils.getCurrentOffsetDateTime(),
                patientMigrationRequestId,
                null);

        return true;
    }
//
//    private void processInboundMessage(String inboundMessage) {
//
//    }

    private AcknowledgeRecordMessage createAcknowledgeRecordMessage(
            String patientNhsNumber,
            String conversationId,
            ConfirmationResponse confirmationResponse) {
        return AcknowledgeRecordMessage.builder()
            .conversationId(conversationId)
            .patientNhsNumber(patientNhsNumber)
            .messageType(QueueMessageType.ACKNOWLEDGE_RECORD)
            .confirmationResponse(confirmationResponse)
            .build();
    }
}
