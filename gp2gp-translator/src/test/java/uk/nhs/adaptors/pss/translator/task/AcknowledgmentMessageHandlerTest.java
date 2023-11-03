package uk.nhs.adaptors.pss.translator.task;

import static java.util.UUID.randomUUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_REQUEST_ACCEPTED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_REQUEST_ACKNOWLEDGED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_PATIENT_NOT_REGISTERED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK_UNKNOWN;
import static uk.nhs.adaptors.common.enums.MigrationStatus.FINAL_ACK_SENT;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import lombok.SneakyThrows;
import uk.nhs.adaptors.common.enums.MigrationStatus;
import uk.nhs.adaptors.connector.model.MigrationStatusLog;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.service.FailedProcessHandlingService;
import uk.nhs.adaptors.pss.translator.service.XPathService;

@ExtendWith(MockitoExtension.class)
public class AcknowledgmentMessageHandlerTest {
    private static final String ACK_TYPE_CODE_XPATH = "//MCCI_IN010000UK13/acknowledgement/@typeCode";
    private static final String ERROR_REASON_CODE_XPATH =
        "//MCCI_IN010000UK13/ControlActEvent/reason/justifyingDetectedIssueEvent/code/@code";

    private static final String CONVERSATION_ID = randomUUID().toString();
    private static final String ACK_TYPE_CODE = "AA";
    private static final String NACK_ERROR_TYPE_CODE = "AE";
    private static final String NACK_REJECT_TYPE_CODE = "AR";

    @Mock
    private XPathService xPathService;

    @Mock
    private MigrationStatusLogService migrationStatusLogService;

    @Mock
    private Document ebXmlDocument;
    @Mock
    private MigrationStatusLog statusLog;

    @Mock
    private FailedProcessHandlingService failedProcessHandlingService;

    @InjectMocks
    private AcknowledgmentMessageHandler acknowledgmentMessageHandler;

    private  InboundMessage inboundMessage;

    @Test
    public void handleMessageWithAckTypeCode() throws SAXException {
        inboundMessage = new InboundMessage();
        prepareXPathServiceMocks(ACK_TYPE_CODE, null);
        prepareMigrationStatusMocks(EHR_EXTRACT_REQUEST_ACCEPTED);

        acknowledgmentMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(migrationStatusLogService)
            .addMigrationStatusLog(EHR_EXTRACT_REQUEST_ACKNOWLEDGED, CONVERSATION_ID, null, null);
    }

    @Test
    public void handleMessageWithNackErrorTypeCode() throws SAXException {
        inboundMessage = new InboundMessage();
        prepareXPathServiceMocks(NACK_ERROR_TYPE_CODE, null);
        prepareMigrationStatusMocks(EHR_EXTRACT_REQUEST_ACCEPTED);

        acknowledgmentMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(migrationStatusLogService).addMigrationStatusLog(EHR_EXTRACT_REQUEST_NEGATIVE_ACK_UNKNOWN, CONVERSATION_ID, null, "");
    }

    @Test
    public void handleMessageWithNackErrorTypeCodeAndErrorCode() throws SAXException {

        inboundMessage = new InboundMessage();
        prepareXPathServiceMocks(NACK_ERROR_TYPE_CODE, "06");
        prepareMigrationStatusMocks(EHR_EXTRACT_REQUEST_ACCEPTED);

        acknowledgmentMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(migrationStatusLogService)
            .addMigrationStatusLog(EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_PATIENT_NOT_REGISTERED, CONVERSATION_ID, null, "06");
    }

    @Test
    public void handleMessageWithNackRejectTypeCode() throws SAXException {
        inboundMessage = new InboundMessage();
        prepareXPathServiceMocks(NACK_REJECT_TYPE_CODE, null);
        prepareMigrationStatusMocks(EHR_EXTRACT_REQUEST_ACCEPTED);

        acknowledgmentMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(migrationStatusLogService).addMigrationStatusLog(EHR_EXTRACT_REQUEST_NEGATIVE_ACK_UNKNOWN, CONVERSATION_ID, null,"");
    }

    @Test
    public void handleMessageWithNackRejectTypeCodeAndErrorCode() throws SAXException {
        inboundMessage = new InboundMessage();
        prepareXPathServiceMocks(NACK_REJECT_TYPE_CODE, "06");
        prepareMigrationStatusMocks(EHR_EXTRACT_REQUEST_ACCEPTED);

        acknowledgmentMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(migrationStatusLogService)
            .addMigrationStatusLog(EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_PATIENT_NOT_REGISTERED, CONVERSATION_ID, null, "06");
    }

