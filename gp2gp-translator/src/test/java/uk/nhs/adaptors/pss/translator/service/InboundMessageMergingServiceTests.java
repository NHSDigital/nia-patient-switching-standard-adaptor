package uk.nhs.adaptors.pss.translator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.exception.AttachmentNotFoundException;
import uk.nhs.adaptors.pss.translator.exception.BundleMappingException;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
                        .inboundMessage(INBOUND_MESSAGE_STRING)
                        .build();

        when(objectMapper.readValue(INBOUND_MESSAGE_STRING, InboundMessage.class)).thenReturn(inboundMessage);

        when(migrationRequestDao.getMigrationRequest(CONVERSATION_ID)).thenReturn(migrationRequest);
        when(bundleMapperService.mapToBundle(any(RCMRIN030000UK06Message.class), any())).thenReturn(bundle);
        when(attachmentHandlerService.buildInboundAttachmentsFromAttachmentLogs(any(), any())).thenReturn(inboundMessage.getAttachments());
        when(attachmentReferenceUpdaterService
                .updateReferenceToAttachment(
                        inboundMessage.getAttachments(), CONVERSATION_ID, inboundMessage.getPayload()
                )).thenReturn(inboundMessage.getPayload());
    }

    private void prepareSkeletonMocks(InboundMessage inboundMessage) throws SAXException {
        when(xPathService.parseDocumentFromXml(inboundMessage.getEbXML())).thenReturn(ebXmlDocument);
        when(xPathService.getNodes(ebXmlDocument, "/Envelope/Body/Manifest/Reference")).thenReturn(null);
    }

    @Test
    public void When_NotAllUploadsComplete_CanMergeCompleteBundle_Expect_ReturnFalse() throws JAXBException {
        var attachmentLogs = createPatientAttachmentList(false, false);
        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID)).thenReturn(attachmentLogs);

        var result = inboundMessageMergingService.canMergeCompleteBundle(CONVERSATION_ID);

        assertFalse(result);
    }

    @Test
    public void When_AllUploadsComplete_CanMergeCompleteBundle_Expect_ReturnTrue() throws JAXBException {
        var attachmentLogs = createPatientAttachmentList(true, true);
        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID)).thenReturn(attachmentLogs);

        var result = inboundMessageMergingService.canMergeCompleteBundle(CONVERSATION_ID);

        assertTrue(result);
    }

    @Test
    public void When_SkeletonMessage_Expect_InboundMessageMerge() throws SAXException, BundleMappingException {
        var attachments = createPatientAttachmentList(true, true);
        var inboundMessage = new InboundMessage();
        inboundMessage.setPayload(readInboundMessageFromFile());
        inboundMessage.setEbXML(readInboundMessageEbXmlFromFile());
        var inboundMessageId = xPathService.getNodeValue(ebXmlDocument, "/Envelope/Header/MessageHeader/MessageData/MessageId");

        prepareMocks(inboundMessage);
        prepareSkeletonMocks(inboundMessage);

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
    public void When_AttachmentsPresent_Expect_AttachmentReferenceUpdated()
            throws AttachmentNotFoundException, JAXBException, InlineAttachmentProcessingException {
        var inboundMessage = new InboundMessage();
        prepareMocks(inboundMessage);

        // no need for skeleton here
        var attachments = createPatientAttachmentList(true, false);
        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID)).thenReturn(attachments);

        inboundMessageMergingService.mergeAndBundleMessage(CONVERSATION_ID);

        verify(attachmentReferenceUpdaterService)
                .updateReferenceToAttachment(inboundMessage.getAttachments(), CONVERSATION_ID, inboundMessage.getPayload());
    }

    @Test
    public void When_HappyPath_Expect_ThrowNoErrors() {
        var inboundMessage = new InboundMessage();
        prepareMocks(inboundMessage);

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
