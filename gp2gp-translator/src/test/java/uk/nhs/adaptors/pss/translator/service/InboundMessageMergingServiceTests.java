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
import uk.nhs.adaptors.pss.translator.storage.StorageManagerService;
import uk.nhs.adaptors.pss.translator.util.XmlParseUtilService;

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
    @InjectMocks
    private InboundMessageMergingService inboundMessageMergingService;

    //mocks without skeleton
    @SneakyThrows
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
    }

    private void prepareSkeletonMocks(InboundMessage inboundMessage) throws SAXException, JAXBException, JsonProcessingException {

        inboundMessage.setPayload("payload");
        Bundle bundle = new Bundle();
        bundle.setId("Test");
        inboundMessage.setPayload(readInboundMessagePayloadFromFile());

        inboundMessage.setEbXML(readInboundMessageEbXmlFromFile());


        //var inboundMessage = objectMapper.readValue(migrationRequest.getInboundMessage(), InboundMessage.class);

        when(migrationRequestDao.getMigrationRequest(any())).thenReturn(PatientMigrationRequest.builder().build());


        when(migrationRequest.getInboundMessage()).thenReturn(any());

        when(objectMapper.readValue((String) any(), InboundMessage.class)).thenReturn(inboundMessage);


        FILE_AS_BYTES = readInboundMessagePayloadFromFile().getBytes(StandardCharsets.UTF_8);

        when(attachmentHandlerService.getAttachment(FILENAME)).thenReturn(FILE_AS_BYTES);

        var reference = new EbxmlReference("First instance is always a payload", "mid:1", "docId");
        var ebXmlAttachments = Arrays.asList(reference);

//        RCMRIN030000UK06Message payload = unmarshallString(inboundMessage.getPayload(),getPayload RCMRIN030000UK06Message.class);

        when(xmlParseUtilService.getEbxmlAttachmentsData(inboundMessage)).thenReturn(ebXmlAttachments);

        when(xPathService.parseDocumentFromXml(any())).thenReturn(ebXmlDocument);



        Node nodeChild = new Node() {
            @Override
            public String getNodeName() {
                return null;
            }

            @Override
            public String getNodeValue() throws DOMException {
                return null;
            }

            @Override
            public void setNodeValue(String nodeValue) throws DOMException {

            }

            @Override
            public short getNodeType() {
                return 0;
            }

            @Override
            public Node getParentNode() {
                return null;
            }

            @Override
            public NodeList getChildNodes() {
                return null;
            }

            @Override
            public Node getFirstChild() {
                return null;
            }

            @Override
            public Node getLastChild() {
                return null;
            }

            @Override
            public Node getPreviousSibling() {
                return null;
            }

            @Override
            public Node getNextSibling() {
                return null;
            }

            @Override
            public NamedNodeMap getAttributes() {
                return null;
            }

            @Override
            public Document getOwnerDocument() {
                return null;
            }

            @Override
            public Node insertBefore(Node newChild, Node refChild) throws DOMException {
                return null;
            }

            @Override
            public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
                return null;
            }

            @Override
            public Node removeChild(Node oldChild) throws DOMException {
                return null;
            }

            @Override
            public Node appendChild(Node newChild) throws DOMException {
                return null;
            }

            @Override
            public boolean hasChildNodes() {
                return false;
            }

            @Override
            public Node cloneNode(boolean deep) {
                return null;
            }

            @Override
            public void normalize() {

            }

            @Override
            public boolean isSupported(String feature, String version) {
                return false;
            }

            @Override
            public String getNamespaceURI() {
                return null;
            }

            @Override
            public String getPrefix() {
                return null;
            }

            @Override
            public void setPrefix(String prefix) throws DOMException {

            }

            @Override
            public String getLocalName() {
                return null;
            }

            @Override
            public boolean hasAttributes() {
                return false;
            }

            @Override
            public String getBaseURI() {
                return null;
            }

            @Override
            public short compareDocumentPosition(Node other) throws DOMException {
                return 0;
            }

            @Override
            public String getTextContent() throws DOMException {
                return null;
            }

            @Override
            public void setTextContent(String textContent) throws DOMException {

            }

            @Override
            public boolean isSameNode(Node other) {
                return false;
            }

            @Override
            public String lookupPrefix(String namespaceURI) {
                return null;
            }

            @Override
            public boolean isDefaultNamespace(String namespaceURI) {
                return false;
            }

            @Override
            public String lookupNamespaceURI(String prefix) {
                return null;
            }

            @Override
            public boolean isEqualNode(Node arg) {
                return false;
            }

            @Override
            public Object getFeature(String feature, String version) {
                return null;
            }

            @Override
            public Object setUserData(String key, Object data, UserDataHandler handler) {
                return null;
            }

            @Override
            public Object getUserData(String key) {
                return null;
            }
        };

        Node node = new Node() {
            @Override
            public String getNodeName() {
                return null;
            }

            @Override
            public String getNodeValue() throws DOMException {
                return null;
            }

            @Override
            public void setNodeValue(String nodeValue) throws DOMException {

            }

            @Override
            public short getNodeType() {
                return 0;
            }

            @Override
            public Node getParentNode() {
                return nodeChild;
            }

            @Override
            public NodeList getChildNodes() {
                return null;
            }

            @Override
            public Node getFirstChild() {
                return null;
            }

            @Override
            public Node getLastChild() {
                return null;
            }

            @Override
            public Node getPreviousSibling() {
                return null;
            }

            @Override
            public Node getNextSibling() {
                return null;
            }

            @Override
            public NamedNodeMap getAttributes() {
                return null;
            }

            @Override
            public Document getOwnerDocument() {
                return null;
            }

            @Override
            public Node insertBefore(Node newChild, Node refChild) throws DOMException {
                return null;
            }

            @Override
            public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
                return null;
            }

            @Override
            public Node removeChild(Node oldChild) throws DOMException {
                return null;
            }

            @Override
            public Node appendChild(Node newChild) throws DOMException {
                return null;
            }

            @Override
            public boolean hasChildNodes() {
                return false;
            }

            @Override
            public Node cloneNode(boolean deep) {
                return null;
            }

            @Override
            public void normalize() {

            }

            @Override
            public boolean isSupported(String feature, String version) {
                return false;
            }

            @Override
            public String getNamespaceURI() {
                return null;
            }

            @Override
            public String getPrefix() {
                return null;
            }

            @Override
            public void setPrefix(String prefix) throws DOMException {

            }

            @Override
            public String getLocalName() {
                return null;
            }

            @Override
            public boolean hasAttributes() {
                return false;
            }

            @Override
            public String getBaseURI() {
                return null;
            }

            @Override
            public short compareDocumentPosition(Node other) throws DOMException {
                return 0;
            }

            @Override
            public String getTextContent() throws DOMException {
                return null;
            }

            @Override
            public void setTextContent(String textContent) throws DOMException {

            }

            @Override
            public boolean isSameNode(Node other) {
                return false;
            }

            @Override
            public String lookupPrefix(String namespaceURI) {
                return null;
            }

            @Override
            public boolean isDefaultNamespace(String namespaceURI) {
                return false;
            }

            @Override
            public String lookupNamespaceURI(String prefix) {
                return null;
            }

            @Override
            public boolean isEqualNode(Node arg) {
                return false;
            }

            @Override
            public Object getFeature(String feature, String version) {
                return null;
            }

            @Override
            public Object setUserData(String key, Object data, UserDataHandler handler) {
                return null;
            }

            @Override
            public Object getUserData(String key) {
                return null;
            }
        };

