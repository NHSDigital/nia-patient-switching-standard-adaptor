package uk.nhs.adaptors.pss.translator.task;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

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

@ExtendWith(MockitoExtension.class)
public class QueueMessageHandlerTest {
    @Mock
    private Message message;

    @Mock
    private SendEhrExtractRequestHandler sendEhrExtractRequestHandler;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private QueueMessageHandler queueMessageHandler;

    @Test
    public void handleMessageWhenSendEhrExtractRequestHandlerReturnsTrue() {
        prepareMocks(true);
        assertTrue(queueMessageHandler.handle(message));
    }

    @Test
    public void handleMessageWhenSendEhrExtractRequestHandlerReturnsFalse() {
        prepareMocks(false);
        assertFalse(queueMessageHandler.handle(message));
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
        var transferRequestMessage = TransferRequestMessage.builder().build();
        when(message.getBody(String.class)).thenReturn(messageBody);
        when(objectMapper.readValue(messageBody, TransferRequestMessage.class)).thenReturn(transferRequestMessage);
        when(sendEhrExtractRequestHandler.prepareAndSendRequest(transferRequestMessage)).thenReturn(prepareAndSendRequestResult);
    }
}
