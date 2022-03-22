package uk.nhs.adaptors.pss.translator.task;

import static java.util.UUID.randomUUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;

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
import uk.nhs.adaptors.pss.translator.model.NACKMessageData;
import uk.nhs.adaptors.pss.translator.service.MhsClientService;
import uk.nhs.adaptors.pss.translator.service.NACKMessageService;

@ExtendWith(MockitoExtension.class)
public class SendNACKMessageHandlerTest {

    private static final String TEST_MESSAGE_REF = randomUUID().toString();
    private static final String TEST_CONVERSATION_ID = randomUUID().toString();
    private static final String TEST_TO_ODS = "1234";
    private static final String TEST_TO_ASID = "5678";
    private static final String TEST_FROM_ASID = "98765";
    private static final String TEST_NACK_CODE = "30";

    @Mock
    private MhsClientService mhsClientService;

    @Mock
    private NACKMessageService messageService;

    @Mock
    private MhsRequestBuilder requestBuilder;

    @Mock
    private WebClient.RequestHeadersSpec request;

    @InjectMocks
    private SendNACKMessageHandler messageHandler;

    private NACKMessageData messageData;

    @BeforeEach
    public void setup() {
        messageData = NACKMessageData.builder()
            .messageRef(TEST_MESSAGE_REF)
            .conversationId(TEST_CONVERSATION_ID)
            .toOdsCode(TEST_TO_ODS)
            .toAsid(TEST_TO_ASID)
            .fromAsid(TEST_FROM_ASID)
            .nackCode(TEST_NACK_CODE)
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
                new byte[] {},
                Charset.defaultCharset())
        );

        assertFalse(messageHandler.prepareAndSendMessage(messageData));
    }
}
