package uk.nhs.adaptors.pss.translator.task.scheduled;

import static com.jayway.jsonpath.JsonPath.parse;
import static java.time.LocalTime.now;
import static java.util.UUID.randomUUID;

import static org.hl7.fhir.dstu3.model.ResourceType.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static uk.nhs.adaptors.connector.model.MigrationStatus.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.BaseStubbing;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.objectweb.asm.TypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.w3c.dom.Document;
import uk.nhs.adaptors.common.service.MDCService;
import uk.nhs.adaptors.connector.model.MessagePersistDuration;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MessagePersistDurationService;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.connector.service.PatientMigrationRequestService;
import uk.nhs.adaptors.pss.translator.config.TimeoutProperties;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.NACKMessageData;
import uk.nhs.adaptors.pss.translator.service.SDSService;
import uk.nhs.adaptors.pss.translator.service.XPathService;
import uk.nhs.adaptors.pss.translator.task.SendNACKMessageHandler;
import uk.nhs.adaptors.pss.translator.util.OutboundMessageUtil;
import xhtml.npfit.presentationtext.P;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class EHRTimeoutHandlerTest {

    @Mock
    private MessagePersistDurationService messagePersistDurationService;

    @Mock
    private SDSService sdsService;

    @Mock
    private PatientMigrationRequestService migrationRequestService;

    @Mock
    private MDCService mdcService;

    @Mock
    private XPathService xPathService;

    @Mock
    private TimeoutProperties timeoutProperties;

    @Mock
    private SendNACKMessageHandler sendNACKMessageHandler;

    @Mock
    private OutboundMessageUtil outboundMessageUtil;

    @Mock
    private MigrationStatusLogService migrationStatusLogService;

    @Mock
    private PatientAttachmentLogService patientAttachmentLogService;

    @Mock
    private PatientMigrationRequest patientMigrationRequestMock;

    @Mock
    private NACKMessageData mockNackMessageData;


    private List<PatientMigrationRequest> pmList;

    // class under test
    @InjectMocks
    public EHRTimeoutHandler ehrTimeoutHandler;

    @Test
    public void ehrTimeoutSendsNakMessage() throws IOException {

        //given
        when(patientMigrationRequestMock.getConversationId()).thenReturn("123");
        when(patientMigrationRequestMock.getId()).thenReturn(1);
        OngoingStubbing<String> stringOngoingStubbing;
        //stringOngoingStubbing = when(patientMigrationRequestMock.getInboundMessage()).thenReturn(createInboundMessage());
        pmList = Arrays.asList(patientMigrationRequestMock);
        when(messagePersistDurationService.getMessagePersistDuration(anyInt(), anyString())).thenReturn((Optional<MessagePersistDuration>) Optional.of(MessagePersistDuration.builder().persistDuration(Duration.ofSeconds(2)).id(123).messageType("A").build()));

        //when
        //when(patientMigrationRequestMock.getInboundMessage()).thenReturn("");

        when(mdcService.getConversationId()).thenReturn("123");

        when(migrationRequestService.getMigrationRequestByCurrentMigrationStatus(EHR_EXTRACT_TRANSLATED)).thenReturn(pmList);
        when(messagePersistDurationService.getMessagePersistDuration(anyInt(), anyString())).thenReturn(Optional.of(MessagePersistDuration.builder().persistDuration(Duration.ofSeconds(2)).id(123).messageType("A").build()));

        when(messagePersistDurationService.addMessagePersistDuration(anyString(), eq(Duration.ofSeconds(2)), anyInt(), anyInt())).thenReturn(MessagePersistDuration.builder().persistDuration(Duration.ofSeconds(2)).id(123).messageType("A").build());
        //when(objectMapper.reader()).thenReturn(mockReader);
        //when(mockReader.readValue((JsonParser) any(), eq(InboundMessage.class))).thenReturn(mockInboundMessage);
        //when(objectMapper.readValue(eq(""), eq(InboundMessage.class))).thenReturn(mockInboundMessage);
        //when(mockInboundMessage.getEbXML()).thenReturn("321");

        try (MockedStatic<DateTime> dtUtilities = Mockito.mockStatic(DateTime.class)) {
            dtUtilities.when(() -> DateTime.parse(anyString())).thenReturn(new DateTime());
            dtUtilities.when(() -> DateTime.now()).thenReturn(new DateTime());
        }


        //when(xPathService.getNodeValue
        //        (any(), any())).thenReturn(DateTime.now().toString());

        //when(mockNackMessageData(mockNackMessageData.getConversationId()).thenReturn("");

        when(sendNACKMessageHandler.prepareAndSendMessage(mockNackMessageData)).thenThrow(new RuntimeException("Success - terminete unit test"));

        ehrTimeoutHandler.checkForTimeouts();
        verify(sendNACKMessageHandler, Mockito.times(1)).prepareAndSendMessage(any());

    }

    private String createInboundMessage() throws IOException {
        InboundMessage inboundMessage = new InboundMessage();
        inboundMessage.setEbXML(readFileAsString("xml/RCMR_IN030000UK06/ebxml_part.xml"));
        inboundMessage.setPayload(readFileAsString("xml/RCMR_IN030000UK06/payload_part.xml"));
        String s = new ObjectMapper().writeValueAsString(inboundMessage);
        return s;
    }

    private static String readFileAsString(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return Files.readString(Paths.get(resource.getURI()));
    }

}

