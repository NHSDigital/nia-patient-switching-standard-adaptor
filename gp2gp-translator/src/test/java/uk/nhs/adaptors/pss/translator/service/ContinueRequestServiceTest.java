package uk.nhs.adaptors.pss.translator.service;

import com.github.mustachejava.Mustache;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.common.util.DateUtils;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import static org.mockito.ArgumentMatchers.any;

import java.io.IOException;
import java.time.Instant;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;
import static uk.nhs.adaptors.pss.translator.util.template.TemplateUtil.loadTemplate;

@ExtendWith(MockitoExtension.class)
public class ContinueRequestServiceTest
{
    private static final String NHS_NUMBER = "9446363101";
    private static final String CONVERSATION_ID = "6E242658-3D8E-11E3-A7DC-172BDA00FA67";
    private static final String LOOSING_ODS_CODE = "B83002"; //to odds code
    private static final String WINNING_ODS_CODE = "C81007"; //from odds code
    private static final String TO_ASID = "715373337545";
    private static final String FROM_ASID = "276827251543";
    private static final String MCCI_IN010000UK13creationTime = "20220407194614";

    private static final Mustache CONTINUE_REQUEST_FILE = loadTemplate("sendContinueRequest.mustache");

    @Mock
    private DateUtils dateUtils;

    @Mock
    private IdGeneratorService idGeneratorService;

    @InjectMocks
    private ContinueRequestService continueRequestService;

    //test parameters
    @Test
    public void buildContinueRequest_WhenParametersCorrect_ExpectToBuildTemplate () throws IOException {
        prepareMocks();

        String continueMessageString = continueRequestService.buildContinueRequest(
                CONVERSATION_ID,
                NHS_NUMBER,
                FROM_ASID,
                TO_ASID,
                LOOSING_ODS_CODE,
                WINNING_ODS_CODE,
                MCCI_IN010000UK13creationTime
        );

        assertEquals(readLargeInboundMessagePayloadFromFile(), continueMessageString);
    }

    @SneakyThrows
    private void prepareMocks() {
        when(idGeneratorService.generateUuid()).thenReturn("C258107F-7A3F-4FB5-8B36-D7C0F6496A17");

        when(dateUtils.getCurrentInstant()).thenReturn(Instant.now());
        when(DateFormatUtil.toHl7Format(Instant.now())).thenReturn("20131025165328");
    }

    @SneakyThrows
    private String readLargeInboundMessagePayloadFromFile() {
        return readResourceAsString("/xml/RCMRIN030000UK06_LARGE_MSG/payload.xml");
    }

    @SneakyThrows
    private String readLargeInboundMessageEbXmlFromFile() {
        return readResourceAsString("/xml/RCMRIN030000UK06_LARGE_MSG/ebxml.xml");
    }
}
