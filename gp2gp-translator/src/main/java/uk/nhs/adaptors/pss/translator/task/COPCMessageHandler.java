package uk.nhs.adaptors.pss.translator.task;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationException;

import org.hl7.v3.COPCIN000001UK01Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.exception.AttachmentLogException;
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



    public void handleMessage(InboundMessage inboundMessage, String conversationId) throws JAXBException, InlineAttachmentProcessingException, SAXException, AttachmentLogException {

        COPCIN000001UK01Message payload = unmarshallString(inboundMessage.getPayload(), COPCIN000001UK01Message.class);
        PatientMigrationRequest migrationRequest = migrationRequestDao.getMigrationRequest(conversationId);
        sendAckMessage(payload, conversationId, migrationRequest.getLosingPracticeOdsCode());

        // Manage the merging of a large file
        checkAndMergeFileParts(inboundMessage, conversationId);

        // Step 5(cont): Check state of conversation_ids values and build bundle if complete
    }

    public void checkAndMergeFileParts(InboundMessage inboundMessage, String conversationId) throws SAXException, AttachmentLogException, ValidationException, InlineAttachmentProcessingException {

        Document ebXmlDocument = xPathService.parseDocumentFromXml(inboundMessage.getEbXML());
        var inboundMessageId = xPathService.getNodeValue(ebXmlDocument, MESSAGE_ID_PATH);
        var currentAttachmentLog = patientAttachmentLogService.findAttachmentLog(inboundMessageId, conversationId);

        // There should never be an instance where an attachment log does not exist at this point for a COPC message
        if (currentAttachmentLog == null) {
            throw new AttachmentLogException("Given COPC message is missing an attachment log");
        }

        // We will not have that many log records for a single EHR, we may as well load them into memory in one go now.
        var conversationAttachmentLogs = patientAttachmentLogService.findAttachmentLogs(conversationId);


        var attachmentLogFragments = conversationAttachmentLogs.stream()
            .sorted(Comparator.comparingInt(PatientAttachmentLog::getOrderNum))
            .filter(log -> log.getParentMid().equals(currentAttachmentLog.getParentMid()))
            .toList();

        //TODO an index could be the last part of an MID to return, in that scenario the file will never merge without he following
        // Single fragment indicates parent index section
        var parentLogMessageId = (attachmentLogFragments.size() == 1) ?
            currentAttachmentLog.getMid() :
            currentAttachmentLog.getParentMid();

        attachmentLogFragments = conversationAttachmentLogs.stream()
            .sorted(Comparator.comparingInt(PatientAttachmentLog::getOrderNum))
            .filter(log -> log.getParentMid().equals(parentLogMessageId))
            .toList();


        var allFragmentsHaveUploaded = attachmentLogFragments.stream()
            .allMatch(PatientAttachmentLog::getUploaded);

        // Step 2: If all parts have been received, combined byte strings
        if(allFragmentsHaveUploaded) {

            String payload = attachmentHandlerService.buildSingleFileStringFromPatientAttachmentLogs(attachmentLogFragments);

            var parentLogFile = conversationAttachmentLogs.stream()
                .filter(log ->  log.getMid().equals(parentLogMessageId))
                .findAny()
                .orElse(null);

            var mergedLargeAttachment = createNewLargeAttachmentInList(parentLogFile, payload);
            attachmentHandlerService.storeAttachments(mergedLargeAttachment, conversationId);

            // Mark large file parent log as uploaded
            var updatedLog = PatientAttachmentLog.builder()
                .mid(parentLogFile.getMid())
                .uploaded(true)
                .build();
            patientAttachmentLogService.updateAttachmentLog(updatedLog, conversationId);

            // Delete old large attachment parts
            attachmentLogFragments.forEach((PatientAttachmentLog log) -> {
                attachmentHandlerService.removeAttachment(log.getFilename());
                patientAttachmentLogService.deleteAttachmentLog(log.getMid(), conversationId);
            });
        }
    }

    public List<InboundMessage.Attachment> createNewLargeAttachmentInList(PatientAttachmentLog largeFileLog, String payload){

        var fileDescription =
            "Filename=" + "\"" + largeFileLog.getFilename()  + "\" " +
            "ContentType=" + largeFileLog.getContentType() + " " +
            "Compressed=" + largeFileLog.getCompressed().toString() + " " +
            "LargeAttachment=" + largeFileLog.getLargeAttachment().toString() + " " +
            "OriginalBase64=" + largeFileLog.getBase64().toString() + " " +
            "Length=" + largeFileLog.getLengthNum();

        List<InboundMessage.Attachment> attachmentList = Arrays.asList(
            InboundMessage.Attachment.builder()
                .payload(payload)
                .isBase64(largeFileLog
                    .getBase64()
                    .toString())
                .contentType(largeFileLog.getContentType())
                .description(fileDescription)
                .build()
        );

        return attachmentList;
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