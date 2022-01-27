package uk.nhs.adaptors.pss.translator.task;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.pss.translator.testutil.CreateParametersUtil.createValidParametersResource;

import java.nio.charset.Charset;
import java.time.OffsetDateTime;

import javax.jms.Message;

import org.hl7.fhir.dstu3.model.Parameters;
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
import uk.nhs.adaptors.connector.dao.MigrationStatusLogDao;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.RequestStatus;
import uk.nhs.adaptors.pss.translator.config.GeneralProperties;
import uk.nhs.adaptors.pss.translator.mhs.MhsRequestBuilder;
import uk.nhs.adaptors.pss.translator.model.OutboundMessage;
import uk.nhs.adaptors.pss.translator.service.EhrExtractRequestService;
import uk.nhs.adaptors.pss.translator.service.MhsClientService;
import uk.nhs.adaptors.pss.translator.util.DateUtils;
import uk.nhs.adaptors.pss.translator.util.FhirParser;

@ExtendWith(MockitoExtension.class)
public class SendEhrExtractRequestHandlerTest {
    @Mock
    private FhirParser fhirParser;

    @Mock
    private MhsRequestBuilder builder;

    @Mock
    private EhrExtractRequestService ehrExtractRequestService;

    @Mock
    private Message message;

    @Mock
    private GeneralProperties generalProperties;

    @Mock
    private WebClient.RequestHeadersSpec request;

    @Mock
    private PatientMigrationRequestDao patientMigrationRequestDao;

    @Mock
    private MigrationStatusLogDao migrationStatusLogDao;

    @Mock
    private MhsClientService mhsClientService;

    @Mock
    private DateUtils dateUtils;

    @InjectMocks
    private SendEhrExtractRequestHandler sendEhrExtractRequestHandler;

    private static final String TEST_NHS_NUMBER = "123456";
    private static final String TEST_FROM_ODS_CODE = "TEST_FROM_ODS";
    private static final String TEST_PAYLOAD_BODY = "TEST_PAYLOAD_BODY";

    @BeforeEach
    @SneakyThrows
    public void setup() {
        when(message.getBody(String.class)).thenReturn("MESSAGE_BODY");
        when(fhirParser.parseResource(message.getBody(String.class), Parameters.class))
            .thenReturn(createValidParametersResource(TEST_NHS_NUMBER));
        when(generalProperties.getFromOdsCode()).thenReturn(TEST_FROM_ODS_CODE);
        when(ehrExtractRequestService.buildEhrExtractRequest(TEST_NHS_NUMBER, TEST_FROM_ODS_CODE)).thenReturn(TEST_PAYLOAD_BODY);
        when(builder.buildSendEhrExtractRequest(anyString(), anyString(), any(OutboundMessage.class))).thenReturn(request);
        when(dateUtils.getCurrentOffsetDateTime()).thenReturn(OffsetDateTime.MIN);
    }

    @Test
    public void whenSendMessageThenTrueIsReturned() {
        var isMessageSentSuccessfully = sendEhrExtractRequestHandler.prepareAndSendRequest(message);

        assertTrue(isMessageSentSuccessfully);
        verify(patientMigrationRequestDao).getMigrationRequestId(TEST_NHS_NUMBER);
        verify(migrationStatusLogDao).addMigrationStatusLog(
            RequestStatus.EHR_EXTRACT_REQUEST_ACCEPTED.name(),
            dateUtils.getCurrentOffsetDateTime(),
            0
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

        var isMessageSentSuccessfully = sendEhrExtractRequestHandler.prepareAndSendRequest(message);

        assertFalse(isMessageSentSuccessfully);
        verify(patientMigrationRequestDao).getMigrationRequestId(TEST_NHS_NUMBER);
        verify(migrationStatusLogDao).addMigrationStatusLog(
            RequestStatus.EHR_EXTRACT_REQUEST_ERROR.name(),
            dateUtils.getCurrentOffsetDateTime(),
            0
        );
    }
}
