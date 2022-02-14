package uk.nhs.adaptors.pss.translator.task;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.pss.translator.util.FileUtils.readFile;

import javax.jms.JMSException;
import javax.jms.Message;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.amqp.JmsReader;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.service.BundleMapperService;

@ExtendWith(MockitoExtension.class)
public class MhsQueueMessageHandlerTest {
    private static final String NHS_NUMBER = "123456";
    private static final String INBOUND_MESSAGE_STRING = "{hi i'm inbound message}";

    @Mock
    private PatientMigrationRequestDao patientMigrationRequestDao;

    @Mock
    private MigrationStatusLogService migrationStatusLogService;

    @Mock
    private FhirParser fhirParser;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private BundleMapperService bundleMapperService;

    @Mock
    private JmsReader jmsReader;

    @Mock
    private Message message;

    @InjectMocks
    private MhsQueueMessageHandler mhsQueueMessageHandler;

    @Test
    public void handleMessageWithoutErrorsShouldReturnTrue() throws JMSException, JsonProcessingException {
        Bundle bundle = new Bundle();
        bundle.setId("Test");
        String bundleString = "{bundle}";
        InboundMessage inboundMessage = new InboundMessage();
        inboundMessage.setPayload(readInboundMessagePayloadFromFile());
        when(jmsReader.readMessage(message)).thenReturn(INBOUND_MESSAGE_STRING);
        when(objectMapper.readValue(INBOUND_MESSAGE_STRING, InboundMessage.class)).thenReturn(inboundMessage);
        when(bundleMapperService.mapToBundle(any(RCMRIN030000UK06Message.class))).thenReturn(bundle);
        when(fhirParser.encodeToJson(bundle)).thenReturn(bundleString);
        when(objectMapper.writeValueAsString(inboundMessage)).thenReturn(INBOUND_MESSAGE_STRING);

        boolean result = mhsQueueMessageHandler.handleMessage(message);

        assertTrue(result);
        verify(migrationStatusLogService).addMigrationStatusLog(EHR_EXTRACT_RECEIVED, NHS_NUMBER);
        verify(patientMigrationRequestDao).saveBundleAndInboundMessageData(NHS_NUMBER, bundleString, INBOUND_MESSAGE_STRING);
        verify(migrationStatusLogService).addMigrationStatusLog(EHR_EXTRACT_TRANSLATED, NHS_NUMBER);
    }

    @Test
    public void handleMessageWhenObjectMapperThrowsErrorShouldReturnFalse() throws JMSException, JsonProcessingException {
        when(jmsReader.readMessage(message)).thenReturn(INBOUND_MESSAGE_STRING);
        when(objectMapper.readValue(INBOUND_MESSAGE_STRING, InboundMessage.class)).thenThrow(new JsonMappingException("hello"));

        boolean result = mhsQueueMessageHandler.handleMessage(message);

        assertFalse(result);
        verifyNoInteractions(migrationStatusLogService);
        verifyNoInteractions(patientMigrationRequestDao);
    }

    @Test
    public void handleMessageWhenJmsReaderThrowsErrorShouldReturnFalse() throws JMSException {
        when(jmsReader.readMessage(message)).thenThrow(new JMSException("Nobody expects the spanish inquisition!"));

        boolean result = mhsQueueMessageHandler.handleMessage(message);

        assertFalse(result);
        verifyNoInteractions(migrationStatusLogService);
        verifyNoInteractions(patientMigrationRequestDao);
    }

    @SneakyThrows
    private String readInboundMessagePayloadFromFile() {
        return readFile("/xml/inbound_message_payload.xml").replace("{{nhsNumber}}", NHS_NUMBER);
    }
}
