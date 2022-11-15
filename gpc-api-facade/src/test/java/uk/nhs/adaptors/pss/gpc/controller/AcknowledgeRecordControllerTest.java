package uk.nhs.adaptors.pss.gpc.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.nhs.adaptors.pss.gpc.service.AcknowledgeRecordService;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AcknowledgeRecordControllerTest {
    private static final String CONVERSATION_ID_VALUE = UUID.randomUUID().toString();
    private static final String CONFIRMATION_RESPONSE_VALUE = "accepted";


    @Mock
    private AcknowledgeRecordService acknowledgeRecordService;

    @InjectMocks
    private AcknowledgeRecordController controller;


    @Test
    public void acknowledgeMigratedStructuredRecordWhenServiceReturnsFalseShouldReturn500() {
        when(acknowledgeRecordService.handleAcknowledgeRecord(CONFIRMATION_RESPONSE_VALUE, CONVERSATION_ID_VALUE))
                .thenReturn(false);

        ResponseEntity<String> response = controller.acknowledgeRecord(
                CONVERSATION_ID_VALUE,
                CONFIRMATION_RESPONSE_VALUE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    public void acknowledgeMigratedStructuredRecordWhenServiceReturnsTrueShouldReturn200() {
        when(acknowledgeRecordService.handleAcknowledgeRecord(CONVERSATION_ID_VALUE, CONFIRMATION_RESPONSE_VALUE))
                .thenReturn(true);

        ResponseEntity<String> response = controller.acknowledgeRecord(
                CONFIRMATION_RESPONSE_VALUE,
                CONVERSATION_ID_VALUE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }
}
