package uk.nhs.adaptors.pss.translator.service;


import org.hl7.v3.COPCIN000001UK01Message;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.nhs.adaptors.pss.translator.mhs.MhsRequestBuilder;
import uk.nhs.adaptors.pss.translator.model.ACKMessageData;
import uk.nhs.adaptors.pss.translator.model.NACKMessageData;
import uk.nhs.adaptors.pss.translator.task.SendACKMessageHandler;
import uk.nhs.adaptors.pss.translator.task.SendNACKMessageHandler;
import uk.nhs.adaptors.pss.translator.util.XmlParseUtilService;

import java.sql.SQLException;
import javax.xml.bind.JAXBException;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class NackAckPreparationServiceTest {

    @Autowired
    private NackAckPreparationService nackAckPreparationService;


    private static final String CONVERSATION_ID = randomUUID().toString();

    //@Mock
    //private SendACKMessageHandler sendACKMessageHandler;

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

        //doThrow(new Exception()).when(sendACKMessageHandler).prepareAndSendMessage(any());
        //when(sendACKMessageHandler.prepareAndSendMessage(any())).thenThrow(new RuntimeException("msg"));

        SendACKMessageHandler sendACKMessageHandler = spy(SendACKMessageHandler.class);
        when(sendACKMessageHandler.prepareAndSendMessage(any())).thenThrow(new SQLException("SQLException"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            nackAckPreparationService.sendAckMessage(mockCOPCMessage, CONVERSATION_ID, "LoloPractice");
        });

        verify(sendACKMessageHandler, times(3))
                .prepareAndSendMessage(any(ACKMessageData.class));
    }

}
