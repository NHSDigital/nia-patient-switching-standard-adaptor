package uk.nhs.adaptors.pss.translator.service;


import org.hl7.v3.COPCIN000001UK01Message;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.nhs.adaptors.pss.translator.mhs.MhsRequestBuilder;
import uk.nhs.adaptors.pss.translator.model.ACKMessageData;
import uk.nhs.adaptors.pss.translator.task.SendACKMessageHandler;
import uk.nhs.adaptors.pss.translator.util.XmlParseUtilService;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(MockitoJUnitRunner.class)
public class NackAckPrepServiceTest {

    @Autowired
    private NackAckPreparationService nackAckPreparationService;


    private static final String CONVERSATION_ID = randomUUID().toString();

    @MockBean
    private MhsRequestBuilder requestBuilder;

    @MockBean
    private MhsClientService mhsClientService;

    @MockBean
    private ApplicationAcknowledgementMessageService messageService;

    @MockBean
    private IdGeneratorService idGeneratorService;

    @Mock
    private COPCIN000001UK01Message mockCOPCMessage;

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

        SendACKMessageHandler sendACKMessageHandler = mock(SendACKMessageHandler.class);
        given(sendACKMessageHandler.prepareAndSendMessage(any())).willAnswer(invocation -> { throw new Exception("abc msg"); });

        Exception exception = assertThrows(Exception.class, () -> {
            nackAckPreparationService.sendAckMessage(mockCOPCMessage, CONVERSATION_ID, "LoloPractice");
        });

        verify(sendACKMessageHandler, times(3))
                .prepareAndSendMessage(any(ACKMessageData.class));
    }

}
