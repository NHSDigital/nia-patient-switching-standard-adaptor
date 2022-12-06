package uk.nhs.adaptors.pss.translator.task.scheduled;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.connector.model.MigrationStatus.CONTINUE_REQUEST_ACCEPTED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.COPC_ACKNOWLEDGED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.COPC_FAILED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.COPC_MESSAGE_PROCESSING;
import static uk.nhs.adaptors.connector.model.MigrationStatus.COPC_MESSAGE_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_PROCESSING;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_GENERAL_PROCESSING_ERROR;
import static uk.nhs.adaptors.pss.translator.model.NACKReason.LARGE_MESSAGE_TIMEOUT;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

import org.hl7.v3.RCMRIN030000UK06Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;

import uk.nhs.adaptors.common.service.MDCService;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.connector.service.PatientMigrationRequestService;
import uk.nhs.adaptors.pss.translator.config.TimeoutProperties;
import uk.nhs.adaptors.pss.translator.exception.MhsServerErrorException;
import uk.nhs.adaptors.pss.translator.exception.SdsRetrievalException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.service.PersistDurationService;
import uk.nhs.adaptors.pss.translator.task.SendNACKMessageHandler;
import uk.nhs.adaptors.pss.translator.util.InboundMessageUtil;
import uk.nhs.adaptors.pss.translator.util.OutboundMessageUtil;
import uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EHRTimeoutHandlerTest {

    private static final String EHR_EXTRACT_MESSAGE_NAME = "RCMR_IN030000UK06";
    private static final String COPC_MESSAGE_NAME = "COPC_IN000001UK01";

    private static final int EHR_EXTRACT_PERSIST_DURATION = 7;
    private static final int COPC_PERSIST_DURATION = 4;
    private static final ZonedDateTime TEN_DAYS_AGO = ZonedDateTime.of(LocalDateTime.now().minusDays(10), ZoneId.systemDefault());
    private static final ZonedDateTime TEN_DAYS_TIME = ZonedDateTime.of(LocalDateTime.now().plusDays(10), ZoneId.systemDefault());
    private static final String INBOUND_MESSAGE_STRING = "test inbound Message";
    private static final String INBOUND_MESSAGE_STRING_TWO = "test inbound Message 2";
    private static final String EBXML_STRING = "test ebXML";
    private static final String EBXML_STRING_TWO = "test ebXML 2";
    @Mock
    private PersistDurationService persistDurationService;
    @Mock
    private PatientMigrationRequestService migrationRequestService;
    @Mock
    private MDCService mdcService;
    @Mock
    private TimeoutProperties timeoutProperties;
    @Mock
    private SendNACKMessageHandler sendNACKMessageHandler;
    @Mock
    private OutboundMessageUtil outboundMessageUtil;
    @Mock
    private InboundMessageUtil inboundMessageUtil;
    @Mock
    private MigrationStatusLogService migrationStatusLogService;
    @Mock
    private PatientAttachmentLogService patientAttachmentLogService;
    @Mock(name = "mockInboundMessage")
    private InboundMessage mockInboundMessage;
    @Mock(name = "mockInboundMessage2")
    private InboundMessage mockInboundMessage2;
    @Mock
    private RCMRIN030000UK06Message mockedPayload;
    @Mock(name = "mockRequest")
    private PatientMigrationRequest mockRequest;
    @Mock(name = "mockRequest2")
    private PatientMigrationRequest mockRequest2;
    @InjectMocks
    private EHRTimeoutHandler ehrTimeoutHandler;

    private void setupMocks() throws JsonProcessingException {
        when(persistDurationService.getPersistDurationFor(any(), eq(EHR_EXTRACT_MESSAGE_NAME)))
            .thenReturn(Duration.ofHours(EHR_EXTRACT_PERSIST_DURATION));
        when(persistDurationService.getPersistDurationFor(any(), eq(COPC_MESSAGE_NAME)))
            .thenReturn(Duration.ofHours(COPC_PERSIST_DURATION));
        when(timeoutProperties.getEhrExtractWeighting()).thenReturn(1);
        when(timeoutProperties.getCopcWeighting()).thenReturn(1);
        when(outboundMessageUtil.parseFromAsid(any())).thenReturn("");
        when(outboundMessageUtil.parseMessageRef(any())).thenReturn("");
        when(outboundMessageUtil.parseToAsid(any())).thenReturn("");
        when(outboundMessageUtil.parseToOdsCode(any())).thenReturn("");

        // inbound messages
        when(mockRequest.getInboundMessage())
            .thenReturn(INBOUND_MESSAGE_STRING);
        when(mockRequest2.getInboundMessage())
            .thenReturn(INBOUND_MESSAGE_STRING_TWO);
        when(inboundMessageUtil.readMessage(INBOUND_MESSAGE_STRING))
            .thenReturn(mockInboundMessage);
        when(inboundMessageUtil.readMessage(INBOUND_MESSAGE_STRING_TWO))
            .thenReturn(mockInboundMessage2);
    }

    @Test
    public void When_CheckForTimeouts_WithTimeout_Expect_SendNACKMessageHandlerIsCalled() {
        String conversationId = UUID.randomUUID().toString();
        callCheckForTimeoutsWithOneRequest(EHR_EXTRACT_TRANSLATED, TEN_DAYS_AGO, 0, conversationId);
        verify(sendNACKMessageHandler, times(1)).prepareAndSendMessage(any());
    }

    @Test
    public void When_CheckForTimeouts_WithNackFailsToSend_Expect_MigrationLogNotUpdated() {
        String conversationId = UUID.randomUUID().toString();

        when(sendNACKMessageHandler.prepareAndSendMessage(any())).thenReturn(false);

        callCheckForTimeoutsWithOneRequest(EHR_EXTRACT_TRANSLATED, TEN_DAYS_AGO, 0, conversationId);
        verify(migrationStatusLogService, times(0))
            .addMigrationStatusLog(LARGE_MESSAGE_TIMEOUT.getMigrationStatus(), conversationId, null);
    }

    @Test
    public void When_CheckForTimeouts_WithSendNackThrows_Expect_MigrationLogNotUpdated() {
        String conversationId = UUID.randomUUID().toString();

        when(sendNACKMessageHandler.prepareAndSendMessage(any())).thenThrow(MhsServerErrorException.class);

        assertThatThrownBy(
                () -> callCheckForTimeoutsWithOneRequest(EHR_EXTRACT_TRANSLATED, TEN_DAYS_AGO, 0, conversationId))
            .isInstanceOf(MhsServerErrorException.class);

        verify(migrationStatusLogService, times(0))
            .addMigrationStatusLog(LARGE_MESSAGE_TIMEOUT.getMigrationStatus(), conversationId, null);
    }

    @Test
    public void When_CheckForTimeouts_WithTimeout_Expect_MigrationLogUpdated() {
        String conversationId = UUID.randomUUID().toString();

        when(sendNACKMessageHandler.prepareAndSendMessage(any())).thenReturn(true);

        callCheckForTimeoutsWithOneRequest(EHR_EXTRACT_TRANSLATED, TEN_DAYS_AGO, 0, conversationId);
        verify(migrationStatusLogService, times(1))
            .addMigrationStatusLog(LARGE_MESSAGE_TIMEOUT.getMigrationStatus(), conversationId, null);
    }

    @Test
    public void When_CheckForTimeouts_WithTimeoutAndCOPCReceived_Expect_MigrationLogUpdated() {
        String conversationId = UUID.randomUUID().toString();

        when(sendNACKMessageHandler.prepareAndSendMessage(any())).thenReturn(true);

        callCheckForTimeoutsWithOneRequest(COPC_MESSAGE_RECEIVED, TEN_DAYS_AGO, 2, conversationId);
        verify(migrationStatusLogService, times(1))
            .addMigrationStatusLog(LARGE_MESSAGE_TIMEOUT.getMigrationStatus(), conversationId, null);
    }

    @Test
    public void When_CheckForTimeouts_WithTimeoutAndCOPCProcessing_Expect_MigrationLogUpdated() {
        String conversationId = UUID.randomUUID().toString();

        when(sendNACKMessageHandler.prepareAndSendMessage(any())).thenReturn(true);

        callCheckForTimeoutsWithOneRequest(COPC_MESSAGE_PROCESSING, TEN_DAYS_AGO, 2, conversationId);
        verify(migrationStatusLogService, times(1))
            .addMigrationStatusLog(LARGE_MESSAGE_TIMEOUT.getMigrationStatus(), conversationId, null);
    }

    @Test
    public void When_CheckForTimeouts_WithTimeoutAndCOPCAcknowledged_Expect_MigrationLogUpdated() {
        String conversationId = UUID.randomUUID().toString();

        when(sendNACKMessageHandler.prepareAndSendMessage(any())).thenReturn(true);

        callCheckForTimeoutsWithOneRequest(COPC_ACKNOWLEDGED, TEN_DAYS_AGO, 2, conversationId);
        verify(migrationStatusLogService, times(1))
            .addMigrationStatusLog(LARGE_MESSAGE_TIMEOUT.getMigrationStatus(), conversationId, null);
    }

    @Test
    public void When_CheckForTimeouts_WithTimeoutAndEhrExtractProcessing_Expect_MigrationLogUpdated() {
        String conversationId = UUID.randomUUID().toString();

        when(sendNACKMessageHandler.prepareAndSendMessage(any())).thenReturn(true);

        callCheckForTimeoutsWithOneRequest(EHR_EXTRACT_PROCESSING, TEN_DAYS_AGO, 2, conversationId);
        verify(migrationStatusLogService, times(1))
            .addMigrationStatusLog(LARGE_MESSAGE_TIMEOUT.getMigrationStatus(), conversationId, null);
    }

    @Test
    public void When_CheckForTimeouts_WithTimeoutAndCopcFailed_Expect_MigrationLogUpdated() {
        String conversationId = UUID.randomUUID().toString();

        when(sendNACKMessageHandler.prepareAndSendMessage(any())).thenReturn(true);

        callCheckForTimeoutsWithOneRequest(COPC_FAILED, TEN_DAYS_AGO, 2, conversationId);
        verify(migrationStatusLogService, times(1))
            .addMigrationStatusLog(LARGE_MESSAGE_TIMEOUT.getMigrationStatus(), conversationId, null);
    }

    @Test
    public void When_CheckForTimeouts_WithoutTimeout_Expect_SendNACKMessageHandlerIsNotCalled() {
        String conversationId = UUID.randomUUID().toString();
        callCheckForTimeoutsWithOneRequest(EHR_EXTRACT_TRANSLATED, TEN_DAYS_TIME, 0, conversationId);
        verify(sendNACKMessageHandler, times(0)).prepareAndSendMessage(any());
    }

    @Test
    public void When_CheckForTimeouts_WithAttachments_Expect_SendNACKMessageHandlerIsCalled() {
        String conversationId = UUID.randomUUID().toString();
        callCheckForTimeoutsWithOneRequest(CONTINUE_REQUEST_ACCEPTED, TEN_DAYS_AGO, 1, conversationId);
        verify(sendNACKMessageHandler, times(1)).prepareAndSendMessage(any());
    }

    @Test
    public void When_CheckForTimeouts_WithCollectionAndTwoTimeouts_Expect_SendNACKMessageHandlerCalledTwice() {
        callCheckForTimeoutWithTwoRequests(TEN_DAYS_AGO, TEN_DAYS_AGO);
        verify(sendNACKMessageHandler, times(2)).prepareAndSendMessage(any());
    }

    @Test
    public void When_CheckForTimeouts_WithCollectionAndOneTimeout_Expect_SendNACKMessageHandlerCalledOnce() {
        callCheckForTimeoutWithTwoRequests(TEN_DAYS_TIME, TEN_DAYS_AGO);
        verify(sendNACKMessageHandler, times(1)).prepareAndSendMessage(any());
    }

    @Test
    public void When_CheckForTimeouts_WithCollectionAndNoTimeouts_Expect_SendNackMessageHandlerNotCalled() {
        callCheckForTimeoutWithTwoRequests(TEN_DAYS_TIME, TEN_DAYS_TIME);
        verify(sendNACKMessageHandler, times(0)).prepareAndSendMessage(any());
    }

    @Test
    public void When_CheckForTimeouts_WithSdsRetrievalException_Expect_MigrationLogNotUpdated() {
        List<PatientMigrationRequest> requests = List.of(mockRequest);
        when(migrationRequestService.getMigrationRequestByCurrentMigrationStatus(EHR_EXTRACT_TRANSLATED))
            .thenReturn(requests);
        when(persistDurationService.getPersistDurationFor(any(), eq(EHR_EXTRACT_MESSAGE_NAME)))
            .thenThrow(new SdsRetrievalException("Test exception"));

        ehrTimeoutHandler.checkForTimeouts();

        verify(migrationStatusLogService, times(0)).addMigrationStatusLog(any(), any(), isNull());
    }

    @Test
    public void When_CheckForTimeouts_WithJsonProcessingException_Expect_MigrationLogUpdated() throws JsonProcessingException {
        String conversationId = UUID.randomUUID().toString();
        List<PatientMigrationRequest> requests = List.of(mockRequest);
        when(migrationRequestService.getMigrationRequestByCurrentMigrationStatus(EHR_EXTRACT_TRANSLATED))
            .thenReturn(requests);
        when(mockRequest.getConversationId()).thenReturn(conversationId);

        doThrow(JsonProcessingException.class).when(inboundMessageUtil).readMessage(any());

        ehrTimeoutHandler.checkForTimeouts();

        verify(migrationStatusLogService, times(1)).addMigrationStatusLog(EHR_GENERAL_PROCESSING_ERROR, conversationId, null);
    }

    @Test
    public void When_CheckForTimeouts_WithSAXException_Expect_MigrationLogUpdated() throws SAXException, JsonProcessingException {
        String conversationId = UUID.randomUUID().toString();
        List<PatientMigrationRequest> requests = List.of(mockRequest);
        when(migrationRequestService.getMigrationRequestByCurrentMigrationStatus(EHR_EXTRACT_TRANSLATED))
            .thenReturn(requests);
        when(mockRequest.getConversationId()).thenReturn(conversationId);
        when(inboundMessageUtil.readMessage(any())).thenReturn(mockInboundMessage);

        doThrow(SAXException.class).when(inboundMessageUtil).parseMessageTimestamp(any());

        ehrTimeoutHandler.checkForTimeouts();

        verify(migrationStatusLogService, times(1)).addMigrationStatusLog(EHR_GENERAL_PROCESSING_ERROR, conversationId, null);
    }

    @Test
    public void When_CheckForTimeouts_WithDateTimeParseException_Expect_MigrationLogUpdated() throws SAXException, JsonProcessingException {
        String conversationId = UUID.randomUUID().toString();
        List<PatientMigrationRequest> requests = List.of(mockRequest);
        when(migrationRequestService.getMigrationRequestByCurrentMigrationStatus(EHR_EXTRACT_TRANSLATED))
            .thenReturn(requests);
        when(mockRequest.getConversationId()).thenReturn(conversationId);
        when(inboundMessageUtil.readMessage(any())).thenReturn(mockInboundMessage);

        doThrow(DateTimeParseException.class).when(inboundMessageUtil).parseMessageTimestamp(any());

        ehrTimeoutHandler.checkForTimeouts();

        verify(migrationStatusLogService, times(1)).addMigrationStatusLog(EHR_GENERAL_PROCESSING_ERROR, conversationId, null);
    }

    private void callCheckForTimeoutsWithOneRequest(MigrationStatus migrationStatus, ZonedDateTime requestTimestamp,
        long numberOfAttachments, String conversationId) {

        try {

            // Arrange

            setupMocks();

            // mock static method
            MockedStatic<XmlUnmarshallUtil> mockedXmlUnmarshall = Mockito.mockStatic(XmlUnmarshallUtil.class);
            mockedXmlUnmarshall.when(
                () -> XmlUnmarshallUtil.unmarshallString(any(), eq(RCMRIN030000UK06Message.class))
            ).thenReturn(mockedPayload);

            // request
            List<PatientMigrationRequest> requests = List.of(mockRequest);
            when(migrationRequestService.getMigrationRequestByCurrentMigrationStatus(migrationStatus))
                .thenReturn(requests);

            // timestamp
            when(mockInboundMessage.getEbXML())
                .thenReturn(EBXML_STRING);
            when(inboundMessageUtil.parseMessageTimestamp(EBXML_STRING))
                .thenReturn(requestTimestamp);
            // number of attachments
            when(patientAttachmentLogService.countAttachmentsForMigrationRequest(mockRequest.getId())).thenReturn(numberOfAttachments);
            // random conversation id for mocked request
            when(mockRequest.getConversationId()).thenReturn(conversationId);

            // Act

            ehrTimeoutHandler.checkForTimeouts();

            mockedXmlUnmarshall.close();
        } catch (JsonProcessingException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    private void callCheckForTimeoutWithTwoRequests(ZonedDateTime firstRequestTimestamp, ZonedDateTime secondRequestTimestamp) {

        try {

            // Arrange

            setupMocks();

            // mock static method
            MockedStatic<XmlUnmarshallUtil> mockedXmlUnmarshall = Mockito.mockStatic(XmlUnmarshallUtil.class);
            mockedXmlUnmarshall.when(
                () -> XmlUnmarshallUtil.unmarshallString(any(), eq(RCMRIN030000UK06Message.class))
            ).thenReturn(mockedPayload);

            // requests
            List<PatientMigrationRequest> requests = List.of(mockRequest, mockRequest2);
            when(migrationRequestService.getMigrationRequestByCurrentMigrationStatus(MigrationStatus.EHR_EXTRACT_TRANSLATED))
                .thenReturn(requests);

            // timestamps
            when(mockInboundMessage.getEbXML())
                .thenReturn(EBXML_STRING);
            when(mockInboundMessage2.getEbXML())
                .thenReturn(EBXML_STRING_TWO);
            when(inboundMessageUtil.parseMessageTimestamp(EBXML_STRING))
                .thenReturn(firstRequestTimestamp);
            when(inboundMessageUtil.parseMessageTimestamp(EBXML_STRING_TWO))
                .thenReturn(secondRequestTimestamp);

            // number of attachments
            when(patientAttachmentLogService.countAttachmentsForMigrationRequest(anyInt())).thenReturn((long) 0);

            // random conversation id for mocked request
            when(mockRequest.getConversationId()).thenReturn(UUID.randomUUID().toString());
            when(mockRequest2.getConversationId()).thenReturn(UUID.randomUUID().toString());

            // Act

            ehrTimeoutHandler.checkForTimeouts();

            mockedXmlUnmarshall.close();

        } catch (JsonProcessingException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
