package uk.nhs.adaptors.pss.translator.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EhrExtractRequestServiceTest {

    private static final String TEST_NHS_NUMBER = "TEST_NHS_NUMBER";
    private static final String TEST_FROM_ODS_CODE = "TEST_FROM_ODS";

    private EhrExtractRequestService ehrExtractRequestService;

    @BeforeEach
    public void setup() {
        ehrExtractRequestService = new EhrExtractRequestService();
    }

    @Test
    public void whenBuildEhrExtractRequestThenTemplateIsFilled() throws IOException {
        final var ehrExtractRequest = ehrExtractRequestService.buildEhrExtractRequest(TEST_NHS_NUMBER, TEST_FROM_ODS_CODE);
        assertTrue(ehrExtractRequest.contains(TEST_NHS_NUMBER) && ehrExtractRequest.contains(TEST_FROM_ODS_CODE));
    }

}
