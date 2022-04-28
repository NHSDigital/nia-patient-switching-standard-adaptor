package uk.nhs.adaptors.pss.translator.task;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import org.xml.sax.SAXException;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.exception.AttachmentLogException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.service.XPathService;

import org.w3c.dom.Document;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;

@ExtendWith(MockitoExtension.class)
public class COPCMessageHandlerTest {

    private static final String NHS_NUMBER = "123456";
    private static final String CONVERSATION_ID = randomUUID().toString();
    private static final String REFERENCES_ATTACHMENTS_PATH = "/Envelope/Body/Manifest/Reference";


    @Mock
    private PatientAttachmentLogService patientAttachmentLogService;

    @Mock
    private XPathService xPathService;

    @Mock
    private Document ebXmlDocument;

    @Mock
    private InboundMessage inboundMessage;

    @InjectMocks
    private COPCMessageHandler copcMessageHandler;

    @Test
    public void When_CurrentAttachmentLogIsNull_ThrowError() throws SAXException {
        var inboundMessage = new InboundMessage();
        inboundMessage.setPayload(readInboundMessageFromFile());
        inboundMessage.setEbXML(readLargeInboundMessageEbXmlFromFile());

        XPathService xPathService2 = new XPathService();

        when(xPathService.parseDocumentFromXml(inboundMessage.getEbXML()))
                .thenReturn(xPathService2.parseDocumentFromXml(inboundMessage.getEbXML()));

        when(patientAttachmentLogService.findAttachmentLog(any(), any()))
                .thenReturn(null);

        Document ebXmlDocument = xPathService.parseDocumentFromXml(inboundMessage.getEbXML());

        when(xPathService.getNodes(ebXmlDocument, REFERENCES_ATTACHMENTS_PATH))
                .thenReturn(xPathService2.getNodes(ebXmlDocument, REFERENCES_ATTACHMENTS_PATH));

        when(patientAttachmentLogService.findAttachmentLog(any(), any())).thenReturn(null);
        assertThrows(AttachmentLogException.class, () -> copcMessageHandler.checkAndMergeFileParts(inboundMessage, any()));
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
