package uk.nhs.adaptors.pss.translator.task;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.common.testutil.CreateParametersUtil.createValidParametersResource;

import javax.jms.JMSException;
import javax.jms.Message;

import org.hl7.fhir.dstu3.model.Parameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.common.util.FhirParser;

@ExtendWith(MockitoExtension.class)
public class QueueMessageHandlerTest {
    private static final String TEST_NHS_NUMBER = "123456";

    @Mock
    private Message message;

    @Mock
    private FhirParser fhirParser;

    @Mock
    private SendEhrExtractRequestHandler sendEhrExtractRequestHandler;

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
        when(message.getBody(String.class)).thenReturn("MESSAGE_BODY");
        when(fhirParser.parseResource(message.getBody(String.class), Parameters.class))
            .thenReturn(createValidParametersResource(TEST_NHS_NUMBER));
        when(sendEhrExtractRequestHandler.prepareAndSendRequest(TEST_NHS_NUMBER)).thenReturn(prepareAndSendRequestResult);
    }
}
