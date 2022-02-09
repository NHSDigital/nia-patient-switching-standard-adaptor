package uk.nhs.adaptors.pss.translator.task;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.SneakyThrows;
import uk.nhs.adaptors.common.model.PssQueueMessage;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.config.GeneralProperties;
import uk.nhs.adaptors.pss.translator.mhs.MhsRequestBuilder;
import uk.nhs.adaptors.pss.translator.mhs.model.OutboundMessage;
import uk.nhs.adaptors.pss.translator.service.EhrExtractRequestService;
import uk.nhs.adaptors.pss.translator.service.MhsClientService;

@ExtendWith(MockitoExtension.class)
public class SendEhrExtractRequestHandlerTest {
    private static final String TEST_NHS_NUMBER = "123456";
    private static final String TEST_FROM_ODS_CODE = "TEST_FROM_ODS";
    private static final String TEST_PAYLOAD_BODY = "TEST_PAYLOAD_BODY";

    @Mock
    private MhsRequestBuilder builder;

    @Mock
    private EhrExtractRequestService ehrExtractRequestService;

    @Mock
    private GeneralProperties generalProperties;

    @Mock
    private WebClient.RequestHeadersSpec request;

    @Mock
    private MigrationStatusLogService migrationStatusLogService;

    @Mock
    private MhsClientService mhsClientService;

    @InjectMocks
    private SendEhrExtractRequestHandler sendEhrExtractRequestHandler;

    private PssQueueMessage pssQueueMessage;

    @BeforeEach
    @SneakyThrows
    public void setup() {
        pssQueueMessage = PssQueueMessage.builder()
            .patientNhsNumber(TEST_NHS_NUMBER)
            .build();
        when(generalProperties.getFromOdsCode()).thenReturn(TEST_FROM_ODS_CODE);
        when(ehrExtractRequestService.buildEhrExtractRequest(TEST_NHS_NUMBER, TEST_FROM_ODS_CODE)).thenReturn(TEST_PAYLOAD_BODY);
        when(builder.buildSendEhrExtractRequest(anyString(), anyString(), any(OutboundMessage.class))).thenReturn(request);
    }

    @Test
    public void whenSendMessageThenTrueIsReturned() {
        var isMessageSentSuccessfully = sendEhrExtractRequestHandler.prepareAndSendRequest(pssQueueMessage);

        assertTrue(isMessageSentSuccessfully);
        verify(migrationStatusLogService).addMigrationStatusLog(
            MigrationStatus.EHR_EXTRACT_REQUEST_ACCEPTED,
            TEST_NHS_NUMBER
        );
    }

    @Test
    public void whenSendMessageThenErrorFalseIsReturned() {
        when(mhsClientService.send(request)).thenThrow(
            new WebClientResponseException(
                HttpStatus.BAD_REQUEST.value(),
                "BAD REQUEST",
                new HttpHeaders(),
                new byte[]{},
                Charset.defaultCharset())
        );

        var isMessageSentSuccessfully = sendEhrExtractRequestHandler.prepareAndSendRequest(pssQueueMessage);

        assertFalse(isMessageSentSuccessfully);
        verify(migrationStatusLogService).addMigrationStatusLog(
            MigrationStatus.EHR_EXTRACT_REQUEST_ERROR,
            TEST_NHS_NUMBER
        );
    }
}
