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
import uk.nhs.adaptors.pss.translator.service.ApplicationAcknowledgementMessageService;
import uk.nhs.adaptors.pss.translator.service.MhsClientService;

import java.nio.charset.Charset;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SendACKMessageHandlerTest {

    private static final String TEST_MESSAGE_REF = randomUUID().toString();
    private static final String TEST_CONVERSATION_ID = randomUUID().toString();
    private static final String TEST_TO_ODS = "1234";
    private static final String TEST_TO_ASID = "5678";
    private static final String TEST_FROM_ASID = "98765";

    @Mock
    private MhsClientService mhsClientService;

    @Mock
    private ApplicationAcknowledgementMessageService messageService;

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
            .build();

        when(requestBuilder.buildSendACKRequest(eq(TEST_CONVERSATION_ID), eq(TEST_TO_ODS), any(OutboundMessage.class)))
            .thenReturn(request);
    }

    @Test
    public void When_SendMessage_WithSuccess_Expect_TrueIsReturned() {
        assertTrue(messageHandler.prepareAndSendMessage(messageData));
    }

    @Test
    public void When_SendMessage_WithFails_Expect_FalseIsReturned() {
        when(mhsClientService.send(request)).thenThrow(
            new WebClientResponseException(
                HttpStatus.BAD_REQUEST.value(),
                "BAD REQUEST",
                new HttpHeaders(),
                new byte[] {},
                Charset.defaultCharset())
        );

        assertFalse(messageHandler.prepareAndSendMessage(messageData));
    }
}
