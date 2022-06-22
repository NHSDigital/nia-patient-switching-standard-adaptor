package uk.nhs.adaptors.pss.translator.service;

import static java.util.UUID.randomUUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import software.amazon.ion.NullValueException;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.EbxmlReference;
import uk.nhs.adaptors.pss.translator.util.XmlParseUtilService;

@ExtendWith(MockitoExtension.class)
public class SkeletonProcessingServiceTests {

    private static final String CONVERSATION_ID = randomUUID().toString();
    private static final String NHS_NUMBER = "1111";
    private static final String FILENAME = "test_main.txt";

    @Mock
    private NodeList nodeList;
    @Mock
    private Node node;
    @Mock
    private PatientAttachmentLogService patientAttachmentLogService;
    @Mock
    private PatientMigrationRequestDao migrationRequestDao;
    @Mock
    private PatientMigrationRequest migrationRequest;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private BundleMapperService bundleMapperService;
    @Mock
    private XPathService xPathService;
    @Mock
    private Document ebXmlDocument;
    @Mock
    private InboundMessage inboundMessageMock;
    @Mock
    private Document document;

    @Mock
    private AttachmentHandlerService attachmentHandlerService;
    @Mock
    private XmlParseUtilService xmlParseUtilService;
    @Mock
    private MigrationStatusLogService migrationStatusLogService;

    @InjectMocks
    private SkeletonProcessingService skeletonProcessingService;

    @SneakyThrows
    private void prepareNonRCMRMocks(InboundMessage inboundMessage) {
        var inboundMessageAsString = objectMapper.writeValueAsString(inboundMessage);
        var patientMigrationRequest = PatientMigrationRequest
                .builder()
                .inboundMessage(inboundMessageAsString)
                .build();

        prepareSkeletonNonRCMRMocks(inboundMessage);
    }

    @SneakyThrows
    private void prepareRCMRMocks(InboundMessage inboundMessage) {
        var inboundMessageAsString = objectMapper.writeValueAsString(inboundMessage);
        var patientMigrationRequest = PatientMigrationRequest
            .builder()
            .inboundMessage(inboundMessageAsString)
            .build();

        prepareSkeletonRCMRMocks(inboundMessage);
    }

    private void prepareSkeletonNonRCMRMocks(InboundMessage inboundMessage) throws SAXException, TransformerException {

        var reference = new EbxmlReference("First instance is always a payload", "mid:1", "docId");
        var ebXmlAttachments = Arrays.asList(reference);
        var fileAsBytes = readInboundMessageSkeletonPayloadFromFile().getBytes(StandardCharsets.UTF_8);
        when(attachmentHandlerService.getAttachment(any(), any())).thenReturn(fileAsBytes);
        when(xmlParseUtilService.getEbxmlAttachmentsData(any())).thenReturn(ebXmlAttachments);
        when(xPathService.parseDocumentFromXml(any())).thenReturn(ebXmlDocument);
        when(xPathService.getNodes(any(), any())).thenReturn(nodeList);
        when(nodeList.item(0)).thenReturn(node);
        when(ebXmlDocument.getElementsByTagName("*")).thenReturn(nodeList);
        when(xmlParseUtilService.getStringFromDocument(any())).thenReturn(inboundMessage.getPayload());
        when(node.getOwnerDocument()).thenReturn(ebXmlDocument);
        when(node.getParentNode()).thenReturn(node);
    }

    private void prepareSkeletonRCMRMocks(InboundMessage inboundMessage) throws SAXException, TransformerException {

        var reference = new EbxmlReference("First instance is always a payload", "mid:1", "docId");
        var ebXmlAttachments = Arrays.asList(reference);
        var fileAsBytes = readInboundMessagePayloadFromFile().getBytes(StandardCharsets.UTF_8);
        when(xmlParseUtilService.getStringFromDocument(any())).thenReturn(inboundMessage.getPayload());
        when(attachmentHandlerService.getAttachment(any(), any())).thenReturn(fileAsBytes);
    }

    @Test
    public void When_UpdateInboundMessageAttachmentHandlerServiceThrowsNullException_Expect_ThrowsNullValueException() throws JAXBException, JsonProcessingException, TransformerException,
        SAXException {
        var inboundMessage = new InboundMessage();
        var attachmentLog = createSkeletonPatientAttachmentLog();

        inboundMessage.setPayload(readInboundMessagePayloadFromFile());
        inboundMessage.setEbXML(readInboundMessageEbXmlFromFile());

        doThrow(NullValueException.class).when(attachmentHandlerService).getAttachment(any(), any());

        assertThrows(NullValueException.class, () ->
            skeletonProcessingService.updateInboundMessageWithSkeleton(attachmentLog, inboundMessage, migrationRequest.getConversationId()));
    }

    @Test
    public void When_HappyPathWithSkeletonAsRCMRMessage_Expect_ThrowNoErrors() throws TransformerException,
        SAXException {
        var inboundMessage = new InboundMessage();
        var attachmentLog = createSkeletonPatientAttachmentLog();

        inboundMessage.setPayload(readInboundMessagePayloadFromFile());
        inboundMessage.setEbXML(readInboundMessageEbXmlFromFile());

        prepareRCMRMocks(inboundMessage);

        skeletonProcessingService.updateInboundMessageWithSkeleton(attachmentLog, inboundMessage, CONVERSATION_ID);
    }

