package uk.nhs.adaptors.pss.gpc.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;

import org.hl7.fhir.dstu3.model.Parameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.nhs.adaptors.connector.dao.MigrationStatusLogDao;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.MigrationStatusLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.util.DateUtils;
import uk.nhs.adaptors.pss.gpc.amqp.PssQueuePublisher;
import uk.nhs.adaptors.pss.gpc.testutil.CreateParametersUtil;

@ExtendWith(MockitoExtension.class)
public class PatientTransferServiceTest {
    private static final String REQUEST_BODY = "{testBody}";
    private static final String PATIENT_NHS_NUMBER = "123456789";

    @Mock
    private FhirParser fhirParser;

    @Mock
    private PatientMigrationRequestDao patientMigrationRequestDao;

    @Mock
    private MigrationStatusLogDao migrationStatusLogDao;

    @Mock
    private PssQueuePublisher pssQueuePublisher;

    @Mock
    private DateUtils dateUtils;

    @InjectMocks
    private PatientTransferService service;

    private Parameters parameters;

    @BeforeEach
    void setUp() {
        parameters = CreateParametersUtil.createValidParametersResource(PATIENT_NHS_NUMBER);
    }

    @Test
    public void handlePatientMigrationRequestWhenRequestIsNew() {
        var migrationRequestId = 1;
        OffsetDateTime now = OffsetDateTime.now();
        when(dateUtils.getCurrentOffsetDateTime()).thenReturn(now);
        when(patientMigrationRequestDao.getMigrationRequest(PATIENT_NHS_NUMBER)).thenReturn(null);
        when(patientMigrationRequestDao.getMigrationRequestId(PATIENT_NHS_NUMBER)).thenReturn(migrationRequestId);
        when(fhirParser.encodeToJson(parameters)).thenReturn(REQUEST_BODY);

        MigrationStatusLog patientMigrationRequest = service.handlePatientMigrationRequest(parameters);

        assertThat(patientMigrationRequest).isEqualTo(null);
        verify(pssQueuePublisher).sendToPssQueue(REQUEST_BODY);
        verify(patientMigrationRequestDao).addNewRequest(PATIENT_NHS_NUMBER);
        verify(migrationStatusLogDao).addMigrationStatusLog(MigrationStatus.REQUEST_RECEIVED, now, migrationRequestId);
    }

    @Test
    public void handlePatientMigrationRequestWhenRequestIsInProgress() {
        PatientMigrationRequest expectedPatientMigrationRequest = createPatientMigrationRequest();
        MigrationStatusLog expectedMigrationStatusLog = createMigrationStatusLog();

        when(patientMigrationRequestDao.getMigrationRequest(PATIENT_NHS_NUMBER)).thenReturn(expectedPatientMigrationRequest);
        when(migrationStatusLogDao.getMigrationStatusLog(expectedPatientMigrationRequest.getId())).thenReturn(expectedMigrationStatusLog);

        MigrationStatusLog patientMigrationRequest = service.handlePatientMigrationRequest(parameters);

        assertThat(patientMigrationRequest).isEqualTo(expectedMigrationStatusLog);
        verifyNoInteractions(pssQueuePublisher);
        verify(patientMigrationRequestDao).getMigrationRequest(PATIENT_NHS_NUMBER);
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
