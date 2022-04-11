package uk.nhs.adaptors.pss.translator.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.common.util.DateUtils;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;

@ExtendWith(MockitoExtension.class)
public class ContinueRequestServiceTest {
    private static final String NHS_NUMBER = "9446363101";
    private static final String CONVERSATION_ID = "6E242658-3D8E-11E3-A7DC-172BDA00FA67";
    private static final String LOOSING_ODS_CODE = "B83002"; //to odds code
    private static final String WINNING_ODS_CODE = "C81007"; //from odds code
    private static final String TO_ASID = "715373337545";
    private static final String FROM_ASID = "276827251543";
    private static final String MCCI_IN010000UK13_CREATIONTIME = "20220407194614";

    @Mock
    private DateUtils dateUtils;

    @Mock
    private IdGeneratorService idGeneratorService;

    @InjectMocks
    private ContinueRequestService continueRequestService;

    @Test
    public void When_BuildContinueRequest_IsCalledWithCorrectParams_Expect_ToBuildTemplateMatchingMatchingTestTemplate()
            throws IOException {
        prepareMocks();

        String expected = readLargeInboundMessagePayloadFromFile();
        String actual = continueRequestService.buildContinueRequest(
                CONVERSATION_ID,
                NHS_NUMBER,
                FROM_ASID,
                TO_ASID,
                LOOSING_ODS_CODE,
                WINNING_ODS_CODE,
                MCCI_IN010000UK13_CREATIONTIME
        );

        assertEquals(expected, actual);
    }

    @SneakyThrows
    private void prepareMocks() {
        when(idGeneratorService.generateUuid()).thenReturn("C258107F-7A3F-4FB5-8B36-D7C0F6496A17");

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