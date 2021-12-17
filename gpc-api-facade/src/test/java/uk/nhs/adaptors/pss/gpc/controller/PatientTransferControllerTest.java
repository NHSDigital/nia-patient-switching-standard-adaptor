package uk.nhs.adaptors.pss.gpc.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import org.hl7.fhir.dstu3.model.Parameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.nhs.adaptors.pss.gpc.model.TransferStatus;
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
        when(patientTransferService.getEmptyBundle()).thenReturn(RESPONSE_BODY);
        when(patientTransferService.handlePatientMigrationRequest(PARAMETERS)).thenReturn(TransferStatus.NEW);

        ResponseEntity<String> response = controller.migratePatientStructuredRecord(REQUEST_BODY);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isEqualTo(RESPONSE_BODY);
    }

    @Test
    public void migratePatientStructuredRecordWhenTransferStatusIsInProgress() {
        when(patientTransferService.handlePatientMigrationRequest(PARAMETERS)).thenReturn(TransferStatus.IN_PROGRESS);

        ResponseEntity<String> response = controller.migratePatientStructuredRecord(REQUEST_BODY);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }
}