    @Test
    public void handleMessageWithUnknownTypeCode() throws SAXException {
        inboundMessage = new InboundMessage();
        prepareXPathServiceMocks("unknown type", null);
        prepareMigrationStatusMocks(EHR_EXTRACT_REQUEST_ACCEPTED);

        acknowledgmentMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(migrationStatusLogService, times(0)).addMigrationStatusLog(any(), any(), any(), anyString());
    }

    @Test
    public void When_HandleMessage_With_NackErrorTypeCodeAndFinalAckSent_Expect_MigrationStatusNotUpdated() throws SAXException {
        inboundMessage = new InboundMessage();
        prepareXPathServiceMocks(NACK_ERROR_TYPE_CODE, null);
        prepareMigrationStatusMocks(FINAL_ACK_SENT);

        acknowledgmentMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(migrationStatusLogService, times(0)).addMigrationStatusLog(any(), any(), any(), anyString());
    }

    @Test
    public void When_HandleMessage_With_NackRejectTypeCodeAndFinalAckSent_Expect_MigrationStatusNotUpdated() throws SAXException {
        inboundMessage = new InboundMessage();
        prepareXPathServiceMocks(NACK_REJECT_TYPE_CODE, null);
        prepareMigrationStatusMocks(FINAL_ACK_SENT);

        acknowledgmentMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(migrationStatusLogService, times(0)).addMigrationStatusLog(any(), any(), any(), anyString());
    }

    @Test
    public void When_HandleMessage_With_AckTypeCodeAndFinalAckSent_Expect_MigrationStatusNotUpdated() throws SAXException {
        inboundMessage = new InboundMessage();
        prepareXPathServiceMocks(ACK_TYPE_CODE, null);
        prepareMigrationStatusMocks(FINAL_ACK_SENT);

        acknowledgmentMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(migrationStatusLogService, times(0)).addMigrationStatusLog(any(), any(), any(), anyString());
    }

    @Test
    public void When_HandleMessage_With_AckTypeCodeAndFailedStatus_Expect_MigrationStatusNotUpdated() throws SAXException {
        prepareFailedProcessMocks(ACK_TYPE_CODE);

        acknowledgmentMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(migrationStatusLogService, times(0)).addMigrationStatusLog(any(), any(), any(), anyString());
    }

    @Test
    public void When_HandleMessage_With_NackTypeCodeAndFailedStatus_Expect_MigrationStatusNotUpdated() throws SAXException {
        prepareFailedProcessMocks(NACK_ERROR_TYPE_CODE);

        acknowledgmentMessageHandler.handleMessage(inboundMessage, CONVERSATION_ID);

        verify(migrationStatusLogService, times(0)).addMigrationStatusLog(any(), any(), any(), anyString());
    }

    @SneakyThrows
    private void prepareXPathServiceMocks(String typeCode, String errorReasonCode) {
        inboundMessage.setPayload("payload");
        when(xPathService.parseDocumentFromXml(inboundMessage.getPayload())).thenReturn(ebXmlDocument);
        when(xPathService.getNodeValue(ebXmlDocument, ACK_TYPE_CODE_XPATH)).thenReturn(typeCode);

        // Required to prevent unnecessary stubs error
        if (NACK_ERROR_TYPE_CODE.equals(typeCode) || NACK_REJECT_TYPE_CODE.equals(typeCode)) {
            when(xPathService.getNodeValue(ebXmlDocument, ERROR_REASON_CODE_XPATH)).thenReturn(errorReasonCode);
        }
    }

    private void prepareMigrationStatusMocks(MigrationStatus latestMigrationStatus) {
        when(migrationStatusLogService.getLatestMigrationStatusLog(CONVERSATION_ID)).thenReturn(statusLog);
        when(statusLog.getMigrationStatus()).thenReturn(latestMigrationStatus);
    }

    @SneakyThrows
    private void prepareFailedProcessMocks(String errorCode) {
        inboundMessage = new InboundMessage();
        inboundMessage.setPayload("payload");
        when(xPathService.parseDocumentFromXml(inboundMessage.getPayload())).thenReturn(ebXmlDocument);
        when(xPathService.getNodeValue(ebXmlDocument, ACK_TYPE_CODE_XPATH)).thenReturn(errorCode);
        when(failedProcessHandlingService.hasProcessFailed(CONVERSATION_ID)).thenReturn(true);
    }
}
