package uk.nhs.adaptors.pss.gpc.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.common.enums.QueueMessageType.TRANSFER_REQUEST;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.FROM_ASID;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.FROM_ODS;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.TO_ASID;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.TO_ODS;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import org.hl7.fhir.dstu3.model.Parameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.nhs.adaptors.common.model.TransferRequestMessage;
import uk.nhs.adaptors.common.service.MDCService;
import uk.nhs.adaptors.common.testutil.CreateParametersUtil;
import uk.nhs.adaptors.common.util.DateUtils;
import uk.nhs.adaptors.connector.dao.MigrationStatusLogDao;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.model.MigrationStatusLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.pss.gpc.amqp.PssQueuePublisher;

@ExtendWith(MockitoExtension.class)
public class PatientTransferServiceTest {
    private static final String PATIENT_NHS_NUMBER = "123456789";
    private static final String CONVERSATION_ID = UUID.randomUUID().toString();
    private static final String LOSING_ODS_CODE = "D443";
    private static final String WINNING_ODS_CODE = "ABC";

    private static final Map<String, String> HEADERS = Map.of(
        TO_ASID, "1234",
        FROM_ASID, "5678",
        TO_ODS, LOSING_ODS_CODE,
        FROM_ODS, WINNING_ODS_CODE
    );

    @Mock
    private PatientMigrationRequestDao patientMigrationRequestDao;

    @Mock
    private MigrationStatusLogDao migrationStatusLogDao;

    @Mock
    private PssQueuePublisher pssQueuePublisher;

    @Mock
    private DateUtils dateUtils;

    @Mock
    private MDCService mdcService;

    @InjectMocks
    private PatientTransferService service;

    private Parameters parameters;

    @BeforeEach
    void setUp() {
        parameters = CreateParametersUtil.createValidParametersResource(PATIENT_NHS_NUMBER);
    }

    @Test
    public void handlePatientMigrationRequestWhenRequestIsNew() {
        var expectedPssQueueMessage = TransferRequestMessage.builder()
            .conversationId(CONVERSATION_ID)
            .patientNhsNumber(PATIENT_NHS_NUMBER)
            .toAsid(HEADERS.get(TO_ASID))
            .fromAsid(HEADERS.get(FROM_ASID))
            .toOds(HEADERS.get(TO_ODS))
            .fromOds(HEADERS.get(FROM_ODS))
            .messageType(TRANSFER_REQUEST)
            .build();
        var migrationRequestId = 1;
        OffsetDateTime now = OffsetDateTime.now();
        when(dateUtils.getCurrentOffsetDateTime()).thenReturn(now);
        when(patientMigrationRequestDao.getMigrationRequest(CONVERSATION_ID)).thenReturn(null);
        when(patientMigrationRequestDao.getMigrationRequestId(CONVERSATION_ID)).thenReturn(migrationRequestId);
        when(mdcService.getConversationId()).thenReturn(CONVERSATION_ID);

        MigrationStatusLog patientMigrationRequest = service.handlePatientMigrationRequest(parameters, HEADERS);

        assertThat(patientMigrationRequest).isEqualTo(null);
        verify(pssQueuePublisher).sendToPssQueue(expectedPssQueueMessage);
        verify(patientMigrationRequestDao).addNewRequest(PATIENT_NHS_NUMBER, CONVERSATION_ID, LOSING_ODS_CODE, WINNING_ODS_CODE);
        verify(migrationStatusLogDao).addMigrationStatusLog(MigrationStatus.REQUEST_RECEIVED, now, migrationRequestId, null);
    }

    @Test
    public void handlePatientMigrationRequestWhenRequestIsInProgress() {
        PatientMigrationRequest expectedPatientMigrationRequest = createPatientMigrationRequest();
        MigrationStatusLog expectedMigrationStatusLog = createMigrationStatusLog();

        when(mdcService.getConversationId()).thenReturn(CONVERSATION_ID);
        when(patientMigrationRequestDao.getMigrationRequest(CONVERSATION_ID)).thenReturn(expectedPatientMigrationRequest);
        when(migrationStatusLogDao.getLatestMigrationStatusLog(expectedPatientMigrationRequest.getId()))
            .thenReturn(expectedMigrationStatusLog);

        MigrationStatusLog patientMigrationRequest = service.handlePatientMigrationRequest(parameters, HEADERS);

        assertThat(patientMigrationRequest).isEqualTo(expectedMigrationStatusLog);
        verifyNoInteractions(pssQueuePublisher);
        verify(patientMigrationRequestDao).getMigrationRequest(CONVERSATION_ID);
        verifyNoMoreInteractions(patientMigrationRequestDao);
    }

    private PatientMigrationRequest createPatientMigrationRequest() {
        return PatientMigrationRequest.builder()
            .id(1)
            .patientNhsNumber(PATIENT_NHS_NUMBER)
            .build();
    }

    private MigrationStatusLog createMigrationStatusLog() {
        return MigrationStatusLog.builder()
            .id(1)
            .migrationStatus(MigrationStatus.REQUEST_RECEIVED)
            .date(OffsetDateTime.now())
            .migrationRequestId(1)
            .build();
    }
}
