package uk.nhs.adaptors.pss.translator.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.nhs.adaptors.pss.translator.mhs.MhsRequestBuilder;
import uk.nhs.adaptors.pss.translator.mhs.model.OutboundMessage;
import uk.nhs.adaptors.pss.translator.model.ACKMessageData;
import uk.nhs.adaptors.pss.translator.service.ACKMessageService;
import uk.nhs.adaptors.pss.translator.service.MhsClientService;

import java.nio.charset.Charset;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
public class SendACKMessageHandlerTest {

    private static final String TEST_MESSAGE_REF = randomUUID().toString();
    private static final String TEST_CONVERSATION_ID = randomUUID().toString();
    private static final String TEST_TO_ODS = "1234";
    private static final String TEST_TO_ASID = "5678";
    private static final String TEST_FROM_ASID = "98765";
    private static final String TEST_ACK_TYPE = "AA";

    @Mock
    private MhsClientService mhsClientService;

    @Mock
    private ACKMessageService messageService;

    @Mock
    private MhsRequestBuilder requestBuilder;

    @Mock
    private WebClient.RequestHeadersSpec request;

    @InjectMocks
    private SendACKMessageHandler messageHandler;

    private ACKMessageData messageData;

    @BeforeEach
    public void setup() {
        messageData = ACKMessageData.builder()
                .messageRef(TEST_MESSAGE_REF)
                .conversationId(TEST_CONVERSATION_ID)
                .toOdsCode(TEST_TO_ODS)
                .toAsid(TEST_TO_ASID)
                .fromAsid(TEST_FROM_ASID)
                .ackType(TEST_ACK_TYPE)
                .build();

        when(requestBuilder.buildSendACKRequest(eq(TEST_CONVERSATION_ID), eq(TEST_TO_ODS), any(OutboundMessage.class)))
                .thenReturn(request);
    }

    @Test
    public void whenSendMessageSuccessThenTrueIsReturned() {
        assertTrue(messageHandler.prepareAndSendMessage(messageData));
    }

    @Test
    public void whenSendMessageFailsThenFalseIsReturned() {
        when(mhsClientService.send(request)).thenThrow(
                new WebClientResponseException(
                        HttpStatus.BAD_REQUEST.value(),
                        "BAD REQUEST",
                        new HttpHeaders(),
                        new byte[]{},
                        Charset.defaultCharset())
        );

        assertFalse(messageHandler.prepareAndSendMessage(messageData));
    }

}
