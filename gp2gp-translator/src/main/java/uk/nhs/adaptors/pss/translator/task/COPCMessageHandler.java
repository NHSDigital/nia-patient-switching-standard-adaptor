package uk.nhs.adaptors.pss.translator.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.v3.COPCIN000001UK01Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.exception.SkeletonEhrProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.service.AttachmentHandlerService;
import uk.nhs.adaptors.pss.translator.service.NackAckPreparationService;
import uk.nhs.adaptors.pss.translator.service.XPathService;
import uk.nhs.adaptors.pss.translator.util.XmlParseUtil;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationException;
import java.text.ParseException;

import static uk.nhs.adaptors.pss.translator.model.NACKReason.EHR_EXTRACT_CANNOT_BE_PROCESSED;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class COPCMessageHandler {

    private static final String DESCRIPTION_PATH = "/Envelope/Body/Manifest/Reference[position()=2]/Description";
    private static final String MESSAGE_ID_PATH = "/Envelope/Header/MessageHeader/MessageData/MessageId";

    private final PatientMigrationRequestDao migrationRequestDao;
    private final AttachmentHandlerService attachmentHandlerService;
    private final PatientAttachmentLogService patientAttachmentLogService;
    private final XPathService xPathService;
    private final NackAckPreparationService nackAckPreparationService;

    public void handleMessage(InboundMessage inboundMessage, String conversationId) throws JAXBException,
        InlineAttachmentProcessingException {

        COPCIN000001UK01Message payload = unmarshallString(inboundMessage.getPayload(), COPCIN000001UK01Message.class);
        PatientMigrationRequest migrationRequest = migrationRequestDao.getMigrationRequest(conversationId);
        
        try {
            Document ebXmlDocument = getEbXmlDocument(inboundMessage);
            String messageId = xPathService.getNodeValue(ebXmlDocument, MESSAGE_ID_PATH);
            PatientAttachmentLog patientAttachmentLog = patientAttachmentLogService.findAttachmentLog(messageId, conversationId);

            if (patientAttachmentLog == null) {
                insertLogAndUploadFragmentFile(inboundMessage, conversationId, payload, ebXmlDocument, migrationRequest.getId());
            } else {
                if (isManifestMessage(ebXmlDocument)) {
                    extractFragmentsAndAddRecordToPatientAttachmentLog(migrationRequest, patientAttachmentLog,
                        conversationId, inboundMessage);
                } else {
                    storeEhrExtractAttachment(patientAttachmentLog, inboundMessage, conversationId);
                    patientAttachmentLog.setUploaded(true);
                    patientAttachmentLogService.updateAttachmentLog(patientAttachmentLog, conversationId);
                }
            }
            nackAckPreparationService.sendAckMessage(payload, conversationId, migrationRequest.getLosingPracticeOdsCode());
        } catch (ParseException | SkeletonEhrProcessingException | SAXException e) {
            LOGGER.error("failed to parse COPC_IN000001UK01 ebxml: "
                + "failed to extract \"mid:\" from xlink:href, before sending the continue message", e);
            nackAckPreparationService.sendNackMessage(EHR_EXTRACT_CANNOT_BE_PROCESSED, payload, conversationId);
        }
    }

    private Document getEbXmlDocument(InboundMessage inboundMessage) throws SAXException {
        return xPathService.parseDocumentFromXml(inboundMessage.getEbXML());
    }

    private void insertLogAndUploadFragmentFile(InboundMessage inboundMessage, String conversationId, COPCIN000001UK01Message payload,
        Document ebXmlDocument, int patientId) throws ValidationException, ParseException, SkeletonEhrProcessingException {
        String fragmentMid = getFragmentMidId(ebXmlDocument);
        String fileName = getFileNameForFragment(inboundMessage, payload);

        PatientAttachmentLog fragmentAttachmentLog
            = buildFragmentAttachmentLog(fragmentMid, fileName, inboundMessage.getAttachments().get(0).getContentType(), patientId);
        storeEhrExtractAttachment(fragmentAttachmentLog, inboundMessage, conversationId);
        fragmentAttachmentLog.setUploaded(true);

        patientAttachmentLogService.addAttachmentLog(fragmentAttachmentLog);
    }

    private void storeEhrExtractAttachment(PatientAttachmentLog fragmentAttachmentLog, InboundMessage inboundMessage,
        String conversationId) throws ValidationException, SkeletonEhrProcessingException {
        attachmentHandlerService.storeEhrExtract(fragmentAttachmentLog.getFilename(),
            inboundMessage.getAttachments().get(0).getPayload(), conversationId, fragmentAttachmentLog.getContentType());
    }

    private String getFileNameForFragment(InboundMessage inboundMessage, COPCIN000001UK01Message payload) throws ParseException {
        if (!inboundMessage.getAttachments().get(0).getDescription().isEmpty()
            && inboundMessage.getAttachments().get(0).getDescription().contains("Filename")) {
            return XmlParseUtil.parseFragmentFilename(inboundMessage.getAttachments().get(0).getDescription());
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
        childLog.setCompressed(XmlParseUtil.parseCompressed(descriptionString));
        childLog.setLargeAttachment(XmlParseUtil.parseLargeAttachment(descriptionString));
        childLog.setSkeleton(parentLog.getSkeleton());
        childLog.setBase64(XmlParseUtil.parseBase64(descriptionString));
        childLog.setOrderNum(orderNum);
    }

    private PatientAttachmentLog buildPatientAttachmentLog(String mid, String description, String parentMid, Integer patientId,
        boolean isSkeleton, Integer attachmentOrder) throws ParseException {

        return PatientAttachmentLog.builder()
            .mid(mid)
            .filename(XmlParseUtil.parseFragmentFilename(description))
            .parentMid(parentMid)
            .patientMigrationReqId(patientId)
            .contentType(XmlParseUtil.parseContentType(description))
            .compressed(XmlParseUtil.parseCompressed(description))
            .largeAttachment(XmlParseUtil.parseLargeAttachment(description))
            .base64(XmlParseUtil.parseBase64(description))
            .skeleton(isSkeleton)
            .orderNum(attachmentOrder)
            .build();
    }
}