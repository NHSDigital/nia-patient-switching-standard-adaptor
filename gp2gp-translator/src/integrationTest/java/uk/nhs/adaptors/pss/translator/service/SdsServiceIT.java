package uk.nhs.adaptors.pss.translator.service;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.nhs.adaptors.pss.translator.exception.SdsRetrievalException;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith({SpringExtension.class})
@AutoConfigureMockMvc
public class SdsServiceIT {

    private static final String EHR_EXTRACT_MESSAGE_TYPE = "RCMR_IN030000UK06";
    private static final String COPC_MESSAGE_TYPE = "COPC_IN000001UK01";
    private static final String TEST_INVALID_MESSAGE_TYPE = "invalid_type";
    private static final String TEST_ODS_CODE = "P83007";
    private static final String TEST_INVALID_ODS_CODE = "Z12345";
    private static final String TEST_CONVERSATION_ID = "6d3d3674-7ce5-11ec-90d6-0242ac120003";
    private static final String INVALID_CONVERSATION_ID = "invalid id";
    private static final int EHR_DURATION_HOURS = 4;
    private static final int EHR_DURATION_MINUTES = 10;
    private static final int COPC_DURATION_HOURS = 7;
    private static final Duration EHR_DURATION = Duration.ofHours(EHR_DURATION_HOURS).plusMinutes(EHR_DURATION_MINUTES);
    private static final Duration COPC_DURATION = Duration.ofHours(COPC_DURATION_HOURS);

    private static String sdsResponseEHRExtract;
    private static String sdsResponseCopcMessage;
    private static String sdsResponseInvalidMessageType;
    private static String sdsResponseInvalidOds;
    private static String sdsResponseInvalidConversationId;

    @Autowired
    private SDSService sdsService;

    @MockBean
    private SdsClientService sdsClientService;

    @BeforeAll
    public static void setup() throws IOException {
        sdsResponseEHRExtract = readFileAsString("json/SDSResponseEHRExtract.json");
        sdsResponseCopcMessage = readFileAsString("json/SDSResponseCopcMessage.json");
        sdsResponseInvalidMessageType = readFileAsString("json/SDSResponseInvalidMessageType.json");
        sdsResponseInvalidOds = readFileAsString("json/SDSResponseInvalidODS.json");
        sdsResponseInvalidConversationId = readFileAsString("json/SDSResponseInvalidConversationId.json");
    }

    private static String readFileAsString(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return Files.readString(Paths.get(resource.getURI()));
    }

    @Test
    public void When_GetPersistDurationFor_WhenEHRExtractValidInput_Expect_CorrectDuration() {
        when(sdsClientService.send(any())).thenReturn(sdsResponseEHRExtract);

        Duration parsedDuration = sdsService.getPersistDurationFor(EHR_EXTRACT_MESSAGE_TYPE, TEST_ODS_CODE, TEST_CONVERSATION_ID);

        assertEquals(EHR_DURATION, parsedDuration);
    }

    @Test
    public void When_GetPersistDurationFor_WhenCOPCValidInput_Expect_CorrectDuration() {
        when(sdsClientService.send(any())).thenReturn(sdsResponseCopcMessage);

        Duration parsedDuration = sdsService.getPersistDurationFor(COPC_MESSAGE_TYPE, TEST_ODS_CODE, TEST_CONVERSATION_ID);

        assertEquals(COPC_DURATION, parsedDuration);
    }

    @Test
    public void When_GetPersistDurationFor_WhenODSIsInValid_Expect_SdsRetrievalException() {
        when(sdsClientService.send(any())).thenReturn(sdsResponseInvalidMessageType);

        assertThrows(SdsRetrievalException.class,
            () -> sdsService.getPersistDurationFor(COPC_MESSAGE_TYPE, TEST_INVALID_ODS_CODE, TEST_CONVERSATION_ID));
    }

    @Test
    public void When_GetPersistDurationFor_WhenMessageTypeIsInvalid_Expect_SdsRetrievalException() {
        when(sdsClientService.send(any())).thenReturn(sdsResponseInvalidOds);

        assertThrows(SdsRetrievalException.class,
            () -> sdsService.getPersistDurationFor(TEST_INVALID_MESSAGE_TYPE, TEST_ODS_CODE, TEST_CONVERSATION_ID));
    }

    @Test
    public void When_GetPersistDurationFor_WhenConversationIdInvalid_Expect_SdsRetrievalException() {
        when(sdsClientService.send(any())).thenReturn(sdsResponseInvalidConversationId);

        assertThrows(SdsRetrievalException.class,
            () -> sdsService.getPersistDurationFor(TEST_INVALID_MESSAGE_TYPE, TEST_ODS_CODE, INVALID_CONVERSATION_ID));

    }
}
