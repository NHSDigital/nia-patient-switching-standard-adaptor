package uk.nhs.adaptors.pss.gpc.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_ACCEPTED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_ERROR;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK;
import static uk.nhs.adaptors.connector.model.MigrationStatus.MIGRATION_COMPLETED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.REQUEST_RECEIVED;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.FROM_ASID;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.FROM_ODS;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.TO_ASID;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.TO_ODS;

import java.time.OffsetDateTime;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Parameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.model.MigrationStatusLog;
import uk.nhs.adaptors.pss.gpc.service.PatientTransferService;

@ExtendWith(MockitoExtension.class)
public class PatientTransferControllerTest {
    private static final String RESPONSE_BODY = "{responseBody}";
    private static final Parameters PARAMETERS = new Parameters();
    private static final String TO_ASID_VALUE = "123";
    private static final String FROM_ASID_VALUE = "321";
    private static final String TO_ODS_VALUE = "ABC";
    private static final String FROM_ODS_VALUE = "DEF";
    private static final Map<String, String> HEADERS = Map.of(
        TO_ASID, TO_ASID_VALUE,
        FROM_ASID, FROM_ASID_VALUE,
        TO_ODS, TO_ODS_VALUE,
        FROM_ODS, FROM_ODS_VALUE
    );

    @Mock
    private PatientTransferService patientTransferService;

    @InjectMocks
    private PatientTransferController controller;

    @Test
    public void migratePatientStructuredRecordWhenTransferStatusIsNew() {
        when(patientTransferService.handlePatientMigrationRequest(PARAMETERS, HEADERS)).thenReturn(null);

        ResponseEntity<String> response = controller.migratePatientStructuredRecord(
            PARAMETERS, TO_ASID_VALUE, FROM_ASID_VALUE, TO_ODS_VALUE, FROM_ODS_VALUE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNull();
    }

    @Test
    public void migratePatientStructuredRecordWhenTransferStatusIsReceived() {
        when(patientTransferService.handlePatientMigrationRequest(PARAMETERS, HEADERS))
            .thenReturn(createMigrationStatusLog(REQUEST_RECEIVED));

        ResponseEntity<String> response = controller.migratePatientStructuredRecord(
            PARAMETERS, TO_ASID_VALUE, FROM_ASID_VALUE, TO_ODS_VALUE, FROM_ODS_VALUE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    public void migratePatientStructuredRecordWhenTransferStatusIsInProgress() {
        when(patientTransferService.handlePatientMigrationRequest(PARAMETERS, HEADERS))
            .thenReturn(createMigrationStatusLog(EHR_EXTRACT_REQUEST_ACCEPTED));

        ResponseEntity<String> response = controller.migratePatientStructuredRecord(
            PARAMETERS, TO_ASID_VALUE, FROM_ASID_VALUE, TO_ODS_VALUE, FROM_ODS_VALUE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    public void migratePatientStructuredRecordWhenTransferStatusIsCompleted() {
        when(patientTransferService.handlePatientMigrationRequest(PARAMETERS, HEADERS))
            .thenReturn(createMigrationStatusLog(MIGRATION_COMPLETED));
        when(patientTransferService.getBundleResource()).thenReturn(RESPONSE_BODY);

        ResponseEntity<String> response = controller.migratePatientStructuredRecord(
            PARAMETERS, TO_ASID_VALUE, FROM_ASID_VALUE, TO_ODS_VALUE, FROM_ODS_VALUE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(RESPONSE_BODY);
    }

    @Test
    public void migratePatientStructuredRecordWhenTransferStatusIsUnsupported() {
        when(patientTransferService.handlePatientMigrationRequest(PARAMETERS, HEADERS))
            .thenReturn(createMigrationStatusLog(EHR_EXTRACT_REQUEST_ERROR));

        Exception exception = assertThrows(IllegalStateException.class, () -> controller.migratePatientStructuredRecord(
            PARAMETERS, TO_ASID_VALUE, FROM_ASID_VALUE, TO_ODS_VALUE, FROM_ODS_VALUE));
        assertThat(exception.getMessage()).isEqualTo("Unsupported transfer status: EHR_EXTRACT_REQUEST_ERROR");
    }

    @Test
    public void migratePatientStructuredRecordWhenTransferStatusIsEhrExtractNegativeAck() {
        when(patientTransferService.handlePatientMigrationRequest(PARAMETERS, HEADERS))
            .thenReturn(createMigrationStatusLog(EHR_EXTRACT_REQUEST_NEGATIVE_ACK));

        ResponseEntity<String> response = controller.migratePatientStructuredRecord(
            PARAMETERS, TO_ASID_VALUE, FROM_ASID_VALUE, TO_ODS_VALUE, FROM_ODS_VALUE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private MigrationStatusLog createMigrationStatusLog(MigrationStatus status) {
        return MigrationStatusLog.builder()
            .id(1)
            .migrationStatus(status)
            .date(OffsetDateTime.now())
            .migrationRequestId(1)
            .build();
    }
}
