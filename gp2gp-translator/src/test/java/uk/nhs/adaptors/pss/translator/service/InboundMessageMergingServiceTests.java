package uk.nhs.adaptors.pss.translator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.util.StringUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.exception.AttachmentLogException;
import uk.nhs.adaptors.pss.translator.exception.AttachmentNotFoundException;
import uk.nhs.adaptors.pss.translator.exception.BundleMappingException;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.EbxmlReference;
import uk.nhs.adaptors.pss.translator.model.NACKReason;
import uk.nhs.adaptors.pss.translator.storage.StorageManagerService;
import uk.nhs.adaptors.pss.translator.util.XmlParseUtilService;
import uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationException;
import javax.xml.transform.TransformerException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

@ExtendWith(MockitoExtension.class)
public class InboundMessageMergingServiceTests {

    private static final String CONVERSATION_ID = randomUUID().toString();
    private static final String NHS_NUMBER = "1111";
    private static final String INBOUND_MESSAGE_STRING = "{hi i'm inbound message}";
    private static final String FILENAME = "test_main.txt";
    private static byte[] FILE_AS_BYTES;


    @Mock
    NodeList nodeList;
    @Mock
    Node node;
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
    private NackAckPreparationService nackAckPreparationService;
    @Mock
    private AttachmentHandlerService attachmentHandlerService;
    @Mock
    private AttachmentReferenceUpdaterService attachmentReferenceUpdaterService;
    @Mock
    private XmlParseUtilService xmlParseUtilService;
    @Mock
    private MigrationStatusLogService migrationStatusLogService;
    @Mock
    private FhirParser fhirParser;
    @Mock
    private StorageManagerService storageManagerService;
    @Mock
    private XmlUnmarshallUtil xmlUnmarshallUtil;
    @InjectMocks
    private InboundMessageMergingService inboundMessageMergingService;

    //mocks without skeleton
/*    @SneakyThrows
    private void prepareMocks(InboundMessage inboundMessage) {
        inboundMessage.setPayload("payload");
        Bundle bundle = new Bundle();
        bundle.setId("Test");
        inboundMessage.setPayload(readInboundMessagePayloadFromFile());

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
    }*/




