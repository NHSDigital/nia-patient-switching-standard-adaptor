package uk.nhs.adaptors.pss.gpc.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.connector.model.RequestStatus.COMPLETED;
import static uk.nhs.adaptors.connector.model.RequestStatus.ERROR;
import static uk.nhs.adaptors.connector.model.RequestStatus.IN_PROGRESS;
import static uk.nhs.adaptors.connector.model.RequestStatus.RECEIVED;

import java.time.OffsetDateTime;

import org.hl7.fhir.dstu3.model.Parameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.model.RequestStatus;
import uk.nhs.adaptors.pss.gpc.service.FhirParser;
import uk.nhs.adaptors.pss.gpc.service.PatientTransferService;

@ExtendWith(MockitoExtension.class)
public class PatientTransferControllerTest {
    private static final String REQUEST_BODY = "{testBody}";
    private static final String RESPONSE_BODY = "{responseBody}";
    private static final Parameters PARAMETERS = new Parameters();

    @Mock
    private PatientTransferService patientTransferService;

    @Mock
    private FhirParser fhirParser;

    @InjectMocks
    private PatientTransferController controller;


    @BeforeEach
    void setUp() {
        when(fhirParser.parseResource(REQUEST_BODY, Parameters.class)).thenReturn(PARAMETERS);
    }

    @Test
    public void migratePatientStructuredRecordWhenTransferStatusIsNew() {
        when(patientTransferService.handlePatientMigrationRequest(PARAMETERS)).thenReturn(null);

        ResponseEntity<String> response = controller.migratePatientStructuredRecord(REQUEST_BODY);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNull();
    }

    @Test
    public void migratePatientStructuredRecordWhenTransferStatusIsReceived() {
        when(patientTransferService.handlePatientMigrationRequest(PARAMETERS)).thenReturn(createPatientMigrationRequest(RECEIVED));

        ResponseEntity<String> response = controller.migratePatientStructuredRecord(REQUEST_BODY);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    public void migratePatientStructuredRecordWhenTransferStatusIsInProgress() {
        when(patientTransferService.handlePatientMigrationRequest(PARAMETERS)).thenReturn(createPatientMigrationRequest(IN_PROGRESS));

        ResponseEntity<String> response = controller.migratePatientStructuredRecord(REQUEST_BODY);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    public void migratePatientStructuredRecordWhenTransferStatusIsCompleted() {
        when(patientTransferService.handlePatientMigrationRequest(PARAMETERS)).thenReturn(createPatientMigrationRequest(COMPLETED));
        when(patientTransferService.getEmptyBundle()).thenReturn(RESPONSE_BODY);

        ResponseEntity<String> response = controller.migratePatientStructuredRecord(REQUEST_BODY);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(RESPONSE_BODY);
    }

    @Test
    public void migratePatientStructuredRecordWhenTransferStatusIsUnsupported() {
        when(patientTransferService.handlePatientMigrationRequest(PARAMETERS)).thenReturn(createPatientMigrationRequest(ERROR));

        Exception exception = assertThrows(IllegalStateException.class, () -> controller.migratePatientStructuredRecord(REQUEST_BODY));
        assertThat(exception.getMessage()).isEqualTo("Unsupported transfer status: ERROR");
    }

    private PatientMigrationRequest createPatientMigrationRequest(RequestStatus status) {
        return PatientMigrationRequest.builder()
            .id(1)
            .patientNhsNumber("123456789")
            .requestStatus(status)
            .date(OffsetDateTime.now())
            .build();
    }
}
