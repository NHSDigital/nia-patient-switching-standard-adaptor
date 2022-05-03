package uk.nhs.adaptors.pss.translator.task;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;


import org.xml.sax.SAXException;

import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.exception.AttachmentLogException;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.service.AttachmentHandlerService;
import uk.nhs.adaptors.pss.translator.service.XPathService;

import org.w3c.dom.Document;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;

import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.bind.ValidationException;

@ExtendWith(MockitoExtension.class)
public class COPCMessageHandlerTest {

    private static final String NHS_NUMBER = "123456";
    private static final String CONVERSATION_ID = randomUUID().toString();
    private static final String REFERENCES_ATTACHMENTS_PATH = "/Envelope/Body/Manifest/Reference";


    @Mock
    private PatientAttachmentLogService patientAttachmentLogService;

    @Mock
    private AttachmentHandlerService attachmentHandlerService;

    @Mock
    private XPathService xPathService;

    @Mock
    private Document ebXmlDocument;

    @Mock
    private InboundMessage inboundMessage;

    @Mock
    private PatientAttachmentLog message;

    @InjectMocks
    private COPCMessageHandler copcMessageHandler;

    @Test
    public void When_HappyPath_ThrowNoErrors() throws SAXException, ValidationException, AttachmentLogException, InlineAttachmentProcessingException {
        var inboundMessage = new InboundMessage();
        inboundMessage.setPayload(readInboundMessageFromFile());
        inboundMessage.setEbXML(readLargeInboundMessageEbXmlFromFile());
        var inboundMessageId = xPathService.getNodeValue(ebXmlDocument, "/Envelope/Header/MessageHeader/MessageData/MessageId");

        when(patientAttachmentLogService.findAttachmentLog(inboundMessageId, CONVERSATION_ID))
            .thenReturn(PatientAttachmentLog.builder().filename("test_frag_1.txt").mid("2").parentMid("1").patientMigrationReqId(1).build());

        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID))
            .thenReturn(new ArrayList<PatientAttachmentLog>(
                Arrays.asList(
                    PatientAttachmentLog.builder().filename("test_main.txt").mid("1")
                            .orderNum(0)
                            .parentMid("0")
                            .uploaded(false)
                            .largeAttachment(true)
                            .base64(true)
                            .compressed(false)
                            .contentType("textplain")
                            .lengthNum(0)
                            .skeleton(false)
                            .patientMigrationReqId(1).build(),
                    PatientAttachmentLog.builder().filename("test_frag_1.txt")
                            .mid("2")
                            .orderNum(0)
                            .parentMid("1")
                            .uploaded(true)
                            .largeAttachment(true)
                            .base64(true)
                            .compressed(false)
                            .contentType("1textplain")
                            .lengthNum(0)
                            .skeleton(false)
                            .patientMigrationReqId(1).build(),
                    PatientAttachmentLog.builder().filename("test_frag_2.txt")
                            .mid("3")
                            .orderNum(1)
                            .parentMid("1")
                            .uploaded(true)
                            .patientMigrationReqId(1)
                            .largeAttachment(true)
                            .base64(true)
                            .compressed(false)
                            .contentType("textplain")
                            .lengthNum(0)
                            .skeleton(false).build()
                )));

        when(attachmentHandlerService.buildSingleFileStringFromPatientAttachmentLogs(any())).thenReturn("fgnujansdflsnglawnmflae");
        copcMessageHandler.checkAndMergeFileParts(inboundMessage, CONVERSATION_ID);
    }

    @Test
    public void When_CurrentAttachmentLogIsNull_Expect_ThrowError() throws SAXException {
        var inboundMessage = new InboundMessage();
        inboundMessage.setPayload(readInboundMessageFromFile());
        inboundMessage.setEbXML(readLargeInboundMessageEbXmlFromFile());

        when(patientAttachmentLogService.findAttachmentLog(any(), any()))
                .thenReturn(null);

        assertThrows(AttachmentLogException.class, () -> copcMessageHandler.checkAndMergeFileParts(inboundMessage, any()));
    }

    @Test
    public void When_CurrentAttachmentLogExists_Expect_ThrowNoError() throws ValidationException, SAXException, AttachmentLogException, InlineAttachmentProcessingException {
        var inboundMessage = new InboundMessage();
        inboundMessage.setPayload(readInboundMessageFromFile());
        inboundMessage.setEbXML(readLargeInboundMessageEbXmlFromFile());
        var inboundMessageId = xPathService.getNodeValue(ebXmlDocument, "/Envelope/Header/MessageHeader/MessageData/MessageId");

        when(patientAttachmentLogService.findAttachmentLog(inboundMessageId, CONVERSATION_ID))
                .thenReturn(PatientAttachmentLog.builder().filename("test_frag_1.txt").mid("2").parentMid("1").patientMigrationReqId(1).build());

        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID))
                .thenReturn(new ArrayList<PatientAttachmentLog>(
                        Arrays.asList(
                                PatientAttachmentLog.builder().filename("test_main.txt").mid("1")
                                        .orderNum(0)
                                        .parentMid("0")
                                        .uploaded(false)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false)
                                        .patientMigrationReqId(1).build(),
                                PatientAttachmentLog.builder().filename("test_frag_1.txt")
                                        .mid("2")
                                        .orderNum(0)
                                        .parentMid("1")
                                        .uploaded(true)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("1textplain")
                                        .lengthNum(0)
                                        .skeleton(false)
                                        .patientMigrationReqId(1).build(),
                                PatientAttachmentLog.builder().filename("test_frag_2.txt")
                                        .mid("3")
                                        .orderNum(1)
                                        .parentMid("1")
                                        .uploaded(true)
                                        .patientMigrationReqId(1)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false).build()
                        )));

        copcMessageHandler.checkAndMergeFileParts(inboundMessage, CONVERSATION_ID);

        verify(patientAttachmentLogService, times(1)).findAttachmentLogs(CONVERSATION_ID);
    }

    @Test
    public void When_fragmentsAreMissingOrUploaded_Expect_NotDeleteFragments() throws ValidationException, SAXException, AttachmentLogException, InlineAttachmentProcessingException {

        var inboundMessage = new InboundMessage();
        inboundMessage.setPayload(readInboundMessageFromFile());
        inboundMessage.setEbXML(readLargeInboundMessageEbXmlFromFile());
        var inboundMessageId = xPathService.getNodeValue(ebXmlDocument, "/Envelope/Header/MessageHeader/MessageData/MessageId");

        when(patientAttachmentLogService.findAttachmentLog(inboundMessageId, CONVERSATION_ID))
                .thenReturn(PatientAttachmentLog.builder().filename("test_frag_1.txt").mid("2").parentMid("1").patientMigrationReqId(1).build());

        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID))
                .thenReturn(new ArrayList<PatientAttachmentLog>(
                        Arrays.asList(
                                PatientAttachmentLog.builder().filename("test_main.txt").mid("1")
                                        .orderNum(0)
                                        .parentMid("0")
                                        .uploaded(false)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false)
                                        .patientMigrationReqId(1).build(),
                                PatientAttachmentLog.builder().filename("test_frag_1.txt")
                                        .mid("2")
                                        .orderNum(0)
                                        .parentMid("1")
                                        .uploaded(false)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false)
                                        .patientMigrationReqId(1).build(),
                                PatientAttachmentLog.builder().filename("test_frag_2.txt")
                                        .mid("3")
                                        .orderNum(1)
                                        .parentMid("1")
                                        .uploaded(true)
                                        .patientMigrationReqId(1)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false).build()
                        )));

        copcMessageHandler.checkAndMergeFileParts(inboundMessage, CONVERSATION_ID);

        verify(attachmentHandlerService, never()).buildSingleFileStringFromPatientAttachmentLogs(any());
    }

    @Test
    public void When_CheckByteCompilationCreatesFileAsExpected_Expect_RunWithNoErrors() throws ValidationException, SAXException, AttachmentLogException, InlineAttachmentProcessingException {
        var inboundMessage = new InboundMessage();
        inboundMessage.setPayload(readInboundMessageFromFile());
        inboundMessage.setEbXML(readLargeInboundMessageEbXmlFromFile());
        var inboundMessageId = xPathService.getNodeValue(ebXmlDocument, "/Envelope/Header/MessageHeader/MessageData/MessageId");

        when(patientAttachmentLogService.findAttachmentLog(inboundMessageId, CONVERSATION_ID))
                .thenReturn(PatientAttachmentLog.builder().filename("test_frag_1.txt").mid("2").parentMid("1").patientMigrationReqId(1).build());

        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID))
                .thenReturn(new ArrayList<PatientAttachmentLog>(
                        Arrays.asList(
                                PatientAttachmentLog.builder().filename("test_main.txt").mid("1")
                                        .orderNum(0)
                                        .parentMid("0")
                                        .uploaded(false)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false)
                                        .patientMigrationReqId(1).build(),
                                PatientAttachmentLog.builder().filename("test_frag_1.txt")
                                        .mid("2")
                                        .orderNum(0)
                                        .parentMid("1")
                                        .uploaded(true)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false)
                                        .patientMigrationReqId(1).build(),
                                PatientAttachmentLog.builder().filename("test_frag_2.txt")
                                        .mid("3")
                                        .orderNum(1)
                                        .parentMid("1")
                                        .uploaded(true)
                                        .patientMigrationReqId(1)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false).build()
                        )));


        copcMessageHandler.checkAndMergeFileParts(inboundMessage, CONVERSATION_ID);
        verify(attachmentHandlerService, times(1)).buildSingleFileStringFromPatientAttachmentLogs(any());

    }

    @Test
    public void When_EnsureStoreAttachmentsIsCalled_Expect_RunWithNoErrors() throws ValidationException, SAXException, AttachmentLogException, InlineAttachmentProcessingException {
        var inboundMessage = new InboundMessage();
        inboundMessage.setPayload(readInboundMessageFromFile());
        inboundMessage.setEbXML(readLargeInboundMessageEbXmlFromFile());
        var inboundMessageId = xPathService.getNodeValue(ebXmlDocument, "/Envelope/Header/MessageHeader/MessageData/MessageId");

        when(patientAttachmentLogService.findAttachmentLog(inboundMessageId, CONVERSATION_ID))
                .thenReturn(PatientAttachmentLog.builder().filename("test_frag_1.txt").mid("2").parentMid("1").patientMigrationReqId(1).build());

        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID))
                .thenReturn(new ArrayList<PatientAttachmentLog>(
                        Arrays.asList(
                                PatientAttachmentLog.builder().filename("test_main.txt").mid("1")
                                        .orderNum(0)
                                        .parentMid("0")
                                        .uploaded(false)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false)
                                        .patientMigrationReqId(1).build(),
                                PatientAttachmentLog.builder().filename("test_frag_1.txt")
                                        .mid("2")
                                        .orderNum(0)
                                        .parentMid("1")
                                        .uploaded(true)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false)
                                        .patientMigrationReqId(1).build(),
                                PatientAttachmentLog.builder().filename("test_frag_2.txt")
                                        .mid("3")
                                        .orderNum(1)
                                        .parentMid("1")
                                        .uploaded(true)
                                        .patientMigrationReqId(1)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false).build()
                        )));


        copcMessageHandler.checkAndMergeFileParts(inboundMessage, CONVERSATION_ID);
        verify(attachmentHandlerService, times(1)).storeAttachments(any(), eq(CONVERSATION_ID));
    }

    @Test
    public void When_UpdateAttachmentLogDoesAsExpected_Expect_RunWithNoErrors() throws ValidationException, SAXException, AttachmentLogException, InlineAttachmentProcessingException {
        var inboundMessage = new InboundMessage();
        inboundMessage.setPayload(readInboundMessageFromFile());
        inboundMessage.setEbXML(readLargeInboundMessageEbXmlFromFile());
        var inboundMessageId = xPathService.getNodeValue(ebXmlDocument, "/Envelope/Header/MessageHeader/MessageData/MessageId");

        when(patientAttachmentLogService.findAttachmentLog(inboundMessageId, CONVERSATION_ID))
                .thenReturn(PatientAttachmentLog.builder().filename("test_frag_1.txt").mid("2").parentMid("1").patientMigrationReqId(1).build());

        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID))
                .thenReturn(new ArrayList<PatientAttachmentLog>(
                        Arrays.asList(
                                PatientAttachmentLog.builder().filename("test_main.txt").mid("1")
                                        .orderNum(0)
                                        .parentMid("0")
                                        .uploaded(false)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false)
                                        .patientMigrationReqId(1).build(),
                                PatientAttachmentLog.builder().filename("test_frag_1.txt")
                                        .mid("2")
                                        .orderNum(0)
                                        .parentMid("1")
                                        .uploaded(true)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false)
                                        .patientMigrationReqId(1).build(),
                                PatientAttachmentLog.builder().filename("test_frag_2.txt")
                                        .mid("3")
                                        .orderNum(1)
                                        .parentMid("1")
                                        .uploaded(true)
                                        .patientMigrationReqId(1)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false).build()
                        )));


        copcMessageHandler.checkAndMergeFileParts(inboundMessage, CONVERSATION_ID);
        verify(patientAttachmentLogService, times(1)).updateAttachmentLog(any(), eq(CONVERSATION_ID));
    }

    @Test
    public void When_DeleteAttachmentCalledForEachAttachmentLogFragment_Expect_RunWithNoErrors() throws ValidationException, SAXException, AttachmentLogException, InlineAttachmentProcessingException {
        var inboundMessage = new InboundMessage();
        inboundMessage.setPayload(readInboundMessageFromFile());
        inboundMessage.setEbXML(readLargeInboundMessageEbXmlFromFile());
        var inboundMessageId = xPathService.getNodeValue(ebXmlDocument, "/Envelope/Header/MessageHeader/MessageData/MessageId");

        when(patientAttachmentLogService.findAttachmentLog(inboundMessageId, CONVERSATION_ID))
                .thenReturn(PatientAttachmentLog.builder().filename("test_frag_1.txt").mid("2").parentMid("1").patientMigrationReqId(1).build());

        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID))
                .thenReturn(new ArrayList<PatientAttachmentLog>(
                        Arrays.asList(
                                PatientAttachmentLog.builder().filename("test_main.txt").mid("1")
                                        .orderNum(0)
                                        .parentMid("0")
                                        .uploaded(false)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false)
                                        .patientMigrationReqId(1).build(),
                                PatientAttachmentLog.builder().filename("test_frag_1.txt")
                                        .mid("2")
                                        .orderNum(0)
                                        .parentMid("1")
                                        .uploaded(true)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false)
                                        .patientMigrationReqId(1).build(),
                                PatientAttachmentLog.builder().filename("test_frag_2.txt")
                                        .mid("3")
                                        .orderNum(1)
                                        .parentMid("1")
                                        .uploaded(true)
                                        .patientMigrationReqId(1)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false).build()
                        )));


        copcMessageHandler.checkAndMergeFileParts(inboundMessage, CONVERSATION_ID);
        verify(patientAttachmentLogService, times(2)).deleteAttachmentLog(any(), eq(CONVERSATION_ID));
    }

    @Test
    public void When_RemoveAttachmentCalledForEachAttachmentLogFragment_Expect_RunWithNoErrors() throws ValidationException, SAXException, AttachmentLogException, InlineAttachmentProcessingException {
        var inboundMessage = new InboundMessage();
        inboundMessage.setPayload(readInboundMessageFromFile());
        inboundMessage.setEbXML(readLargeInboundMessageEbXmlFromFile());
        var inboundMessageId = xPathService.getNodeValue(ebXmlDocument, "/Envelope/Header/MessageHeader/MessageData/MessageId");

        when(patientAttachmentLogService.findAttachmentLog(inboundMessageId, CONVERSATION_ID))
                .thenReturn(PatientAttachmentLog.builder().filename("test_frag_1.txt").mid("2").parentMid("1").patientMigrationReqId(1).build());

        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID))
                .thenReturn(new ArrayList<PatientAttachmentLog>(
                        Arrays.asList(
                                PatientAttachmentLog.builder().filename("test_main.txt").mid("1")
                                        .orderNum(0)
                                        .parentMid("0")
                                        .uploaded(false)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false)
                                        .patientMigrationReqId(1).build(),
                                PatientAttachmentLog.builder().filename("test_frag_1.txt")
                                        .mid("2")
                                        .orderNum(0)
                                        .parentMid("1")
                                        .uploaded(true)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false)
                                        .patientMigrationReqId(1).build(),
                                PatientAttachmentLog.builder().filename("test_frag_2.txt")
                                        .mid("3")
                                        .orderNum(1)
                                        .parentMid("1")
                                        .uploaded(true)
                                        .patientMigrationReqId(1)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false).build()
                        )));


        copcMessageHandler.checkAndMergeFileParts(inboundMessage, CONVERSATION_ID);
        verify(attachmentHandlerService, times(2)).removeAttachment(any());
    }

    @Test
    public void When_ParentCOPCMessageIncomingAfterFragments_Expect_RunWithNoErrors() throws ValidationException, SAXException, AttachmentLogException, InlineAttachmentProcessingException {
        var inboundMessage = new InboundMessage();
        inboundMessage.setPayload(readInboundMessageFromFile());
        inboundMessage.setEbXML(readLargeInboundMessageEbXmlFromFile());
        var inboundMessageId = xPathService.getNodeValue(ebXmlDocument, "/Envelope/Header/MessageHeader/MessageData/MessageId");

        when(patientAttachmentLogService.findAttachmentLog(inboundMessageId, CONVERSATION_ID))
                .thenReturn(PatientAttachmentLog.builder().filename("test_main.txt").mid("1").parentMid("0").patientMigrationReqId(1).build());

        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID))
                .thenReturn(new ArrayList<PatientAttachmentLog>(
                        Arrays.asList(
                                PatientAttachmentLog.builder().filename("test_main.txt").mid("1")
                                        .orderNum(0)
                                        .parentMid("0")
                                        .uploaded(true)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false)
                                        .patientMigrationReqId(1).build(),
                                PatientAttachmentLog.builder().filename("test_frag_1.txt")
                                        .mid("2")
                                        .orderNum(0)
                                        .parentMid("1")
                                        .uploaded(true)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false)
                                        .patientMigrationReqId(1).build(),
                                PatientAttachmentLog.builder().filename("test_frag_2.txt")
                                        .mid("3")
                                        .orderNum(1)
                                        .parentMid("1")
                                        .uploaded(true)
                                        .patientMigrationReqId(1)
                                        .largeAttachment(true)
                                        .base64(true)
                                        .compressed(false)
                                        .contentType("textplain")
                                        .lengthNum(0)
                                        .skeleton(false).build()
                        )));


        copcMessageHandler.checkAndMergeFileParts(inboundMessage, CONVERSATION_ID);
        verify(attachmentHandlerService, times(1)).buildSingleFileStringFromPatientAttachmentLogs(any());
    }

    @SneakyThrows
    private String readInboundMessageFromFile(){
        return readResourceAsString("/xml/inbound_message_payload.xml").replace("{{nhsNumber}}", NHS_NUMBER);
    }

    @SneakyThrows
    private String readLargeInboundMessageEbXmlFromFile() {
        return readResourceAsString("/xml/RCMRIN030000UK06_LARGE_MSG/ebxml.xml");
    }

}
