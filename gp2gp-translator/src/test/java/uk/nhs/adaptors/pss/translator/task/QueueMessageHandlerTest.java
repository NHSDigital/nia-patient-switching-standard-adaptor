package uk.nhs.adaptors.pss.translator.task;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import uk.nhs.adaptors.common.model.TransferRequestMessage;
import uk.nhs.adaptors.common.service.MDCService;

@ExtendWith(MockitoExtension.class)
public class QueueMessageHandlerTest {

    private static final String CONVERSATION_ID = UUID.randomUUID().toString();

    @Mock
    private Message message;

    @Mock
    private SendEhrExtractRequestHandler sendEhrExtractRequestHandler;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MDCService mdcService;

    @InjectMocks
    private QueueMessageHandler queueMessageHandler;

    @Test
    public void handleMessageWhenSendEhrExtractRequestHandlerReturnsTrue() {
        prepareMocks(true);

        boolean messageAcknowledged = queueMessageHandler.handle(message);

        verify(mdcService).applyConversationId(CONVERSATION_ID);
        assertTrue(messageAcknowledged);
    }

    @Test
    public void handleMessageWhenSendEhrExtractRequestHandlerReturnsFalse() {
        prepareMocks(false);

        boolean messageAcknowledged = queueMessageHandler.handle(message);

        verify(mdcService).applyConversationId(CONVERSATION_ID);
        assertFalse(messageAcknowledged);
    }

    @Test
    @SneakyThrows
    public void handleMessageWhenJMSExceptionIsThrown() {
        when(message.getBody(String.class)).thenThrow(new JMSException("not good"));
        assertFalse(queueMessageHandler.handle(message));
    }

    @SneakyThrows
    private void prepareMocks(boolean prepareAndSendRequestResult) {
        var messageBody = "MESSAGE_BODY";
        var transferRequestMessage = TransferRequestMessage.builder()
            .conversationId(CONVERSATION_ID)
            .build();
        when(message.getBody(String.class)).thenReturn(messageBody);
        when(objectMapper.readValue(messageBody, TransferRequestMessage.class)).thenReturn(transferRequestMessage);
        when(sendEhrExtractRequestHandler.prepareAndSendRequest(transferRequestMessage)).thenReturn(prepareAndSendRequestResult);
    }
}
