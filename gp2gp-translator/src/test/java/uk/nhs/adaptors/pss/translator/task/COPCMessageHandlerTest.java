package uk.nhs.adaptors.pss.translator.task;

import static java.util.UUID.randomUUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;

import java.util.Arrays;

import javax.xml.bind.JAXBException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Document;

import lombok.SneakyThrows;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.exception.SkeletonEhrProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.service.AttachmentHandlerService;
import uk.nhs.adaptors.pss.translator.service.XPathService;

@ExtendWith(MockitoExtension.class)
class COPCMessageHandlerTest {

    private static final String CONVERSATION_ID = randomUUID().toString();
    private static final String LOSING_ODE_CODE = "G543";
    private static final String WINNING_ODE_CODE = "B943";
    private static final String MESSAGE_ID = "CBBAE92D-C7E8-4A9C-8887-F5AEBA1F8CE1";

    @Mock
    private PatientMigrationRequestDao patientMigrationRequestDao;
    @Mock
    private MigrationStatusLogService migrationStatusLogService;
    @Mock
    private AttachmentHandlerService attachmentHandlerService;
    @Mock
    private PatientAttachmentLogService patientAttachmentLogService;
    @Mock
    private XPathService xPathService;
    @Mock
    private SendACKMessageHandler sendACKMessageHandler;
    @Mock
    private SendNACKMessageHandler sendNACKMessageHandler;
    @InjectMocks
    private COPCMessageHandler copcMessageHandler;
    @Captor
    private ArgumentCaptor<PatientAttachmentLog> patientLogCaptor;
    @Captor
    private ArgumentCaptor<String> filenameCaptor;
    @Captor
    private ArgumentCaptor<String> payloadCaptor;
    @Captor
    private ArgumentCaptor<String> conversationIdCaptor;
    @Captor
    private ArgumentCaptor<String> contentTypeCaptor;
    @Mock
    private Document ebXmlDocument;

    @Test
    public void shouldCreateFragmentRecordWhenFragmentIsReceivedBeforeFragmentIndex() throws JAXBException,
        InlineAttachmentProcessingException {
        // Arrange
        var messageId = "CBBAE92D-C7E8-4A9C-8887-F5AEBA1F8CE1";
        when(patientAttachmentLogService.findAttachmentLog(messageId, CONVERSATION_ID)).thenReturn(null);
        InboundMessage message = new InboundMessage();
        prepareFragmentMocks(message);
        // ACT
        copcMessageHandler.handleMessage(message, CONVERSATION_ID);

        // Assert
        verify(patientAttachmentLogService).addAttachmentLog(patientLogCaptor.capture());

        PatientAttachmentLog actual = patientLogCaptor.getAllValues().get(0);

        assertThat(actual.getUploaded()).isTrue();
        assertEquals("047C22B4-613F-47D3-9A72-44A1758464FB", actual.getMid());
        assertEquals("E39E79A2-FA96-48FF-9373-7BBCB9D036E7_1.messageattachment", actual.getFilename());
        assertEquals("xml/text", actual.getContentType());
        assertEquals(1, actual.getPatientMigrationReqId());
    }

