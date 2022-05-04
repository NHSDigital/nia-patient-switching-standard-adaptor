package uk.nhs.adaptors.pss.translator.task;

import static uk.nhs.adaptors.pss.translator.model.NACKReason.EHR_EXTRACT_CANNOT_BE_PROCESSED;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationException;

import org.hl7.v3.COPCIN000001UK01Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.ACKMessageData;
import uk.nhs.adaptors.pss.translator.model.NACKMessageData;
import uk.nhs.adaptors.pss.translator.model.NACKReason;
import uk.nhs.adaptors.pss.translator.service.AttachmentHandlerService;
import uk.nhs.adaptors.pss.translator.service.XPathService;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class COPCMessageHandler {

    private static final String DESCRIPTION_PATH = "/Envelope/Body/Manifest/Reference[position()=2]/Description";
    private static final String MESSAGE_ID_PATH = "/Envelope/Header/MessageHeader/MessageData/MessageId";

    private final PatientMigrationRequestDao migrationRequestDao;
    private final MigrationStatusLogService migrationStatusLogService;
    private final AttachmentHandlerService attachmentHandlerService;
    private final PatientAttachmentLogService patientAttachmentLogService;
    private final XPathService xPathService;
    private final SendACKMessageHandler sendACKMessageHandler;
    private final SendNACKMessageHandler sendNACKMessageHandler;

    public void handleMessage(InboundMessage inboundMessage, String conversationId) throws JAXBException,
        InlineAttachmentProcessingException {

        COPCIN000001UK01Message payload = unmarshallString(inboundMessage.getPayload(), COPCIN000001UK01Message.class);
        PatientMigrationRequest migrationRequest = migrationRequestDao.getMigrationRequest(conversationId);

        try {
            Document ebXmlDocument = getEbXmlDocument(inboundMessage);
            String messageId = xPathService.getNodeValue(ebXmlDocument, MESSAGE_ID_PATH);
            PatientAttachmentLog patientAttachmentLog = patientAttachmentLogService.findAttachmentLog(messageId, conversationId);

            if (patientAttachmentLog != null) {

                if (isManifestMessage(ebXmlDocument)) {
                    extractFragmentsAndAddRecordToPatientAttachmentLog(migrationRequest, patientAttachmentLog,
                        conversationId, inboundMessage);
                } else {
                    storeEhrExtractAttachment(patientAttachmentLog, inboundMessage, conversationId);
                    patientAttachmentLog.setUploaded(true);
                    patientAttachmentLogService.updateAttachmentLog(patientAttachmentLog, conversationId);
                }
            } else {
                insertAndUploadFragmentFile(inboundMessage, conversationId, payload, ebXmlDocument, migrationRequest.getId());
            }
            sendAckMessage(payload, conversationId, migrationRequest.getLosingPracticeOdsCode());
        } catch (ParseException | SAXException e) {
            LOGGER.error("failed to parse COPC_IN000001UK01 ebxml: "
                + "failed to extract \"mid:\" from xlink:href, before sending the continue message", e);
            sendNackMessage(EHR_EXTRACT_CANNOT_BE_PROCESSED, payload, conversationId);
        }
    }

    private Document getEbXmlDocument(InboundMessage inboundMessage) throws SAXException {
        return xPathService.parseDocumentFromXml(inboundMessage.getEbXML());
    }

    private void insertAndUploadFragmentFile(InboundMessage inboundMessage, String conversationId, COPCIN000001UK01Message payload,
        Document ebXmlDocument, int patientId) throws ValidationException {
        String fragmentMid = getFragmentMidId(ebXmlDocument);
        String fileName = getFileNameForFragment(inboundMessage, payload);

        PatientAttachmentLog fragmentAttachmentLog
            = buildFragmentAttachmentLog(fragmentMid, fileName, inboundMessage.getAttachments().get(0).getContentType(), patientId);
        storeEhrExtractAttachment(fragmentAttachmentLog, inboundMessage, conversationId);
        fragmentAttachmentLog.setUploaded(true);

        patientAttachmentLogService.addAttachmentLog(fragmentAttachmentLog);
    }

    private void storeEhrExtractAttachment(PatientAttachmentLog fragmentAttachmentLog, InboundMessage inboundMessage,
        String conversationId) throws ValidationException {
        attachmentHandlerService.storeEhrExtract(fragmentAttachmentLog.getFilename(),
            inboundMessage.getAttachments().get(0).getPayload(), conversationId, fragmentAttachmentLog.getContentType());
    }

    private String getFileNameForFragment(InboundMessage inboundMessage, COPCIN000001UK01Message payload) {
        if (!inboundMessage.getAttachments().get(0).getDescription().isEmpty()
            && inboundMessage.getAttachments().get(0).getDescription().contains("Filename")) {
            return parseFilename(inboundMessage.getAttachments().get(0).getDescription());
        } else {
            return retrieveFileNameFromPayload(payload);
        }
    }

    private PatientAttachmentLog buildFragmentAttachmentLog(String fragmentMid, String fileName, String contentType, int patientId) {
        return PatientAttachmentLog.builder()
            .mid(fragmentMid)
            .filename(fileName)
            .contentType(contentType)
            .patientMigrationReqId(patientId)
            .build();
    }

    private String retrieveFileNameFromPayload(COPCIN000001UK01Message doc) {
        return doc.getControlActEvent()
            .getSubject()
            .getPayloadInformation()
            .getPertinentInformation()
            .get(0)
            .getPertinentPayloadBody()
            .getValue()
            .getReference()
            .getValue();
    }

    private String getFragmentMidId(Document ebXmlDocument) {
        return xPathService.getNodeValue(ebXmlDocument, MESSAGE_ID_PATH);
    }

    private boolean isManifestMessage(Document ebXmlDocument) {
        return xPathService.getNodeValue(ebXmlDocument, DESCRIPTION_PATH).contains("Filename=");
    }

    private void extractFragmentsAndAddRecordToPatientAttachmentLog(PatientMigrationRequest migrationRequest,
        PatientAttachmentLog parentAttachmentLog, String conversationId, InboundMessage message) throws ParseException {
        int orderNum = 0;
        for (InboundMessage.ExternalAttachment externalAttachment : message.getExternalAttachments()) {
            PatientAttachmentLog fragmentLog = patientAttachmentLogService.findAttachmentLog(externalAttachment.getMessageId(),
                conversationId);
            String descriptionString = externalAttachment.getDescription();
            if (fragmentLog != null) {
                updateFragmentLog(fragmentLog, parentAttachmentLog, descriptionString, orderNum);
                patientAttachmentLogService.updateAttachmentLog(fragmentLog, conversationId);
            } else {
                PatientAttachmentLog newFragmentLog = buildPatientAttachmentLog(externalAttachment.getMessageId(), descriptionString,
                    parentAttachmentLog.getMid(), migrationRequest.getId(), parentAttachmentLog.getSkeleton(), orderNum);
                patientAttachmentLogService.addAttachmentLog(newFragmentLog);
            }
            orderNum++;
        }
    }
    private void updateFragmentLog(PatientAttachmentLog childLog, PatientAttachmentLog parentLog, String descriptionString,
        int orderNum) throws ParseException {
        childLog.setParentMid(parentLog.getMid());
        childLog.setCompressed(parseCompressed(descriptionString));
        childLog.setLargeAttachment(parseLargeAttachment(descriptionString));
        childLog.setSkeleton(parentLog.getSkeleton());
        childLog.setBase64(parseBase64(descriptionString));
        childLog.setOrderNum(orderNum);
    }
    private PatientAttachmentLog buildPatientAttachmentLog(String mid, String description, String parentMid, Integer patientId,
        boolean isSkeleton, Integer attachmentOrder) throws ParseException {

        return PatientAttachmentLog.builder()
            .mid(mid)
            .filename(parseFilename(description))
            .parentMid(parentMid)
            .patientMigrationReqId(patientId)
            .contentType(parseContentType(description))
            .compressed(parseCompressed(description))
            .largeAttachment(parseLargeAttachment(description))
            .base64(parseBase64(description))
            .skeleton(isSkeleton)
            .orderNum(attachmentOrder)
            .build();
    }
    public boolean sendAckMessage(COPCIN000001UK01Message payload, String conversationId, String losingPracticeOdsCode) {

        LOGGER.debug("Sending ACK message for message with Conversation ID: [{}]", conversationId);

        return sendACKMessageHandler.prepareAndSendMessage(prepareAckMessageData(
            payload,
            conversationId,
            losingPracticeOdsCode
        ));
    }
    private String parseFilename(String description) {

        return Arrays.asList(description.split(" ")).stream()
            .filter(desc -> desc.contains("Filename"))
            .map(desc -> desc.replace("Filename=", ""))
            .toList().get(0);
    }
    private boolean parseBase64(String description) throws ParseException {
        Pattern pattern = Pattern.compile("OriginalBase64=(Yes|No)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1).equals("Yes");
        }

        throw new ParseException("Unable to parse isBase64", 0);
    }
    private boolean parseLargeAttachment(String description) throws ParseException {
        Pattern pattern = Pattern.compile("LargeAttachment=(Yes|No)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1).equals("Yes");
        }

        throw new ParseException("Unable to parse isLargeAttachment", 0);
    }
    private boolean parseCompressed(String description) throws ParseException {
        Pattern pattern = Pattern.compile("Compressed=(Yes|No)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1).equals("Yes");
        }

        throw new ParseException("Unable to parse isCompressed", 0);
    }
    private String parseContentType(String description) throws ParseException {
        Pattern pattern = Pattern.compile("ContentType=([A-Za-z\\d\\-/]*)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new ParseException("Unable to parse ContentType", 0);
    }
    private ACKMessageData prepareAckMessageData(COPCIN000001UK01Message payload,
        String conversationId, String losingPracticeOdsCode) {

        String messageRef = parseMessageRef(payload);
        String toAsid = parseToAsid(payload);
        String fromAsid = parseFromAsid(payload);

        return ACKMessageData.builder()
            .conversationId(conversationId)
            .toOdsCode(losingPracticeOdsCode)
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
    public boolean sendNackMessage(NACKReason reason, COPCIN000001UK01Message payload, String conversationId) {

        LOGGER.debug("Sending NACK message with acknowledgement code [{}] for message EHR Extract message [{}]", reason.getCode(),
            payload.getId().getRoot());

        migrationStatusLogService.addMigrationStatusLog(reason.getMigrationStatus(), conversationId);

        return sendNACKMessageHandler.prepareAndSendMessage(prepareNackMessageData(
            reason,
            payload,
            conversationId
        ));
    }
    private NACKMessageData prepareNackMessageData(NACKReason reason, COPCIN000001UK01Message payload,
        String conversationId) {

        String toOdsCode = parseToOdsCode(payload);
        String messageRef = parseMessageRef(payload);
        String toAsid = parseToAsid(payload);
        String fromAsid = parseFromAsid(payload);
        String nackCode = reason.getCode();

        return NACKMessageData.builder()
            .conversationId(conversationId)
            .nackCode(nackCode)
            .toOdsCode(toOdsCode)
            .messageRef(messageRef)
            .toAsid(toAsid)
            .fromAsid(fromAsid)
            .build();
    }
    private String parseToOdsCode(COPCIN000001UK01Message payload) {

        Element gp2gpElement = payload.getControlActEvent()
            .getSubject()
            .getPayloadInformation()
            .getValue()
            .getAny()
            .get(0);


        return gp2gpElement.getFirstChild() // Version
            .getNextSibling() // Receipients
            .getNextSibling() // From
            .getFirstChild() // From:Data
            .getNodeValue();
    }
}