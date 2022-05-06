package uk.nhs.adaptors.pss.translator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.v3.COPCIN000001UK01Message;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uk.nhs.adaptors.connector.dao.MigrationStatusLogDao;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.MigrationStatusLog;
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.exception.AttachmentLogException;
import uk.nhs.adaptors.pss.translator.exception.AttachmentNotFoundException;
import uk.nhs.adaptors.pss.translator.exception.BundleMappingException;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.StringReader;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;

@ExtendWith(MockitoExtension.class)
public class InboundMessageMergingServiceTests {

    private static final String CONVERSATION_ID = randomUUID().toString();
    private static final String NHS_NUMBER = "1111";
    private static final String INBOUND_MESSAGE_STRING = "{hi i'm inbound message}";

    @Mock
    private PatientAttachmentLogService patientAttachmentLogService;
    @Mock
    private PatientMigrationRequestDao migrationRequestDao;
    @Mock
    private MigrationStatusLogService migrationStatusLogService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private BundleMapperService bundleMapperService;
    @Mock
    private XPathService xPathService;
    @Mock
    private Document ebXmlDocument;
    @Mock
    private AttachmentHandlerService attachmentHandlerService;
    @Mock
    private AttachmentReferenceUpdaterService attachmentReferenceUpdaterService;

    @InjectMocks
    private InboundMessageMergingService inboundMessageMergingService;

    @SneakyThrows
    private void prepareMocks(InboundMessage inboundMessage) {
        inboundMessage.setPayload("payload");
        Bundle bundle = new Bundle();
        bundle.setId("Test");
        inboundMessage.setPayload(readInboundMessageFromFile());

        inboundMessage.setEbXML(readInboundMessageEbXmlFromFile());

        PatientMigrationRequest migrationRequest =
                PatientMigrationRequest.builder()
                        .build();

        MigrationStatusLog migrationStatusLog =
                MigrationStatusLog.builder()
                        .date(OffsetDateTime.ofInstant(
                                Instant.now(),
                                ZoneId.systemDefault()))
                        .build();

        when(objectMapper.writeValueAsString(inboundMessage)).thenReturn(INBOUND_MESSAGE_STRING);

        when(migrationStatusLogService.getLatestMigrationStatusLog(CONVERSATION_ID)).thenReturn(migrationStatusLog);
        when(migrationRequestDao.getMigrationRequest(CONVERSATION_ID)).thenReturn(migrationRequest);
        when(xPathService.parseDocumentFromXml(inboundMessage.getEbXML())).thenReturn(ebXmlDocument);
        when(xPathService.getNodes(ebXmlDocument, "/Envelope/Body/Manifest/Reference")).thenReturn(null);
        when(bundleMapperService.mapToBundle(any(RCMRIN030000UK06Message.class), any())).thenReturn(bundle);
        when(attachmentHandlerService.buildInboundAttachmentsFromAttachmentLogs(any(), any())).thenReturn(new ArrayList<InboundMessage.Attachment>());
        when(attachmentReferenceUpdaterService
                .updateReferenceToAttachment(
                        inboundMessage.getAttachments(), CONVERSATION_ID, inboundMessage.getPayload()
                )).thenReturn(inboundMessage.getPayload());
    }

