package uk.nhs.adaptors.pss.translator.task;

import static java.util.UUID.randomUUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_ACCEPTED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_ACKNOWLEDGED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK;
import static uk.nhs.adaptors.connector.model.MigrationStatus.FINAL_ACK_SENT;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import lombok.SneakyThrows;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.model.MigrationStatusLog;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.service.XPathService;

@ExtendWith(MockitoExtension.class)
public class AcknowledgmentMessageHandlerTest {
    private static final String ACK_TYPE_CODE_XPATH = "//MCCI_IN010000UK13/acknowledgement/@typeCode";
    private static final String CONVERSATION_ID = randomUUID().toString();
    private static final String ACK_TYPE_CODE = "AA";
    private static final String NACK_TYPE_CODE = "AE";

    @Mock
    private XPathService xPathService;

    @Mock
    private MigrationStatusLogService migrationStatusLogService;

    @Mock
    private Document ebXmlDocument;
    @Mock
    private MigrationStatusLog statusLog;

    @InjectMocks
    private AcknowledgmentMessageHandler acknowledgmentMessageHandler;

    private  InboundMessage inboundMessage;

    @Test
    public void handleMessageWithAckTypeCode() throws SAXException {
        inboundMessage = new InboundMessage();
        prepareXPathServiceMocks(ACK_TYPE_CODE);
        prepareMigrationStatusMocks(EHR_EXTRACT_REQUEST_ACCEPTED);

        acknowledgmentMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(migrationStatusLogService).addMigrationStatusLog(EHR_EXTRACT_REQUEST_ACKNOWLEDGED, CONVERSATION_ID, null);
    }

    @Test
    public void handleMessageWithNackTypeCode() throws SAXException {
        inboundMessage = new InboundMessage();
        prepareXPathServiceMocks(NACK_TYPE_CODE);
        prepareMigrationStatusMocks(EHR_EXTRACT_REQUEST_ACCEPTED);

        acknowledgmentMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(migrationStatusLogService).addMigrationStatusLog(EHR_EXTRACT_REQUEST_NEGATIVE_ACK, CONVERSATION_ID, null);
    }

    @Test
    public void handleMessageWithUnknownTypeCode() throws SAXException {
        inboundMessage = new InboundMessage();
        prepareXPathServiceMocks("unknown type");
        prepareMigrationStatusMocks(EHR_EXTRACT_REQUEST_ACCEPTED);

        acknowledgmentMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(migrationStatusLogService, times(0)).addMigrationStatusLog(any(), any(), any());
    }

    @Test
    public void When_HandleMessage_With_NackTypeCodeAndFinalAckSent_Expect_MigrationStatusNotUpdated() throws SAXException {
        inboundMessage = new InboundMessage();
        prepareXPathServiceMocks("AE");
        prepareMigrationStatusMocks(FINAL_ACK_SENT);

        acknowledgmentMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(migrationStatusLogService, times(0)).addMigrationStatusLog(any(), any(), any());
    }

    @Test
    public void When_HandleMessage_With_AckTypeCodeAndFinalAckSent_Expect_MigrationStatusNotUpdated() throws SAXException {
        inboundMessage = new InboundMessage();
        prepareXPathServiceMocks("AA");
        prepareMigrationStatusMocks(FINAL_ACK_SENT);

        acknowledgmentMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(migrationStatusLogService, times(0)).addMigrationStatusLog(any(), any(), any());
    }

    @SneakyThrows
    private void prepareXPathServiceMocks(String typeCode) {
        inboundMessage.setPayload("payload");
        when(xPathService.parseDocumentFromXml(inboundMessage.getPayload())).thenReturn(ebXmlDocument);
        when(xPathService.getNodeValue(ebXmlDocument, ACK_TYPE_CODE_XPATH)).thenReturn(typeCode);
    }

    private void prepareMigrationStatusMocks(MigrationStatus latestMigrationStatus) {
        when(migrationStatusLogService.getLatestMigrationStatusLog(CONVERSATION_ID)).thenReturn(statusLog);
        when(statusLog.getMigrationStatus()).thenReturn(latestMigrationStatus);
    }
}