    @Test
    public void shouldUploadFileWhenFragmentIsReceivedBeforeFragmentIndex() throws JAXBException,
            InlineAttachmentProcessingException, SkeletonEhrProcessingException {
        // Arrange

        when(patientAttachmentLogService.findAttachmentLog(MESSAGE_ID, CONVERSATION_ID)).thenReturn(null);
        InboundMessage message = new InboundMessage();
        prepareFragmentMocks(message);
        // ACT
        copcMessageHandler.handleMessage(message, CONVERSATION_ID);

        // Assert
        verify(attachmentHandlerService).storeEhrExtract(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void shouldUpdateExistingFragmentRecordWhenItHasAlreadyBeenReceived() throws JAXBException,
        InlineAttachmentProcessingException {
        var parentMid = "CBBAE92D-C7E8-4A9C-8887-F5AEBA1F8CE1";
        var childMid = "28B31-4245-4AFC-8DA2-8A40623A5101";
        InboundMessage message = new InboundMessage();
        when(patientAttachmentLogService.findAttachmentLog(MESSAGE_ID, CONVERSATION_ID))
            .thenReturn(buildPatientAttachmentLog(parentMid, null));
        when(patientAttachmentLogService.findAttachmentLog(childMid, CONVERSATION_ID))
            .thenReturn(buildPartialPatientAttachmentLog(childMid, "text/plain"));
        prepareFragmentIndexMocks(message);
        copcMessageHandler.handleMessage(message, CONVERSATION_ID);

        verify(patientAttachmentLogService).updateAttachmentLog(patientLogCaptor.capture(), conversationIdCaptor.capture());

        PatientAttachmentLog actual = patientLogCaptor.getValue();
        assertThat(actual.getBase64()).isTrue();
        assertThat(actual.getLargeAttachment()).isFalse();
        assertThat(actual.getCompressed()).isFalse();
        assertEquals(0, actual.getOrderNum());
        assertEquals(parentMid, actual.getParentMid());
        assertEquals(CONVERSATION_ID, conversationIdCaptor.getValue());
    }

    @Test
    public void shouldCreateNewFragmentRecordWhenItDoesntExistInPatientAttachmentLogDb() throws JAXBException,
        InlineAttachmentProcessingException {
        var childMid = "28B31-4245-4AFC-8DA2-8A40623A5101";
        InboundMessage message = new InboundMessage();
        when(patientAttachmentLogService.findAttachmentLog(MESSAGE_ID, CONVERSATION_ID))
            .thenReturn(buildPatientAttachmentLog("CBBAE92D-C7E8-4A9C-8887-F5AEBA1F8CE1", null));
        when(patientAttachmentLogService.findAttachmentLog(childMid, CONVERSATION_ID))
            .thenReturn(null);
        prepareFragmentIndexMocks(message);
        copcMessageHandler.handleMessage(message, CONVERSATION_ID);

        verify(patientAttachmentLogService).addAttachmentLog(patientLogCaptor.capture());
        PatientAttachmentLog actual = patientLogCaptor.getValue();

        assertEquals(childMid, actual.getMid());
        assertEquals("E39E79A2-FA96-48FF-9373-7BBCB9D036E7_0.messageattachment", actual.getFilename());
        assertEquals("CBBAE92D-C7E8-4A9C-8887-F5AEBA1F8CE1", actual.getParentMid());
        assertEquals(1, actual.getPatientMigrationReqId());
        assertEquals("text/plain", actual.getContentType());
        assertThat(actual.getCompressed()).isFalse();
        assertThat(actual.getLargeAttachment()).isFalse();
        assertThat(actual.getBase64()).isTrue();
        assertEquals(0, actual.getOrderNum());
    }

    @Test
    public void shouldUploadFragmentFileWhenAFragmentMessageIsReceived() throws JAXBException, InlineAttachmentProcessingException, SkeletonEhrProcessingException {
        InboundMessage message = new InboundMessage();
        when(patientAttachmentLogService.findAttachmentLog(MESSAGE_ID, CONVERSATION_ID))
            .thenReturn(buildPatientAttachmentLog("047C22B4-613F-47D3-9A72-44A1758464FB",
                "CBBAE92D-C7E8-4A9C-8887-F5AEBA1F8CE1"));
        prepareExpectedFragmentMocks(message);

        copcMessageHandler.handleMessage(message, CONVERSATION_ID);

        verify(attachmentHandlerService).storeEhrExtract(filenameCaptor.capture(), payloadCaptor.capture(),
            conversationIdCaptor.capture(), contentTypeCaptor.capture());

        assertEquals("E39E79A2-FA96-48FF-9373-7BBCB9D036E7.txt", filenameCaptor.getValue());
        assertEquals("This is a payload", payloadCaptor.getValue());
        assertEquals(CONVERSATION_ID, conversationIdCaptor.getValue());
        assertEquals("xml/text", contentTypeCaptor.getValue());
    }

    @Test
    public void shouldUpdatePatientAttachmentLogAfterFileIsUploadedWhenAFragmentMessageIsReceived() throws JAXBException,
        InlineAttachmentProcessingException {
        InboundMessage message = new InboundMessage();
        when(patientAttachmentLogService.findAttachmentLog(MESSAGE_ID, CONVERSATION_ID))
            .thenReturn(buildPatientAttachmentLog("047C22B4-613F-47D3-9A72-44A1758464FB", "CBBAE92D-C7E8-4A9C-8887-F5AEBA1F8CE1"));
        prepareExpectedFragmentMocks(message);

        copcMessageHandler.handleMessage(message, CONVERSATION_ID);

        verify(patientAttachmentLogService).updateAttachmentLog(patientLogCaptor.capture(), conversationIdCaptor.capture());

        PatientAttachmentLog actual = patientLogCaptor.getAllValues().get(0);
        assertThat(actual.getUploaded()).isTrue();
        assertEquals(CONVERSATION_ID, conversationIdCaptor.getValue());
    }

    private PatientAttachmentLog buildPatientAttachmentLog(String mid, String parentMid) {
        return PatientAttachmentLog.builder()
            .mid(mid)
            .filename("E39E79A2-FA96-48FF-9373-7BBCB9D036E7.txt")
            .parentMid(parentMid)
            .patientMigrationReqId(1)
            .contentType("xml/text")
            .skeleton(false)
            .build();
    }

    private PatientAttachmentLog buildPartialPatientAttachmentLog(String mid, String contentType) {
        return PatientAttachmentLog.builder()
            .mid(mid)
            .filename("Filename=E39E79A2-FA96-48FF-9373-7BBCB9D036E7_1.messageattachment")
            .patientMigrationReqId(1)
            .contentType(contentType)
            .build();
    }

    @SneakyThrows
    private void prepareExpectedFragmentMocks(InboundMessage message) {
        message.setPayload(readXmlFile("inbound_message_payload_fragment_index.xml"));
        String ebxml = readXmlFile("inbound_message_ebxml_fragment_index.xml");
        message.setEbXML(ebxml);
        message.setAttachments(Arrays.asList(new InboundMessage.Attachment("xml/text", "Yes", "Filename=E39E79A2-FA96-48FF-9373"
            + "-7BBCB9D036E7_1.messageattachment ContentType=text/plain Compressed=No LargeAttachment=No OriginalBase64=Yes", "this is a "
            + "payload")));
        message.getAttachments().get(0).setPayload("This is a payload");
        PatientMigrationRequest migrationRequest =
            PatientMigrationRequest.builder()
                .id(1)
                .losingPracticeOdsCode(LOSING_ODE_CODE)
                .winningPracticeOdsCode(WINNING_ODE_CODE)
                .build();

        when(patientMigrationRequestDao.getMigrationRequest(CONVERSATION_ID)).thenReturn(migrationRequest);

        when(xPathService.parseDocumentFromXml(message.getEbXML())).thenReturn(ebXmlDocument);
        when(xPathService.getNodeValue(ebXmlDocument, "/Envelope/Header/MessageHeader/MessageData/MessageId"))
            .thenReturn("CBBAE92D-C7E8-4A9C-8887-F5AEBA1F8CE1");
        when(xPathService.getNodeValue(ebXmlDocument, "/Envelope/Body/Manifest/Reference[position()=2]/Description"))
            .thenReturn("not an index file");
    }

    @SneakyThrows
    private void prepareFragmentIndexMocks(InboundMessage message) {
        message.setPayload(readXmlFile("inbound_message_payload_fragment_index.xml"));
        String ebxml = readXmlFile("inbound_message_ebxml_fragment_index.xml");
        message.setEbXML(ebxml);
        InboundMessage.ExternalAttachment extAttachment = new InboundMessage.ExternalAttachment("CBBAE92D-C7E8-4A9C-8887-F5AEBA1F8CE1",
            "28B31-4245-4AFC-8DA2-8A40623A5101", "E39E79A2-FA96-48FF-9373-7BBCB9D036E7_0.messageattachment", "Filename=E39E79A2-FA96-48FF"
            + "-9373-7BBCB9D036E7_0.messageattachment ContentType=text/plain Compressed=No LargeAttachment=No OriginalBase64=Yes");

        message.setExternalAttachments(Arrays.asList(extAttachment));
        PatientMigrationRequest migrationRequest =
            PatientMigrationRequest.builder()
                .id(1)
                .losingPracticeOdsCode(LOSING_ODE_CODE)
                .winningPracticeOdsCode(WINNING_ODE_CODE)
                .build();

        when(patientMigrationRequestDao.getMigrationRequest(CONVERSATION_ID)).thenReturn(migrationRequest);

        when(xPathService.parseDocumentFromXml(message.getEbXML())).thenReturn(ebXmlDocument);
        when(xPathService.getNodeValue(ebXmlDocument, "/Envelope/Header/MessageHeader/MessageData/MessageId"))
            .thenReturn("CBBAE92D-C7E8-4A9C-8887-F5AEBA1F8CE1");
        when(xPathService.getNodeValue(ebXmlDocument, "/Envelope/Body/Manifest/Reference[position()=2]/Description"))
            .thenReturn("Filename=blah");
    }

    @SneakyThrows
    private void prepareFragmentMocks(InboundMessage inboundMessage) {
        inboundMessage.setPayload(readXmlFile("inbound_message_payload_fragment_index.xml"));

        inboundMessage.setEbXML(readXmlFile("inbound_message_ebxml_fragment_index.xml"));
        inboundMessage.setAttachments(Arrays.asList(new InboundMessage.Attachment("xml/text", "Yes", "Filename=E39E79A2-FA96-48FF-9373"
            + "-7BBCB9D036E7_1.messageattachment ContentType=text/plain Compressed=No LargeAttachment=No OriginalBase64=Yes", "")));
        inboundMessage.getAttachments().get(0).setPayload("This is a payload");
        PatientMigrationRequest migrationRequest =
            PatientMigrationRequest.builder()
                .id(1)
                .losingPracticeOdsCode(LOSING_ODE_CODE)
                .winningPracticeOdsCode(WINNING_ODE_CODE)
                .build();

        when(patientMigrationRequestDao.getMigrationRequest(CONVERSATION_ID)).thenReturn(migrationRequest);

        when(xPathService.parseDocumentFromXml(inboundMessage.getEbXML())).thenReturn(ebXmlDocument);
        when(xPathService.getNodeValue(ebXmlDocument, "/Envelope/Header/MessageHeader/MessageData/MessageId")).thenReturn("CBBAE92D-C7E8"
            + "-4A9C-8887-F5AEBA1F8CE1").thenReturn("047C22B4-613F-47D3-9A72-44A1758464FB");
    }

    @SneakyThrows
    private String readXmlFile(String file) {
        return readResourceAsString("/xml/COPCMessageHandler/" + file);
    }
}

