package uk.nhs.adaptors.pss.gpc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.common.enums.ConfirmationResponse;
import uk.nhs.adaptors.common.enums.QueueMessageType;
import uk.nhs.adaptors.common.model.AcknowledgeRecordMessage;
import uk.nhs.adaptors.common.util.DateUtils;
import uk.nhs.adaptors.connector.dao.MigrationStatusLogDao;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.model.MigrationStatusLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.pss.gpc.amqp.PssQueuePublisher;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.CONFIRMATION_RESPONSE;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.CONVERSATION_ID;

@ExtendWith(MockitoExtension.class)
public class AcknowledgeRecordServiceTest {
    private static final String CONVERSATION_ID_VALUE = UUID.randomUUID().toString();
    private static final String INVALID_CONFIRMATION_RESPONSE_VALUE = "NotAValidResponse";
    private static final String PATIENT_NHS_NUMBER = "123456789";
    private static final Integer PATIENT_MIGRATION_REQUEST_ID = 1;

    @Mock
    private PatientMigrationRequestDao patientMigrationRequestDao;

    @Mock
    private MigrationStatusLogDao migrationStatusLogDao;

    @Mock
    private PssQueuePublisher pssQueuePublisher;

    @Mock
    private PatientMigrationRequest patientMigrationRequest;

    @Mock
    private MigrationStatusLog migrationStatusLog;

    @Mock
    private DateUtils dateUtils;

    private static final OffsetDateTime datetimeNow = OffsetDateTime.now();

    @InjectMocks
    private AcknowledgeRecordService service;

    @Test
    public void handleAcknowledgeRecordRequestShouldReturnFalseWhenConversationIdIsEmpty() {
        Map<String, String> headers = Map.of(
                CONVERSATION_ID, "",
                CONFIRMATION_RESPONSE, ConfirmationResponse.ACCEPTED.name()
        );

        var result = service.handleAcknowledgeRecord(headers);

        assertThat(result).isFalse();
    }

    @Test
    public void handleAcknowledgeRecordRequestShouldReturnFalseWhenConfirmationResponseIsEmpty() {
        Map<String, String> headers = Map.of(
                CONVERSATION_ID, CONVERSATION_ID_VALUE,
                CONFIRMATION_RESPONSE, ""
        );

        var result = service.handleAcknowledgeRecord(headers);

        assertThat(result).isFalse();
    }

    @Test
    public void handleAcknowledgeRecordRequestShouldReturnFalseWhenMigrationRequestNotFound() {
        var headers = getValidHeaders(ConfirmationResponse.ACCEPTED);

        when(patientMigrationRequestDao.getMigrationRequest(CONVERSATION_ID_VALUE))
                .thenReturn(null);

        var result = service.handleAcknowledgeRecord(headers);

        assertThat(result).isFalse();
        verify(patientMigrationRequestDao, times(1)).getMigrationRequest(CONVERSATION_ID_VALUE);
    }

    @Test
    public void handleAcknowledgeRecordRequestShouldReturnFalseWhenStatusIsNotMigrationCompleted() {
        var headers = getValidHeaders(ConfirmationResponse.ACCEPTED);

        when(patientMigrationRequestDao.getMigrationRequest(CONVERSATION_ID_VALUE))
                .thenReturn(patientMigrationRequest);
        when(patientMigrationRequest.getId())
                .thenReturn(PATIENT_MIGRATION_REQUEST_ID);
        when(migrationStatusLogDao.getLatestMigrationStatusLog(PATIENT_MIGRATION_REQUEST_ID))
                .thenReturn(migrationStatusLog);
        when(migrationStatusLog.getMigrationStatus())
                .thenReturn(MigrationStatus.REQUEST_RECEIVED);

        var result = service.handleAcknowledgeRecord(headers);

        assertThat(result).isFalse();
        verify(patientMigrationRequestDao, times(1)).getMigrationRequest(CONVERSATION_ID_VALUE);
        verify(migrationStatusLogDao, times(1)).getLatestMigrationStatusLog(PATIENT_MIGRATION_REQUEST_ID);
    }

