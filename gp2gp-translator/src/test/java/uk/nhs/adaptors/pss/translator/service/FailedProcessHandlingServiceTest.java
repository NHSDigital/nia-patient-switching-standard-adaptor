package uk.nhs.adaptors.pss.translator.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_LRG_MSG_TIMEOUT;
import static uk.nhs.adaptors.pss.translator.model.NACKReason.LARGE_MESSAGE_GENERAL_FAILURE;
import static uk.nhs.adaptors.pss.translator.model.NACKReason.LARGE_MESSAGE_TIMEOUT;
import static uk.nhs.adaptors.pss.translator.model.NACKReason.UNEXPECTED_CONDITION;

import java.util.UUID;

import org.hl7.v3.COPCIN000001UK01Message;
import org.hl7.v3.II;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.model.MigrationStatusLog;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.model.NACKMessageData;
import uk.nhs.adaptors.pss.translator.task.SendNACKMessageHandler;

@ExtendWith(MockitoExtension.class)
public class FailedProcessHandlingServiceTest {

    @Mock
    private NackAckPreparationService nackAckPreparationService;

    @Mock
    private MigrationStatusLogService migrationStatusLogService;
    @Mock
    private SendNACKMessageHandler sendNACKMessageHandler;
    @Mock
    private RCMRIN030000UK06Message ehrExtractMessage;
    @Mock
    private COPCIN000001UK01Message copcMessage;
    @Mock
    private II mockId;
    @Mock
    private NACKMessageData messageData;

    @InjectMocks
    FailedProcessHandlingService failedProcessHandlingService;

    @ParameterizedTest
    @EnumSource(
        value = MigrationStatus.class,
        mode = EnumSource.Mode.EXCLUDE,
        names = {
            "REQUEST_RECEIVED",
            "EHR_EXTRACT_REQUEST_ACCEPTED",
            "EHR_EXTRACT_RECEIVED",
            "EHR_EXTRACT_PROCESSING",
            "EHR_EXTRACT_REQUEST_ACKNOWLEDGED",
            "EHR_EXTRACT_TRANSLATED",
            "CONTINUE_REQUEST_ACCEPTED",
            "COPC_MESSAGE_RECEIVED",
            "COPC_MESSAGE_PROCESSING",
            "COPC_ACKNOWLEDGED",
            "COPC_FAILED",
            "MIGRATION_COMPLETED",
            "FINAL_ACK_SENT",
            "CONTINUE_REQUEST_ERROR",
            "EHR_EXTRACT_REQUEST_ERROR"
        })
    public void When_HasProcessFailed_With_FailedMigrationStatus_Expect_True(MigrationStatus migrationStatus) {
        String conversationId = UUID.randomUUID().toString();
        MigrationStatusLog statusLog = MigrationStatusLog.builder()
            .migrationStatus(migrationStatus)
            .build();

        when(migrationStatusLogService.getLatestMigrationStatusLog(conversationId))
            .thenReturn(statusLog);

        boolean result = failedProcessHandlingService.hasProcessFailed(conversationId);

        assertThat(result).isTrue();
    }

    @Test
    public void When_HandleFailedProcess_With_EhrExtract_Expect_UnexpectedCondition() {
        String conversationId = UUID.randomUUID().toString();

        when(ehrExtractMessage.getId()).thenReturn(mockId);
        when(nackAckPreparationService.prepareNackMessageData(eq(UNEXPECTED_CONDITION), eq(ehrExtractMessage), eq(conversationId)))
            .thenReturn(messageData);

        failedProcessHandlingService.handleFailedProcess(ehrExtractMessage, conversationId);

        verify(sendNACKMessageHandler).prepareAndSendMessage(messageData);
    }

    @Test
    public void When_HandleFailedProcess_With_COPCAndIncumbentNackdProcess_Expect_UnexpectedCondition() {
        String conversationId = UUID.randomUUID().toString();
        MigrationStatusLog statusLog = MigrationStatusLog.builder()
            .migrationStatus(EHR_EXTRACT_REQUEST_NEGATIVE_ACK)
            .build();

        when(copcMessage.getId()).thenReturn(mockId);
        when(migrationStatusLogService.getLatestMigrationStatusLog(conversationId))
            .thenReturn(statusLog);

        when(nackAckPreparationService.prepareNackMessageData(eq(UNEXPECTED_CONDITION), eq(copcMessage), eq(conversationId)))
            .thenReturn(messageData);

        failedProcessHandlingService.handleFailedProcess(copcMessage, conversationId);

        verify(sendNACKMessageHandler).prepareAndSendMessage(messageData);
    }

    @Test
    public void When_HandleFailedProcess_With_COPCAndProcessTimeout_Expect_LargeMessageTimeout() {
        String conversationId = UUID.randomUUID().toString();
        MigrationStatusLog statusLog = MigrationStatusLog.builder()
            .migrationStatus(ERROR_LRG_MSG_TIMEOUT)
            .build();

        when(copcMessage.getId()).thenReturn(mockId);
        when(migrationStatusLogService.getLatestMigrationStatusLog(conversationId))
            .thenReturn(statusLog);

        when(nackAckPreparationService.prepareNackMessageData(eq(LARGE_MESSAGE_TIMEOUT), eq(copcMessage), eq(conversationId)))
            .thenReturn(messageData);

        failedProcessHandlingService.handleFailedProcess(copcMessage, conversationId);

        verify(sendNACKMessageHandler).prepareAndSendMessage(messageData);
    }

    @ParameterizedTest
    @EnumSource(
        value = MigrationStatus.class,
        mode = EnumSource.Mode.EXCLUDE,
        names = {
            "REQUEST_RECEIVED",
            "EHR_EXTRACT_REQUEST_ACCEPTED",
            "EHR_EXTRACT_RECEIVED",
            "EHR_EXTRACT_PROCESSING",
            "EHR_EXTRACT_REQUEST_ACKNOWLEDGED",
            "EHR_EXTRACT_TRANSLATED",
            "CONTINUE_REQUEST_ACCEPTED",
            "COPC_MESSAGE_RECEIVED",
            "COPC_MESSAGE_PROCESSING",
            "COPC_ACKNOWLEDGED",
            "COPC_FAILED",
            "MIGRATION_COMPLETED",
            "FINAL_ACK_SENT",
            "ERROR_LRG_MSG_TIMEOUT",
            "EHR_EXTRACT_REQUEST_NEGATIVE_ACK"
        })
    public void When_HandleFailedProcess_With_OtherStatus_Expect_LargeMessageGeneralFailure(MigrationStatus migrationStatus) {
        String conversationId = UUID.randomUUID().toString();
        MigrationStatusLog statusLog = MigrationStatusLog.builder()
            .migrationStatus(migrationStatus)
            .build();

        when(copcMessage.getId()).thenReturn(mockId);
        when(migrationStatusLogService.getLatestMigrationStatusLog(conversationId))
            .thenReturn(statusLog);

        when(nackAckPreparationService.prepareNackMessageData(eq(LARGE_MESSAGE_GENERAL_FAILURE), eq(copcMessage), eq(conversationId)))
            .thenReturn(messageData);

        failedProcessHandlingService.handleFailedProcess(copcMessage, conversationId);

        verify(sendNACKMessageHandler).prepareAndSendMessage(messageData);
    }
}
