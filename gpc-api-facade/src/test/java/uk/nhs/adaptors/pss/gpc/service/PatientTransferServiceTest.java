package uk.nhs.adaptors.pss.gpc.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.Identifier;
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
import uk.nhs.adaptors.connector.model.RequestStatus;
import uk.nhs.adaptors.pss.gpc.amqp.PssQueuePublisher;
import uk.nhs.adaptors.pss.gpc.util.DateUtils;

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
        parameters = createParametersResource();
    }

    @Test
    public void handlePatientMigrationRequestWhenRequestIsNew() {
        OffsetDateTime now = OffsetDateTime.now();
        when(dateUtils.getCurrentOffsetDateTime()).thenReturn(now);
        when(patientMigrationRequestDao.getMigrationRequest(PATIENT_NHS_NUMBER)).thenReturn(null);
        when(fhirParser.encodeToJson(parameters)).thenReturn(REQUEST_BODY);

        MigrationStatusLog patientMigrationRequest = service.handlePatientMigrationRequest(parameters);

        assertThat(patientMigrationRequest).isEqualTo(null);
        verify(pssQueuePublisher).sendToPssQueue(REQUEST_BODY);
        verify(patientMigrationRequestDao).addNewRequest(PATIENT_NHS_NUMBER);
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

    private Parameters createParametersResource() {
        Parameters parameters = new Parameters();

        Parameters.ParametersParameterComponent nhsNumberComponent = new Parameters.ParametersParameterComponent();
        nhsNumberComponent.setName("patientNHSNumber");
        Identifier identifier = new Identifier();
        identifier
            .setSystem("https://fhir.nhs.uk/Id/nhs-number")
            .setValue(PATIENT_NHS_NUMBER);
        nhsNumberComponent.setValue(identifier);

        BooleanType booleanType = new BooleanType();
        booleanType.setValue(true);
        Parameters.ParametersParameterComponent sensitiveInformationPart = new Parameters.ParametersParameterComponent();
        sensitiveInformationPart
            .setName("includeSensitiveInformation")
            .setValue(booleanType);
        Parameters.ParametersParameterComponent fullRecordComponent = new Parameters.ParametersParameterComponent();
        fullRecordComponent
            .setName("includeFullRecord")
            .addPart(sensitiveInformationPart);

        parameters
            .addParameter(nhsNumberComponent)
            .addParameter(fullRecordComponent);

        return parameters;
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
            .requestStatus(RequestStatus.MHS_ACCEPTED)
            .date(OffsetDateTime.now())
            .migrationRequestId(1)
            .build();
    }
}