    @Test
    public void when_NotAllUploadsComplete_CanMergeCompleteBundle_Expect_ReturnFalse() throws BundleMappingException, AttachmentLogException, SAXException, JAXBException, AttachmentNotFoundException, JsonProcessingException, InlineAttachmentProcessingException {
        var attachmentLogs = createPatientAttachmentList(false, false);
        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID)).thenReturn(attachmentLogs);

        var result = inboundMessageMergingService.canMergeCompleteBundle(CONVERSATION_ID);

        assertFalse(result);
    }

    @Test
    public void when_AllUploadsComplete_CanMergeCompleteBundle_Expect_ReturnTrue() throws BundleMappingException, AttachmentLogException, SAXException, JAXBException, AttachmentNotFoundException, JsonProcessingException, InlineAttachmentProcessingException {
        var attachmentLogs = createPatientAttachmentList(true, true);
        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID)).thenReturn(attachmentLogs);

        var result = inboundMessageMergingService.canMergeCompleteBundle(CONVERSATION_ID);

        assertTrue(result);
    }

    @Test
    public void when_SkeletonMessage_Expect_FileMerge() throws AttachmentLogException, SAXException, ParserConfigurationException, BundleMappingException, JAXBException, AttachmentNotFoundException, JsonProcessingException, InlineAttachmentProcessingException {
        var attachments = createPatientAttachmentList(true, true);
        var inboundMessage = new InboundMessage();
        inboundMessage.setPayload(readInboundMessageFromFile());
        inboundMessage.setEbXML(readInboundMessageEbXmlFromFile());
        var inboundMessageId = xPathService.getNodeValue(ebXmlDocument, "/Envelope/Header/MessageHeader/MessageData/MessageId");


        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID)).thenReturn(attachments);

        inboundMessageMergingService.mergeAndBundleMessage(CONVERSATION_ID);

        // verify( file "got" once )
        // verify bundle inbound message contains file txt instead of narrative statement
        ArgumentCaptor<RCMRIN030000UK06Message> argument = ArgumentCaptor.forClass(RCMRIN030000UK06Message.class);
        verify(bundleMapperService).mapToBundle(argument.capture(), any());
        var payload = argument.getValue();
        // todo: pull what we need out of payload and check it has contents of relevant extract in
        // var result = payload. ????
        // assert(result.contains(textFromExtract))
    }

    @Test
    public void when_AttachmentsPresent_Expect_AttachmentReferenceUpdated() throws AttachmentNotFoundException, JAXBException, InlineAttachmentProcessingException, BundleMappingException, JsonProcessingException, AttachmentLogException, SAXException {
        var inboundMessage = new InboundMessage();
        prepareMocks(inboundMessage);

        // no need for skeleton here
        var attachments = createPatientAttachmentList(true, false);
        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID)).thenReturn(attachments);

        inboundMessageMergingService.mergeAndBundleMessage(CONVERSATION_ID);

        verify(attachmentReferenceUpdaterService).updateReferenceToAttachment(inboundMessage.getAttachments(), CONVERSATION_ID, inboundMessage.getPayload());
    }

    @Test
    public void when_HappyPath_Expect_ThrowNoErrors() throws AttachmentNotFoundException, JAXBException, BundleMappingException, JsonProcessingException, AttachmentLogException, SAXException, InlineAttachmentProcessingException {
        var inboundMessage = new InboundMessage();
        prepareMocks(inboundMessage);

        // no need for skeleton here
        var attachments = createPatientAttachmentList(true, true);
        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID)).thenReturn(attachments);

        inboundMessageMergingService.mergeAndBundleMessage(CONVERSATION_ID);
    }

    private ArrayList<PatientAttachmentLog> createPatientAttachmentList(Boolean isParentUploaded, Boolean isSkeleton) {
        var patientAttachmentLogs = new ArrayList<PatientAttachmentLog>();
        patientAttachmentLogs.add(
                PatientAttachmentLog.builder().filename("test_main.txt").mid("1")
                        .orderNum(0)
                        .parentMid("0")
                        .uploaded(isParentUploaded)
                        .largeAttachment(true)
                        .base64(true)
                        .compressed(false)
                        .contentType("text/plain")
                        .lengthNum(0)
                        .skeleton(isSkeleton)
                        .deleted(false)
                        .patientMigrationReqId(1).build()
        );
        return patientAttachmentLogs;
    }

    @SneakyThrows
    private String readInboundMessageFromFile() {
        return readResourceAsString("/xml/inbound_message_payload.xml").replace("{{nhsNumber}}", NHS_NUMBER);
    }

    @SneakyThrows
    private String readInboundMessageEbXmlFromFile() {
        return readResourceAsString("/xml/RCMRIN030000UK06_LARGE_MSG/ebxml.xml");
    }

}
