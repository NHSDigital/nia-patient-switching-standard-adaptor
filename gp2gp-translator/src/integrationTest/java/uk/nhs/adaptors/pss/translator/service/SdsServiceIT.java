package uk.nhs.adaptors.pss.translator.service;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.nhs.adaptors.pss.translator.exception.SdsRetrievalException;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith({SpringExtension.class})
@DirtiesContext
@AutoConfigureMockMvc
public class SdsServiceIT {

    private static final String EHR_EXTRACT_MESSAGE_TYPE = "RCMR_IN030000UK06";
    private static final String COPC_MESSAGE_TYPE = "COPC_IN000001UK01";
    private static final String TEST_INVALID_MESSAGE_TYPE = "invalid_type";
    private static final String TEST_ODS_CODE = "P83007";
    private static final String TEST_INVALID_ODS_CODE = "Z12345";
    private static final String TEST_CONVERSATION_ID = "6d3d3674-7ce5-11ec-90d6-0242ac120003";
    private static final Duration EHR_DURATION = Duration.ofHours(4).plusMinutes(10);
    private static final Duration COPC_DURATION = Duration.ofHours(7);

    @Autowired
    private SDSService sdsService;

    @Test
    public void When_getPersistDurationFor_WhenEHRExtractValidInput_Expect_CorrectDuration() {
        Duration parsedDuration = sdsService.getPersistDurationFor(EHR_EXTRACT_MESSAGE_TYPE, TEST_ODS_CODE, TEST_CONVERSATION_ID);

        assertEquals(EHR_DURATION, parsedDuration);
    }

    @Test
    public void When_getPersistDurationFor_WhenCOPCValidInput_Expect_CorrectDuration() {
        Duration parsedDuration = sdsService.getPersistDurationFor(COPC_MESSAGE_TYPE,TEST_ODS_CODE, TEST_CONVERSATION_ID);

        assertEquals(COPC_DURATION, parsedDuration);
    }

    @Test
    public void When_getPersistDurationFor_WhenODSIsInValid_Expect_SdsRetrievalException() {
        assertThrows(SdsRetrievalException.class,
            () -> sdsService.getPersistDurationFor(COPC_MESSAGE_TYPE, TEST_INVALID_ODS_CODE, TEST_CONVERSATION_ID));
    }

    @Test
    public void When_getPersistDurationFor_WhenMessageTypeIsInvalid_Expect_SdsRetrievalException() {
        assertThrows(SdsRetrievalException.class,
            () -> sdsService.getPersistDurationFor(TEST_INVALID_MESSAGE_TYPE, TEST_ODS_CODE, TEST_CONVERSATION_ID));
    }


}
