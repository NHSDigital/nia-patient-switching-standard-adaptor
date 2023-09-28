package uk.nhs.adaptors.pss.translator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.common.util.DateUtils;
import uk.nhs.adaptors.pss.translator.model.ContinueRequestData;

@ExtendWith(MockitoExtension.class)
public class ContinueRequestServiceTest {
    private static final String NHS_NUMBER = "9446363101";
    private static final String LOSING_ODS_CODE = "C81007";
    private static final String WINNING_ODS_CODE = "B83002";
    private static final String TO_ASID = "715373337545";
    private static final String FROM_ASID = "276827251543";
    private static final String MCCI_IN010000UK13_CREATIONTIME = "20220407194614";
    private static final String TEST_EHR_EXTRACT_ID = "TEST-EHR-EXTRACT-ID";
    private static final String MESSAGE_ID = "c258107f-7a3f-4fb5-8b36-d7c0f6496a17";

    @Mock
    private DateUtils dateUtils;

    @InjectMocks
    private ContinueRequestService continueRequestService;

    @Test
    public void When_BuildContinueRequest_IsCalledWithCorrectParams_Expect_ToBuildTemplateMatchingMatchingTestTemplate()
            throws IOException {
        prepareMocks();

        var continueMessageData = ContinueRequestData.builder()
            .nhsNumber(NHS_NUMBER)
            .fromAsid(FROM_ASID)
            .toAsid(TO_ASID)
            .toOdsCode(LOSING_ODS_CODE)
            .fromOdsCode(WINNING_ODS_CODE)
            .mcciIN010000UK13creationTime(MCCI_IN010000UK13_CREATIONTIME)
            .ehrExtractId(TEST_EHR_EXTRACT_ID)
            .build();

        String expected = readLargeInboundMessagePayloadFromFile();
        String actual = continueRequestService.buildContinueRequest(continueMessageData, MESSAGE_ID);

        assertEquals(expected, actual);
    }

    @SneakyThrows
    private void prepareMocks() {

        final int YEAR = 1980;
        final int MONTH = 4;
        final int DAYOFMONTH = 9;
        final int HOUR = 20;
        final int MINUTE = 15;
        final int SECOND = 45;
        final int NANOSECOND = 345875000;
        OffsetDateTime offsetDT4 = OffsetDateTime.of(
                YEAR,
                MONTH,
                DAYOFMONTH,
                HOUR,
                MINUTE,
                SECOND,
                NANOSECOND,
                ZoneOffset.of("+07:00"));

        when(dateUtils.getCurrentInstant()).thenReturn(
                offsetDT4.toInstant()
        );
    }

    @SneakyThrows
    private String readLargeInboundMessagePayloadFromFile() {
        return readResourceAsString("/xml/COPC_IN000001UK01_CONTINUE/payload.xml");
    }
}