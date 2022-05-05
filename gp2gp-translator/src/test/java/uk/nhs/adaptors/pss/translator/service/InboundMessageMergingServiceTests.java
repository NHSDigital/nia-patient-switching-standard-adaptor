package uk.nhs.adaptors.pss.translator.service;

import lombok.SneakyThrows;
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
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.exception.AttachmentLogException;
import uk.nhs.adaptors.pss.translator.exception.BundleMappingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.StringReader;
import java.util.ArrayList;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;

@ExtendWith(MockitoExtension.class)
public class InboundMessageMergingServiceTests {

    private static final String CONVERSATION_ID = randomUUID().toString();
    private static final String NHS_NUMBER = "1111";
    @Mock
    private PatientAttachmentLogService patientAttachmentLogService;
    @Mock
    private MigrationStatusLogService migrationStatusLogService;
    @Mock
    private BundleMapperService bundleMapperService;

    @Mock
    private XPathService xPathService;

    @Mock
    private Document ebXmlDocument;
    @InjectMocks
    private InboundMessageMergingService inboundMessageMergingService;

    @Test
    public void when_NotAllUploadsComplete_Expect_ReturnWithoutChanging() throws BundleMappingException, AttachmentLogException, SAXException, JAXBException {
        var attachments = createPatientAttachmentList(true, false, 2);
        var inboundMessage = new InboundMessage();
        inboundMessage.setPayload(readInboundMessageFromFile());
        inboundMessage.setEbXML(readLargeInboundMessageEbXmlFromFile());
        var inboundMessageId = xPathService.getNodeValue(ebXmlDocument, "/Envelope/Header/MessageHeader/MessageData/MessageId");

        when(patientAttachmentLogService.findAttachmentLog(inboundMessageId, CONVERSATION_ID))
                .thenReturn(PatientAttachmentLog.builder()
                        .filename("test_frag_1.txt")
                        .mid("2")
                        .parentMid("1")
                        .patientMigrationReqId(1)
                        .build());

        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID)).thenReturn(attachments);

        inboundMessageMergingService.mergeAndBundleMessage(inboundMessage, CONVERSATION_ID);

        // verify nothing changed?
        verify(bundleMapperService, never()).mapToBundle(any(COPCIN000001UK01Message.class), any());
        verify(migrationStatusLogService, never()).updatePatientMigrationRequestAndAddMigrationStatusLog(any(), any(), any(), any());
    }

    @Test
    public void when_SkeletonMessage_Expect_FileMerge() throws AttachmentLogException, SAXException, ParserConfigurationException, BundleMappingException, JAXBException {
        var attachments = createPatientAttachmentList(true, false, 2);
        var inboundMessage = new InboundMessage();
        inboundMessage.setPayload(readInboundMessageFromFile());
        inboundMessage.setEbXML(readLargeInboundMessageEbXmlFromFile());
        var inboundMessageId = xPathService.getNodeValue(ebXmlDocument, "/Envelope/Header/MessageHeader/MessageData/MessageId");

        when(patientAttachmentLogService.findAttachmentLog(inboundMessageId, CONVERSATION_ID))
                .thenReturn(PatientAttachmentLog.builder()
                        .filename("test_frag_1.txt")
                        .mid("2")
                        .parentMid("1")
                        .patientMigrationReqId(1)
                        .skeleton(true)
                        .build());

        when(patientAttachmentLogService.findAttachmentLogs(CONVERSATION_ID)).thenReturn(attachments);

        inboundMessageMergingService.mergeAndBundleMessage(inboundMessage, CONVERSATION_ID);

        // verify( file "got" once )
        // verify bundle inbound message contains file txt instead of narrative statement
        ArgumentCaptor<COPCIN000001UK01Message> argument = ArgumentCaptor.forClass(COPCIN000001UK01Message.class);
        verify(bundleMapperService).mapToBundle(argument.capture(), any());
        var payload = argument.getValue();
        // todo: pull what we need out of payload and check it has contents of relevant extract in
        // var result = payload. ????
        // assert(result.contains(textFromExtract))
    }

    @Test
    public void when_AttachmentsPresent_Expect_AttachmentReferenceUpdated() {
        // straight forward pull from ehr tests (hopefully)
        assertTrue(false);
    }

    @Test
    public void when_HappyPath_Expect_ThrowNoErrors() {
        assertTrue(false);
    }

    private ArrayList<PatientAttachmentLog> createPatientAttachmentList(Boolean isParentUploaded, Boolean isFragmentUploaded, Integer amount) {
        // todo: if this stays the same, it is merely copied from Rio & Abid's PR, implement more re-usably? / will their code come into this file??
        var patientAttachmentLogs = new ArrayList<PatientAttachmentLog>(amount);
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
                        .skeleton(false)
                        .patientMigrationReqId(1).build()
        );
        for(var i = 1; i <= amount; i++) {
            patientAttachmentLogs.add(
                    PatientAttachmentLog.builder().filename("text_frag_" + i + ".txt").mid(Integer.toString(i+1))
                            .orderNum(0)
                            .parentMid("1")
                            .uploaded(isFragmentUploaded)
                            .largeAttachment(true)
                            .base64(true)
                            .compressed(false)
                            .contentType("text/plain")
                            .lengthNum(0)
                            .skeleton(false)
                            .patientMigrationReqId(1).build()
            );
        }
        return patientAttachmentLogs;
    }

    @SneakyThrows
    private String readInboundMessageFromFile() {
        return readResourceAsString("/xml/inbound_message_payload.xml").replace("{{nhsNumber}}", NHS_NUMBER);
    }

    @SneakyThrows
    private String readLargeInboundMessageEbXmlFromFile() {
        return readResourceAsString("/xml/RCMRIN030000UK06_LARGE_MSG/ebxml.xml");
    }

}