    @SneakyThrows
    private void prepareMocks(InboundMessage inboundMessage,ArrayList<PatientAttachmentLog> attachments) {
        Bundle bundle = new Bundle();
        bundle.setId("Test");

        var inboundMessageAsString = objectMapper.writeValueAsString(inboundMessage);
        var patientMigrationRequest = PatientMigrationRequest
                .builder()
                .inboundMessage(inboundMessageAsString)
                .build();

        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID)).thenReturn(attachments);

        when(migrationRequestDao.getMigrationRequest(any())).thenReturn(patientMigrationRequest);
        when(objectMapper.readValue(inboundMessageAsString, InboundMessage.class)).thenReturn(inboundMessage);
        when(attachmentReferenceUpdaterService.updateReferenceToAttachment(any(), any(), any())).thenReturn(inboundMessage.getPayload());

        if(attachments.get(0).getSkeleton()) {
            prepareSkeletonMocks(inboundMessage);
        }
    }

    private void prepareSkeletonMocks(InboundMessage inboundMessage) throws SAXException, JAXBException, JsonProcessingException, TransformerException, AttachmentNotFoundException, InlineAttachmentProcessingException {

        var reference = new EbxmlReference("First instance is always a payload", "mid:1", "docId");
        var ebXmlAttachments = Arrays.asList(reference);
        FILE_AS_BYTES = readInboundMessagePayloadFromFile().getBytes(StandardCharsets.UTF_8);

        when(attachmentHandlerService.getAttachment(FILENAME)).thenReturn(FILE_AS_BYTES);
        when(xmlParseUtilService.getEbxmlAttachmentsData(inboundMessage)).thenReturn(ebXmlAttachments);
        when(xPathService.parseDocumentFromXml(any())).thenReturn(ebXmlDocument);
        when(xPathService.getNodes(any(), any())).thenReturn(nodeList);

        when(nodeList.item(0)).thenReturn(node);
        when(nodeList.item(1)).thenReturn(node);
        when(ebXmlDocument.getElementsByTagName("*")).thenReturn(nodeList);
        when(xmlParseUtilService.getStringFromDocument(any())).thenReturn(inboundMessage.getPayload());

        when(node.getOwnerDocument()).thenReturn(ebXmlDocument);
        when(node.getParentNode()).thenReturn(node);

    }

    @Test
    public void When_HappyPathWithSkeleton_Expect_ThrowNoErrors() throws JAXBException, JsonProcessingException, SAXException, TransformerException, AttachmentNotFoundException, InlineAttachmentProcessingException {
        var inboundMessage = new InboundMessage();

        inboundMessage.setPayload("payload");
        inboundMessage.setPayload(readInboundMessagePayloadFromFile()); //change
        inboundMessage.setEbXML(readInboundMessageEbXmlFromFile()); //change

        var attachments = createPatientAttachmentList(true, true);

        prepareMocks(inboundMessage, attachments);

        inboundMessageMergingService.mergeAndBundleMessage(CONVERSATION_ID);

        verify(nackAckPreparationService, never()).sendNackMessage((NACKReason) any(), (RCMRIN030000UK06Message) any(), any());
        verify(migrationStatusLogService, times(1)).updatePatientMigrationRequestAndAddMigrationStatusLog(any(), any(), any(), any());
    }

    @Test
    public void When_HappyPathWithNoSkeleton_Expect_ThrowNoErrors() throws JAXBException, JsonProcessingException, SAXException, TransformerException, AttachmentNotFoundException, InlineAttachmentProcessingException {
        var inboundMessage = new InboundMessage();
        var attachments = createPatientAttachmentList(true, true);

        prepareMocks(inboundMessage, attachments);

        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID)).thenReturn(attachments);

        inboundMessageMergingService.mergeAndBundleMessage(CONVERSATION_ID);

        verify(nackAckPreparationService, never()).sendNackMessage((NACKReason) any(), (RCMRIN030000UK06Message) any(), any());
        verify(migrationStatusLogService, times(1)).updatePatientMigrationRequestAndAddMigrationStatusLog(any(), any(), any(), any());
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
    public void When_CanMergeCompleteBundleHasNullOrEmptyParams_Expect_ThrowIllegalStateException() {

        var conversationId = "";
        assertThrows(ValidationException.class, () -> inboundMessageMergingService.canMergeCompleteBundle(conversationId));

    }

    @Test
    public void When_MergeAndBundleMessageHasNullOrEmptyParams_Expect_ThrowIllegalStateException() {

        var conversationId = "";
        assertThrows(ValidationException.class, () -> inboundMessageMergingService.mergeAndBundleMessage(conversationId));

    }

    @Test
    public void When_NotSkeletonMessage_Expect_NotToGetAttachmentFromService()
            throws AttachmentNotFoundException, JAXBException, InlineAttachmentProcessingException, JsonProcessingException {
        var inboundMessage = new InboundMessage();

        // no need for skeleton here
        var attachments = createPatientAttachmentList(true, false);

        prepareMocks(inboundMessage, attachments);

        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID)).thenReturn(attachments);

        inboundMessageMergingService.mergeAndBundleMessage(CONVERSATION_ID);

        //Check it doesn't hit business logic specifically for skeleton message
        verify(attachmentHandlerService, never()).getAttachment(FILENAME);
    }

    @Test
    public void When_SkeletonMessage_Expect_InboundMessageMerge() throws SAXException, BundleMappingException, JAXBException, JsonProcessingException, TransformerException, AttachmentNotFoundException, InlineAttachmentProcessingException {
        var attachments = createPatientAttachmentList(true, true);
        var inboundMessage = new InboundMessage();
        inboundMessage.setPayload(readInboundMessagePayloadFromFile());
        inboundMessage.setEbXML(readInboundMessageEbXmlFromFile());
        var inboundMessageId = xPathService.getNodeValue(ebXmlDocument, "/Envelope/Header/MessageHeader/MessageData/MessageId");

        prepareMocks(inboundMessage, attachments);

        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID)).thenReturn(attachments);

        //true/false
        verify(migrationStatusLogService).updatePatientMigrationRequestAndAddMigrationStatusLog(CONVERSATION_ID, any(), any(), any());
    }

    // test attachmentsContainSkeletonMessage if true/false

    //CATCH (NEEDS TO BE IN COPC)
    //Test NAcks Send
    //         doThrow(new DataFormatException()).when(fhirParser).encodeToJson(bundle);


        //AttachmentNotFoundException |
        //BundleMappingException |
        //JsonProcessingException |
        //JAXBException |
        //TransformerException |
        //InlineAttachmentProcessingException |
        //SAXException ex


        ////ValidationException |  JsonMappingException |

    @Test
    public void When_AttachmentsPresent_Expect_AttachmentReferenceUpdated()
            throws AttachmentNotFoundException, JAXBException, InlineAttachmentProcessingException, JsonProcessingException {
        var inboundMessage = new InboundMessage();

        var attachments = createPatientAttachmentList(true, false);

        prepareMocks(inboundMessage, attachments);

        // no need for skeleton here
        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID)).thenReturn(attachments);

        inboundMessageMergingService.mergeAndBundleMessage(CONVERSATION_ID);

        verify(attachmentReferenceUpdaterService)
                .updateReferenceToAttachment(inboundMessage.getAttachments(), CONVERSATION_ID, inboundMessage.getPayload());
    }



    //test happy path with IF and without IF (with extract and without extract)

    private ArrayList<PatientAttachmentLog> createPatientAttachmentList(Boolean isParentUploaded, Boolean isSkeleton) {
        var patientAttachmentLogs = new ArrayList<PatientAttachmentLog>();
        patientAttachmentLogs.add(
                PatientAttachmentLog.builder().filename(FILENAME).mid("1")
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
    private String readInboundMessagePayloadFromFile() {
        return readResourceAsString("/xml/inbound_message_payload.xml").replace("{{nhsNumber}}", NHS_NUMBER);
    }

    @SneakyThrows
    private String readInboundMessageEbXmlFromFile() {
        return readResourceAsString("/xml/RCMRIN030000UK06_LARGE_MSG/ebxml.xml");
    }

}
