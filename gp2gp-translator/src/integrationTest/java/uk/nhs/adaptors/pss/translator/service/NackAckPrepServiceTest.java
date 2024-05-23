package uk.nhs.adaptors.pss.translator.service;


import org.hl7.v3.COPCIN000001UK01Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.MhsRequestBuilder;
import uk.nhs.adaptors.pss.translator.model.ACKMessageData;
import uk.nhs.adaptors.pss.translator.task.SendACKMessageHandler;
import uk.nhs.adaptors.pss.translator.util.XmlParseUtilService;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@DirtiesContext
public class NackAckPrepServiceTest {

    @MockBean
    private SendACKMessageHandler sendACKMessageHandler;

    @Autowired
    private NackAckPreparationService nackAckPreparationService;

    private static final String CONVERSATION_ID = randomUUID().toString();

    @MockBean
    private MigrationStatusLogService migrationStatusLogService;

    @MockBean
    private MhsRequestBuilder requestBuilder;

    @MockBean
    private MhsClientService mhsClientService;

    @MockBean
    private ApplicationAcknowledgementMessageService messageService;

    @Mock
    private COPCIN000001UK01Message mockCOPCMessage;

    private static final int NUMBER_OF_RETRIES = 3;

    @Test
    public void When_SendAckMessageRCMR_WithNoErrors_Expect_SomeRetries() {
        MockedStatic<XmlParseUtilService> mockedXmlParseUtilService = Mockito.mockStatic(XmlParseUtilService.class);

        mockedXmlParseUtilService.when(
            () -> XmlParseUtilService.parseMessageRef(any(COPCIN000001UK01Message.class))
                                      ).thenReturn("Ref");

        mockedXmlParseUtilService.when(
            () -> XmlParseUtilService.parseToAsid(any(COPCIN000001UK01Message.class))
                                      ).thenReturn("Asid");

        mockedXmlParseUtilService.when(
            () -> XmlParseUtilService.parseFromAsid(any(COPCIN000001UK01Message.class))
                                      ).thenReturn("FromAsid");

        doThrow(new RuntimeException("")).when(sendACKMessageHandler).prepareAndSendMessage(any());

        Exception exception = assertThrows(Exception.class, () -> {
            nackAckPreparationService.sendAckMessage(mockCOPCMessage, CONVERSATION_ID, "ColoColoPractice");
        });

        verify(sendACKMessageHandler, times(NUMBER_OF_RETRIES))
                .prepareAndSendMessage(any(ACKMessageData.class));
    }

}