    @Test
    public void HandleAcknowledgeRecordRequestShouldReturnFalseWhenConfirmationResponseIsInvalid() {
        Map<String, String> headers = Map.of(
                CONVERSATION_ID, CONVERSATION_ID_VALUE,
                CONFIRMATION_RESPONSE,  INVALID_CONFIRMATION_RESPONSE_VALUE
        );

        when(patientMigrationRequestDao.getMigrationRequest(CONVERSATION_ID_VALUE))
                .thenReturn(patientMigrationRequest);
        when(patientMigrationRequest.getId())
                .thenReturn(PATIENT_MIGRATION_REQUEST_ID);
        when(migrationStatusLogDao.getLatestMigrationStatusLog(PATIENT_MIGRATION_REQUEST_ID))
                .thenReturn(migrationStatusLog);
        when(migrationStatusLog.getMigrationStatus())
                .thenReturn(MigrationStatus.MIGRATION_COMPLETED);


        var result = service.handleAcknowledgeRecord(headers);

        assertThat(result).isFalse();
        verify(patientMigrationRequestDao, times(1)).getMigrationRequest(CONVERSATION_ID_VALUE);
        verify(migrationStatusLogDao, times(1)).getLatestMigrationStatusLog(PATIENT_MIGRATION_REQUEST_ID);
    }

    @Test
    public void HandleAcknowledgeRecordRequestShouldReturnTrueWhenConfirmationResponseIsAccepted() {
        configureMocksForValidResponse();

        var headers = getValidHeaders(ConfirmationResponse.ACCEPTED);

        var expectedPssMessage = AcknowledgeRecordMessage.builder()
                .conversationId(CONVERSATION_ID_VALUE)
                .patientNhsNumber(PATIENT_NHS_NUMBER)
                .messageType(QueueMessageType.ACKNOWLEDGE_RECORD)
                .confirmationResponse(ConfirmationResponse.ACCEPTED)
                .build();

        var result = service.handleAcknowledgeRecord(headers);

        assertThat(result).isTrue();
        verify(patientMigrationRequestDao, times(1)).getMigrationRequest(CONVERSATION_ID_VALUE);
        verify(migrationStatusLogDao, times(1)).getLatestMigrationStatusLog(PATIENT_MIGRATION_REQUEST_ID);
        verify(migrationStatusLogDao, times(1)).addMigrationStatusLog(
                MigrationStatus.EHR_EXTRACT_REQUEST_ACKNOWLEDGED,
                datetimeNow,
                PATIENT_MIGRATION_REQUEST_ID,
                null);
       verify(pssQueuePublisher, times(1)).sendToPssQueue(expectedPssMessage);
    }

    @ParameterizedTest
    @EnumSource(value = ConfirmationResponse.class, names = { "ACCEPTED" }, mode = EnumSource.Mode.EXCLUDE)
    public void HandleAcknowledgeRecordRequestShouldReturnTrueWhenNegativeAcknowledgement(
            ConfirmationResponse response) {
        configureMocksForValidResponse();

        var headers = getValidHeaders(response);

        var expectedPssMessage = AcknowledgeRecordMessage.builder()
                .conversationId(CONVERSATION_ID_VALUE)
                .patientNhsNumber(PATIENT_NHS_NUMBER)
                .messageType(QueueMessageType.ACKNOWLEDGE_RECORD)
                .confirmationResponse(response)
                .build();

        var result = service.handleAcknowledgeRecord(headers);

        assertThat(result).isTrue();
        verify(patientMigrationRequestDao, times(1)).getMigrationRequest(CONVERSATION_ID_VALUE);
        verify(migrationStatusLogDao, times(1)).getLatestMigrationStatusLog(PATIENT_MIGRATION_REQUEST_ID);
        verify(migrationStatusLogDao, times(1)).addMigrationStatusLog(
                MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK,
                datetimeNow,
                PATIENT_MIGRATION_REQUEST_ID,
                null);
        verify(pssQueuePublisher, times(1)).sendToPssQueue(expectedPssMessage);
    }

    private void configureMocksForValidResponse() {
        when(dateUtils.getCurrentOffsetDateTime())
                .thenReturn(datetimeNow);
        when(patientMigrationRequestDao.getMigrationRequest(CONVERSATION_ID_VALUE))
                .thenReturn(patientMigrationRequest);
        when(patientMigrationRequest.getId())
                .thenReturn(PATIENT_MIGRATION_REQUEST_ID);
        when(patientMigrationRequest.getPatientNhsNumber())
                .thenReturn(PATIENT_NHS_NUMBER);
        when(migrationStatusLog.getMigrationStatus())
                .thenReturn(MigrationStatus.MIGRATION_COMPLETED);
        when(migrationStatusLogDao.getLatestMigrationStatusLog(PATIENT_MIGRATION_REQUEST_ID))
                .thenReturn(migrationStatusLog);
    }
    private Map<String, String> getValidHeaders(ConfirmationResponse response) {
        return Map.of(
                CONVERSATION_ID, CONVERSATION_ID_VALUE,
                CONFIRMATION_RESPONSE, response.name()
        );
    }
}
