package uk.nhs.adaptors.pss.translator.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.nhs.adaptors.common.model.TransferRequestMessage;
import uk.nhs.adaptors.common.util.DateUtils;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@ExtendWith(MockitoExtension.class)
public class EhrExtractRequestServiceTest {
    private static final String TEST_NHS_NUMBER = "TEST_NHS_NUMBER";
    private static final String TEST_FROM_ODS_CODE = "TEST_FROM_ODS";
    private static final String TEST_TO_ODS_CODE = "TEST_TO_ODS";
    private static final String TEST_FROM_ASID = "TEST_FROM_ASID";
    private static final String TEST_TO_ASID = "TEST_TO_ASID";
    private static final String MESSAGE_ID = "123-ABC";
    private static final String EHR_REQUEST_ID = "657-ABC";

    @Mock
    private DateUtils dateUtils;

    @Mock
    private IdGeneratorService idGeneratorService;

    @InjectMocks
    private EhrExtractRequestService ehrExtractRequestService;

    @Test
    public void whenBuildEhrExtractRequestThenTemplateIsFilled() throws IOException {
        var instant = Instant.now();
        when(dateUtils.getCurrentInstant()).thenReturn(instant);
        when(idGeneratorService.generateUuid()).thenReturn(MESSAGE_ID, EHR_REQUEST_ID);
        var message = TransferRequestMessage.builder()
            .patientNhsNumber(TEST_NHS_NUMBER)
            .fromAsid(TEST_FROM_ASID)
            .toAsid(TEST_TO_ASID)
            .fromOds(TEST_FROM_ODS_CODE)
            .toOds(TEST_TO_ODS_CODE)
            .build();

        final var ehrExtractRequest = ehrExtractRequestService.buildEhrExtractRequest(message);
        assertTrue(ehrExtractRequest.contains(TEST_NHS_NUMBER));
        assertTrue(ehrExtractRequest.contains(TEST_FROM_ODS_CODE));
        assertTrue(ehrExtractRequest.contains(TEST_TO_ODS_CODE));
        assertTrue(ehrExtractRequest.contains(TEST_FROM_ASID));
        assertTrue(ehrExtractRequest.contains(TEST_TO_ASID));
        assertTrue(ehrExtractRequest.contains(MESSAGE_ID.toLowerCase()));
        assertTrue(ehrExtractRequest.contains(EHR_REQUEST_ID.toLowerCase()));
        assertTrue(ehrExtractRequest.contains(DateFormatUtil.toHl7Format(instant)));
    }
}
