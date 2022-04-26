package uk.nhs.adaptors.pss.translator.task;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.hl7.v3.COPCIN000001UK01Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.ion.NullValueException;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.ACKMessageData;
import uk.nhs.adaptors.pss.translator.service.AttachmentHandlerService;
import uk.nhs.adaptors.pss.translator.service.XPathService;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class COPCMessageHandler {

    private final PatientMigrationRequestDao migrationRequestDao;
    private final SendACKMessageHandler sendACKMessageHandler;
    private final PatientAttachmentLogService patientAttachmentLogService;
    private final AttachmentHandlerService attachmentHandlerService;
    private final XPathService xPathService;

    private static final String MESSAGE_ID_PATH = "/Envelope/Header/MessageHeader/MessageData/MessageId";



    public void handleMessage(InboundMessage inboundMessage, String conversationId) throws JAXBException, InlineAttachmentProcessingException, SAXException {

        COPCIN000001UK01Message payload = unmarshallString(inboundMessage.getPayload(), COPCIN000001UK01Message.class);
        PatientMigrationRequest migrationRequest = migrationRequestDao.getMigrationRequest(conversationId);

        sendAckMessage(payload, conversationId, migrationRequest.getLosingPracticeOdsCode());

        Document ebXmlDocument = xPathService.parseDocumentFromXml(inboundMessage.getEbXML());
        var inboundMessageId = xPathService.getNodeValue(ebXmlDocument, MESSAGE_ID_PATH);
        var currentAttachmentLog = patientAttachmentLogService.findAttachmentLog(inboundMessageId, conversationId);
        var conversationAttachmentLogs = patientAttachmentLogService.findAttachmentLogs(conversationId);

// Change to try catch
        if(currentAttachmentLog == null) {
            // Should this wrap the code below instead of throwing an exception ?
            throw new NullValueException();
        }

        var attachmentLogFragments = conversationAttachmentLogs.stream()
            .sorted(Comparator.comparingInt(PatientAttachmentLog::getOrderNum))
            .filter(log -> log.getParentMid().equals(currentAttachmentLog.getParentMid()))
            .toList();

        var allFragmentsHaveUploaded = attachmentLogFragments.stream()
            .allMatch(PatientAttachmentLog::getUploaded);

        // Step 2: If all parts have been received, combined byte strings

        if(allFragmentsHaveUploaded) {
            List<byte[]> byteList = new ArrayList<>();
            int totalByteSize = 0;
            for(int i=0; i < attachmentLogFragments.size(); i++) {
                var log = attachmentLogFragments.get(i);
                var filename = log.getFilename();
                var attachmentBytes = attachmentHandlerService.getAttachment(filename);
                totalByteSize += attachmentBytes.length;
                byteList.add(attachmentHandlerService.getAttachment(filename));
            }
            byte[] file = new byte[totalByteSize];
            for(int i=0; i < file.length; i++) {
                var x = 0;
                var currentBytes = byteList.get(x);
                if(i < currentBytes.length) {
                    x++;
                }
                file[i] = currentBytes[i];
            }
            //TODO: FIX INDEX OUT OF BOUNDS ON LOOP ERROR. USED THESE LINKS TO CREATE NEW LOGIC
            //https://stackoverflow.com/questions/5683486/how-to-combine-two-byte-arrays\
            //https://mkyong.com/java/how-do-convert-byte-array-to-string-in-java/

            var parentLogFile = conversationAttachmentLogs.stream()
                .filter(log ->  log.getMid().equals(currentAttachmentLog.getParentMid()))
                .findAny()
                .orElse(null);

            List<InboundMessage.Attachment> attachmentList = new ArrayList<InboundMessage.Attachment>();

            var fileDescription =
                "Filename=" + "\"" + parentLogFile.getFilename()  + "\" " +
                "ContentType=" + parentLogFile.getContentType() + " " +
                "Compressed=" + parentLogFile.getCompressed().toString() + " " +
                "LargeAttachment=" + parentLogFile.getLargeAttachment().toString() + " " +
                "OriginalBase64=" + parentLogFile.getBase64().toString() + " " +
                "Length=" + parentLogFile.getLengthNum();



            attachmentList.add(
                InboundMessage.Attachment.builder()
                    .payload(new String(file, StandardCharsets. UTF_8))
                    .isBase64(parentLogFile
                        .getBase64()
                        .toString())
                    .contentType(parentLogFile.getContentType())
                    .description(fileDescription)
                    .build()
            );
            attachmentHandlerService.storeAttachments(attachmentList, conversationId);
            // Step 4: Mark parent_mid as uploaded
            var updatedLog = PatientAttachmentLog.builder()
                .mid(parentLogFile.getMid())
                .uploaded(true)
                .build();
            patientAttachmentLogService.updateAttachmentLog(updatedLog, conversationId);
        }




        // Step 5(cont): Check state of conversation_ids values and build bundle if complete
    }

    public boolean sendAckMessage(COPCIN000001UK01Message payload, String conversationId, String losingPracticeOdsCode) {

        LOGGER.debug("Sending ACK message for message with Conversation ID: [{}]", conversationId);

        return sendACKMessageHandler.prepareAndSendMessage(prepareAckMessageData(
            payload,
            conversationId,
            losingPracticeOdsCode
        ));
    }

    private ACKMessageData prepareAckMessageData(COPCIN000001UK01Message payload,
        String conversationId, String losingPracticeOdsCode) {

        String toOdsCode = losingPracticeOdsCode;
        String messageRef = parseMessageRef(payload);
        String toAsid = parseToAsid(payload);
        String fromAsid = parseFromAsid(payload);

        return ACKMessageData.builder()
            .conversationId(conversationId)
            .toOdsCode(toOdsCode)
            .messageRef(messageRef)
            .toAsid(toAsid)
            .fromAsid(fromAsid)
            .build();
    }

    private String parseFromAsid(COPCIN000001UK01Message payload) {
        return payload.getCommunicationFunctionRcv()
            .get(0)
            .getDevice()
            .getId()
            .get(0)
            .getExtension();
    }

    private String parseToAsid(COPCIN000001UK01Message payload) {
        return payload.getCommunicationFunctionSnd()
            .getDevice()
            .getId()
            .get(0)
            .getExtension();
    }

    private String parseMessageRef(COPCIN000001UK01Message payload) {
        return payload.getId().getRoot();
    }
}