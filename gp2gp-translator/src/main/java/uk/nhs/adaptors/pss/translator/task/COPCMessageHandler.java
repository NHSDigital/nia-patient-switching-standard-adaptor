package uk.nhs.adaptors.pss.translator.task;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.hl7.v3.COPCIN000001UK01Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.primitives.Bytes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.ACKMessageData;
import uk.nhs.adaptors.pss.translator.service.AttachmentHandlerService;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class COPCMessageHandler {

    private final PatientMigrationRequestDao migrationRequestDao;
    private final SendACKMessageHandler sendACKMessageHandler;
    private final PatientAttachmentLogService patientAttachmentLogService;
    private final AttachmentHandlerService attachmentHandlerService;
    
    
    public void handleMessage(InboundMessage inboundMessage, String conversationId) throws JAXBException, InlineAttachmentProcessingException {

        COPCIN000001UK01Message payload = unmarshallString(inboundMessage.getPayload(), COPCIN000001UK01Message.class);
        PatientMigrationRequest migrationRequest = migrationRequestDao.getMigrationRequest(conversationId);

        sendAckMessage(payload, conversationId, migrationRequest.getLosingPracticeOdsCode());

        var conversationAttachmentLogs = patientAttachmentLogService.findAttachmentLogs(conversationId);

        var inboudMessageId = payload.getId().toString();

        // Step 1: Check parent_mid & conversation_id records to see if all fragments have uploaded
        var currentAttachmentLog = conversationAttachmentLogs.stream()
            .filter( log -> log.getMid() == inboudMessageId).findAny()
            .orElse(null);

        var parentAttachmentLogs = conversationAttachmentLogs.stream()
            .filter(log -> log.getParentMid() == currentAttachmentLog.getParentMid())
            .collect(Collectors.toList());

        var completedParentAttachmentLogs = parentAttachmentLogs.stream()
            .sorted(Comparator.comparingInt(PatientAttachmentLog::getOrderNum))
            .filter(log -> log.getUploaded() == true)
            .collect(Collectors.toList());

        if(parentAttachmentLogs.size() == completedParentAttachmentLogs.size()) {
            // Step 2: If all parts have been received, combined byte strings
            List<Byte> file = new ArrayList<>();
            completedParentAttachmentLogs.forEach( log -> {
                // Step 3: Pass new byte string into attachment handler with parent_mid's filename
               var filename = log.getFilename();
                file.addAll(Bytes.asList(attachmentHandlerService.getAttachment(filename)));
            });

            var parentLogFile = conversationAttachmentLogs.stream()
                .filter(log -> log.getMid() == currentAttachmentLog.getParentMid()).findAny().orElse(null);

            List<InboundMessage.Attachment> attachmentList = new ArrayList<InboundMessage.Attachment>();

            var fileDescription =
                "Filename=" + parentLogFile.getFilename() + " " +
                "ContentType=" + parentLogFile.getContentType() + " " +
                "Compressed=" + parentLogFile.getCompressed().toString() + " " +
                "LargeAttachment=" + parentLogFile.getLargeAttachment().toString() + " " +
                "OriginalBase64=" + parentLogFile.getBase64().toString() + " " +
                "Length=" + parentLogFile.getLengthNum();

            attachmentList.add(
                InboundMessage.Attachment.builder()
                    .payload(file.toString())
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