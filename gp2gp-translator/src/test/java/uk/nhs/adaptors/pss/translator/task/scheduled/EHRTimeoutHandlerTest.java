package uk.nhs.adaptors.pss.translator.task.scheduled;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.connector.model.MigrationStatus.CONTINUE_REQUEST_ACCEPTED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    @Mock
    private static PersistDurationService persistDurationService;
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

    @InjectMocks
    private EHRTimeoutHandler ehrTimeoutHandler;

    private void setupMocks() {
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
    }

    @Test
    public void When_CheckForTimeouts_WithTimeout_Expect_SendNACKMessageHandlerIsCalled() {
        checkForSendNackMessageHandlerCall(EHR_EXTRACT_TRANSLATED,TEN_DAYS_AGO, 1, 0);
    }

    @Test
    public void WhenCheckForTimeouts_WithoutTimeout_ExpectSendNACKMessageHandlerIsNotCalled() {
        checkForSendNackMessageHandlerCall(EHR_EXTRACT_TRANSLATED, TEN_DAYS_TIME, 0, 0);

    }

    @Test
    public void WhenCheckForTimeouts_WithAttachments_ExpectSendNACKMessageHandlerIsCalled() {
        checkForSendNackMessageHandlerCall(CONTINUE_REQUEST_ACCEPTED, TEN_DAYS_AGO, 1, 1);
    }

    private void checkForSendNackMessageHandlerCall(MigrationStatus migrationStatus, ZonedDateTime requestTimestamp, int numberOfInvocations, long numberOfAttachments) {
        // Arrange
        PatientMigrationRequest mockRequest = Mockito.mock(PatientMigrationRequest.class);
        List<PatientMigrationRequest> requests = List.of(mockRequest);
        InboundMessage mockInboundMessage = Mockito.mock(InboundMessage.class);
        RCMRIN030000UK06Message mockedPayload = Mockito.mock(RCMRIN030000UK06Message.class);

        try (MockedStatic<XmlUnmarshallUtil> mockedXmlUnmarshall = Mockito.mockStatic(XmlUnmarshallUtil.class)) {
            // mock static method
            mockedXmlUnmarshall.when(
                () -> XmlUnmarshallUtil.unmarshallString(eq(mockInboundMessage.getPayload()), eq(RCMRIN030000UK06Message.class))
            ).thenReturn(mockedPayload);

            when(migrationRequestService.getMigrationRequestByCurrentMigrationStatus(migrationStatus))
                .thenReturn(requests);

            setupMocks();
            // inbound message
            when(inboundMessageUtil.readMessage(eq(mockRequest.getInboundMessage())))
                .thenReturn(mockInboundMessage);
            // timestamp
            when(inboundMessageUtil.parseMessageTimestamp(eq(mockInboundMessage.getEbXML())))
                .thenReturn(requestTimestamp);
            // number of attachments
            when(patientAttachmentLogService.countAttachmentsForMigrationRequest(mockRequest.getId())).thenReturn(numberOfAttachments);
            // random conversation id for mocked request
            when(mockRequest.getConversationId()).thenReturn(UUID.randomUUID().toString());

            // Act
            ehrTimeoutHandler.checkForTimeouts();

            // Assert
            verify(sendNACKMessageHandler, times(numberOfInvocations)).prepareAndSendMessage(any());
        } catch (JsonProcessingException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
