package uk.nhs.adaptors.pss.translator.task.scheduled;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    @Mock
    private  PersistDurationService persistDurationService;
    @Mock
    private  PatientMigrationRequestService migrationRequestService;
    @Mock
    private  MDCService mdcService;
    @Mock
    private  TimeoutProperties timeoutProperties;
    @Mock
    private  SendNACKMessageHandler sendNACKMessageHandler;
    @Mock
    private  OutboundMessageUtil outboundMessageUtil;
    @Mock
    private  InboundMessageUtil inboundMessageUtil;
    @Mock
    private  MigrationStatusLogService migrationStatusLogService;
    @Mock
    private  PatientAttachmentLogService patientAttachmentLogService;

    @InjectMocks
    private EHRTimeoutHandler ehrTimeoutHandler;

    @Test
    public void WhenCheckForTimeouts_WithTimeout_ExpectSendNACKMessageHandlerIsCalled() throws JsonProcessingException, SAXException {
        PatientMigrationRequest mockRequest = Mockito.mock(PatientMigrationRequest.class);
        List<PatientMigrationRequest> requests = List.of(mockRequest);
        InboundMessage mockInboundMessage = Mockito.mock(InboundMessage.class);
        RCMRIN030000UK06Message mockedPayload = Mockito.mock(RCMRIN030000UK06Message.class);
        try (MockedStatic<XmlUnmarshallUtil> mockedXmlUnmarshall = Mockito.mockStatic(XmlUnmarshallUtil.class)) {
            mockedXmlUnmarshall.when(
                () -> XmlUnmarshallUtil.unmarshallString(eq(mockInboundMessage.getPayload()), eq(RCMRIN030000UK06Message.class))
            ).thenReturn(mockedPayload);

            when(migrationRequestService.getMigrationRequestByCurrentMigrationStatus(EHR_EXTRACT_TRANSLATED))
                .thenReturn(requests);
            when(persistDurationService.getPersistDurationFor(mockRequest, EHR_EXTRACT_MESSAGE_NAME))
                .thenReturn(Duration.ofHours(4));
            when(persistDurationService.getPersistDurationFor(mockRequest, COPC_MESSAGE_NAME))
                .thenReturn(Duration.ofHours(4));
            when(inboundMessageUtil.readMessage(eq(mockRequest.getInboundMessage())))
                .thenReturn(mockInboundMessage);
            when(inboundMessageUtil.parseMessageTimestamp(eq(mockInboundMessage.getEbXML())))
                .thenReturn(ZonedDateTime.of(LocalDateTime.now().minusDays(10), ZoneId.systemDefault()));
            when(patientAttachmentLogService.countAttachmentsForMigrationRequest(mockRequest.getId())).thenReturn((long) 0);
            when(timeoutProperties.getEhrExtractWeighting()).thenReturn(1);
            when(timeoutProperties.getCopcWeighting()).thenReturn(1);
            when(outboundMessageUtil.parseFromAsid(eq(mockedPayload))).thenReturn("");
            when(outboundMessageUtil.parseMessageRef(eq(mockedPayload))).thenReturn("");
            when(outboundMessageUtil.parseToAsid(eq(mockedPayload))).thenReturn("");
            when(outboundMessageUtil.parseToOdsCode(eq(mockedPayload))).thenReturn("");
            when(mockRequest.getConversationId()).thenReturn(UUID.randomUUID().toString());

            ehrTimeoutHandler.checkForTimeouts();

            verify(sendNACKMessageHandler, times(1)).prepareAndSendMessage(any());
        }
    }
}
