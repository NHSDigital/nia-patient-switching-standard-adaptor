package uk.nhs.adaptors.pss.gpc.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.nhs.adaptors.pss.gpc.GpcFacadeApplication;
import uk.nhs.adaptors.pss.gpc.service.MigratePatientStructuredRecordService;

@SpringBootTest(classes = {GpcFacadeApplication.class})
public class PatientTransferControllerTest {
    private static final String REQUEST_BODY = "{testBody}";

    @InjectMocks
    private PatientTransferController controller;

    @Mock
    private MigratePatientStructuredRecordService migratePatientStructuredRecordService;

    @Test
    public void migratePatientStructuredRecord() {
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("response", HttpStatus.ACCEPTED);
        when(migratePatientStructuredRecordService.handlePatientMigrationRequest(REQUEST_BODY)).thenReturn(expectedResponse);

        ResponseEntity<String> response = controller.migratePatientStructuredRecord(REQUEST_BODY);
        assertThat(response.getStatusCode()).isEqualTo(expectedResponse.getStatusCode());
        assertThat(response.getBody()).isEqualTo(expectedResponse.getBody());
    }
}