/*        when(xPathService.getNodes(any(), any()))
                .thenReturn(new NodeList() {
                    @Override
                    public Node item(int index) {
                        return nodeChild;
                    }

                    @Override
                    public int getLength() {
                        return 0;
                    }
                });*/

        when(xPathService.getNodes(any(), any()))
                .thenReturn(nodeList);



        when(nodeList.item(0)).thenReturn(node);

        when(nodeList.item(1)).thenReturn(node);

        when(ebXmlDocument.getElementsByTagName("*")).thenReturn(nodeList);

        //when(node.getOwnerDocument()).thenReturn(ebXmlDocument);
        //when(any()).thenReturn(node);



//        when(xPathService.parseDocumentFromXml(inboundMessage.getPayload())).thenReturn(null);

//         var nodeList = new ArrayList<Node>();
//         when(xPathService.getNodes(any(), any())).thenReturn((NodeList) nodeList);
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
        prepareMocks(inboundMessage);

        // no need for skeleton here
        var attachments = createPatientAttachmentList(true, false);
        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID)).thenReturn(attachments);

        inboundMessageMergingService.mergeAndBundleMessage(CONVERSATION_ID);

        //Check it doesn't hit business logic specifically for skeleton message
        verify(attachmentHandlerService, never()).getAttachment(FILENAME);
    }

    @Test
    public void When_SkeletonMessage_Expect_InboundMessageMerge() throws SAXException, BundleMappingException, JAXBException, JsonProcessingException {
        var attachments = createPatientAttachmentList(true, true);
        var inboundMessage = new InboundMessage();
        inboundMessage.setPayload(readInboundMessagePayloadFromFile());
        inboundMessage.setEbXML(readInboundMessageEbXmlFromFile());
        var inboundMessageId = xPathService.getNodeValue(ebXmlDocument, "/Envelope/Header/MessageHeader/MessageData/MessageId");

        prepareMocks(inboundMessage);
        prepareSkeletonMocks(inboundMessage);

        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID)).thenReturn(attachments);

        //inboundMessageMergingService.mergeAndBundleMessage(CONVERSATION_ID);

        // verify( file "got" once )
        // verify bundle inbound message contains file txt instead of narrative statement
        ArgumentCaptor<RCMRIN030000UK06Message> argument = ArgumentCaptor.forClass(RCMRIN030000UK06Message.class);
        verify(bundleMapperService).mapToBundle(argument.capture(), any());
        var payload = argument.getValue();
        // todo: pull what we need out of payload and check it has contents of relevant extract in
        // var result = payload. ????
        // assert(result.contains(textFromExtract))

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
        prepareMocks(inboundMessage);

        // no need for skeleton here
        var attachments = createPatientAttachmentList(true, false);
        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID)).thenReturn(attachments);

        inboundMessageMergingService.mergeAndBundleMessage(CONVERSATION_ID);

        verify(attachmentReferenceUpdaterService)
                .updateReferenceToAttachment(inboundMessage.getAttachments(), CONVERSATION_ID, inboundMessage.getPayload());
    }

    //happy path when no errors
    //not Skelleton
    @Test
    public void When_HappyPath_Expect_ThrowNoErrors() throws JAXBException, JsonProcessingException, SAXException {
        var inboundMessage = new InboundMessage();
        prepareSkeletonMocks(inboundMessage);

        var attachments = createPatientAttachmentList(true, true);
        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID)).thenReturn(attachments);
//      when(attachmentHandlerService.getAttachment(FILENAME)).thenReturn(new byte[10]);

        inboundMessageMergingService.mergeAndBundleMessage(CONVERSATION_ID);

        // should include skeleton message - rest of skeleton related stuff needs to be mocked
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
