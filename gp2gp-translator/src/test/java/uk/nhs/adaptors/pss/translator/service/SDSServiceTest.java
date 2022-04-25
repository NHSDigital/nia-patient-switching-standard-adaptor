package uk.nhs.adaptors.pss.translator.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.pss.translator.exception.SdsRetrievalException;
import uk.nhs.adaptors.pss.translator.sds.SdsRequestBuilder;

@ExtendWith(MockitoExtension.class)
public class SDSServiceTest {

    private static final String EHR_EXTRACT_MESSAGE_TYPE = "RCMR_IN030000UK06";
    private static final String TEST_ODS_CODE = "P83007";
    private static final String TEST_CONVERSATION_ID = "test-conversation-id";

    @Mock
    private FhirParser fhirParser;

    @Mock
    private SdsRequestBuilder sdsRequestBuilder;

    @Mock
    private SdsClientService sdsClientService;

    @InjectMocks
    private SDSService sdsService;

    @Test
    public void When_GetPersistDurationForEHRExtract_WithWebClientResponseException_Expect_ThrowsSdsRetrievalException() {
        when(sdsClientService.send(any())).thenThrow(new WebClientResponseException(
            HttpStatus.BAD_REQUEST.value(),
            "BAD REQUEST",
            new HttpHeaders(),
            new byte[] {},
            Charset.defaultCharset())
        );

        assertThrows(SdsRetrievalException.class, () -> sdsService.getPersistDurationFor(EHR_EXTRACT_MESSAGE_TYPE, TEST_ODS_CODE,
            TEST_CONVERSATION_ID));
    }
}
