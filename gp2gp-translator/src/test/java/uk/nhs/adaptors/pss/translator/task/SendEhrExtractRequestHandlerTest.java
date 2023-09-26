package uk.nhs.adaptors.pss.translator.task;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import uk.nhs.adaptors.common.enums.MigrationStatus;
import uk.nhs.adaptors.common.model.TransferRequestMessage;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.MhsRequestBuilder;
import uk.nhs.adaptors.pss.translator.mhs.model.OutboundMessage;
import uk.nhs.adaptors.pss.translator.service.EhrExtractRequestService;
import uk.nhs.adaptors.pss.translator.service.IdGeneratorService;
import uk.nhs.adaptors.pss.translator.service.MhsClientService;

@ExtendWith(MockitoExtension.class)
public class SendEhrExtractRequestHandlerTest {
    private static final String TEST_NHS_NUMBER = "123456";
    private static final String TEST_TO_ODS_CODE = "TEST_FROM_ODS";
    private static final String TEST_PAYLOAD_BODY = "TEST_PAYLOAD_BODY";
    private static final String CONVERSATION_ID = "abc-236";
    private static final String TEST_MESSAGE_ID = "message-id";

    @Mock
    private MhsRequestBuilder builder;

    @Mock
    private EhrExtractRequestService ehrExtractRequestService;

    @Mock
    private WebClient.RequestHeadersSpec request;

    @Mock
    private MigrationStatusLogService migrationStatusLogService;

    @Mock
    private MhsClientService mhsClientService;
    @Mock
    private IdGeneratorService idGeneratorService;

    @InjectMocks
    private SendEhrExtractRequestHandler sendEhrExtractRequestHandler;

    private TransferRequestMessage pssQueueMessage;

    @BeforeEach
    @SneakyThrows
    public void setup() {
        pssQueueMessage = TransferRequestMessage.builder()
            .patientNhsNumber(TEST_NHS_NUMBER)
            .toOds(TEST_TO_ODS_CODE)
            .conversationId(CONVERSATION_ID)
            .build();

        when(idGeneratorService.generateUuid()).thenReturn(TEST_MESSAGE_ID);

        when(ehrExtractRequestService.buildEhrExtractRequest(eq(pssQueueMessage), anyString())).thenReturn(TEST_PAYLOAD_BODY);
        when(builder.buildSendEhrExtractRequest(eq(CONVERSATION_ID), eq(TEST_TO_ODS_CODE), any(OutboundMessage.class),
            eq(TEST_MESSAGE_ID.toUpperCase())))
            .thenReturn(request);
    }

    @Test
    public void whenSendMessageThenTrueIsReturned() {
        var isMessageSentSuccessfully = sendEhrExtractRequestHandler.prepareAndSendRequest(pssQueueMessage);

        assertTrue(isMessageSentSuccessfully);
        verify(migrationStatusLogService).addMigrationStatusLog(
            MigrationStatus.EHR_EXTRACT_REQUEST_ACCEPTED,
            CONVERSATION_ID,
            null
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
            CONVERSATION_ID,
            null
        );
    }
}