    @Test
    public void When_HappyPathWithSkeletonAsRCMRMessage_Expect_InboundMessagePayloadIsNewRCMRMessage() throws JAXBException, JsonProcessingException, TransformerException,
        SAXException {
        var inboundMessage = new InboundMessage();
        var attachmentLog = createSkeletonPatientAttachmentLog();

        inboundMessage.setPayload(readInboundMessagePayloadFromFile());
        inboundMessage.setEbXML(readInboundMessageEbXmlFromFile());

        prepareRCMRMocks(inboundMessage);

        var newInboundMessage =
            skeletonProcessingService.updateInboundMessageWithSkeleton(attachmentLog, inboundMessage, CONVERSATION_ID);

        assertEquals(newInboundMessage.getPayload(), readInboundMessagePayloadFromFile() );
    }

    @Test
    public void When_HappyPathWithSkeletonAsSectionMessage_Expect_ThrowNoErrors() throws JAXBException, JsonProcessingException, TransformerException,
        SAXException {
        var inboundMessage = new InboundMessage();
        var attachmentLog = createSkeletonPatientAttachmentLog();

        inboundMessage.setPayload(readInboundMessagePayloadFromFile());
        inboundMessage.setEbXML(readInboundMessageEbXmlFromFile());

        prepareNonRCMRMocks(inboundMessage);

        skeletonProcessingService.updateInboundMessageWithSkeleton(attachmentLog, inboundMessage, CONVERSATION_ID);
    }

    @Test
    public void When_SkeletonAsSectionMessage_Expect_ThrowNoErrors() throws JAXBException, JsonProcessingException, TransformerException,
        SAXException {
        var inboundMessage = new InboundMessage();
        var attachmentLog = createSkeletonPatientAttachmentLog();

        inboundMessage.setPayload(readInboundMessagePayloadFromFile());
        inboundMessage.setEbXML(readInboundMessageEbXmlFromFile());

        prepareNonRCMRMocks(inboundMessage);
        skeletonProcessingService.updateInboundMessageWithSkeleton(attachmentLog, inboundMessage, CONVERSATION_ID);
    }

    @Test
    public void When_SkeletonAsSectionMessageAndEBXMLSkeletonReferenceIsEmpty_Expect_ThrowTransformException() throws JAXBException, JsonProcessingException, TransformerException,
        SAXException {
        var inboundMessage = new InboundMessage();
        var attachmentLog = createSkeletonPatientAttachmentLog();

        inboundMessage.setPayload(readInboundMessagePayloadFromFile());
        inboundMessage.setEbXML(readInboundMessageEbXmlFromFile());

        var reference = new EbxmlReference("First instance is always a payload", "mid:1", "docId");
        var ebXmlAttachments = Arrays.asList(reference);
        var fileAsBytes = readInboundMessageSkeletonPayloadFromFile().getBytes(StandardCharsets.UTF_8);
        when(attachmentHandlerService.getAttachment(any(), any())).thenReturn(fileAsBytes);
        when(xmlParseUtilService.getEbxmlAttachmentsData(any())).thenReturn(ebXmlAttachments);
        when(xPathService.parseDocumentFromXml(any())).thenReturn(ebXmlDocument);

        var emptyAttachmentsData = new ArrayList<EbxmlReference>();
        when(xmlParseUtilService.getEbxmlAttachmentsData(any())).thenReturn(
            emptyAttachmentsData
        );

        assertThrows(TransformerException.class, () ->
            skeletonProcessingService.updateInboundMessageWithSkeleton(attachmentLog, inboundMessage, migrationRequest.getConversationId()));
    }

    private PatientAttachmentLog createSkeletonPatientAttachmentLog() {
        return
            PatientAttachmentLog.builder()
                    .filename(FILENAME)
                    .mid("1")
                    .orderNum(0)
                    .parentMid("0")
                    .uploaded(true)
                    .largeAttachment(true)
                    .base64(true)
                    .compressed(false)
                    .contentType("text/plain")
                    .lengthNum(0)
                    .skeleton(true)
                    .deleted(false)
                    .patientMigrationReqId(1)
                    .build();
    }

    @SneakyThrows
    private String readInboundMessagePayloadFromFile() {
        return readResourceAsString("/xml/inbound_message_payload.xml").replace("{{nhsNumber}}", NHS_NUMBER);
    }

    @SneakyThrows
    private String readInboundMessageSkeletonPayloadFromFile() {
        return readResourceAsString("/xml/inbound_message_skeleton_section_payload.xml").replace("{{nhsNumber}}", NHS_NUMBER);
    }

    @SneakyThrows
    private String readInboundMessageEbXmlFromFile() {
        return readResourceAsString("/xml/RCMRIN030000UK06_LARGE_MSG/ebxml.xml");
    }
}
