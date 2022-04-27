package uk.nhs.adaptors.pss.translator.task;

import static uk.nhs.adaptors.pss.translator.model.NACKReason.EHR_EXTRACT_CANNOT_BE_PROCESSED;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import java.text.ParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationException;

import org.hl7.v3.COPCIN000001UK01Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
    private static final String REFERENCES_ATTACHMENTS_PATH = "/Envelope/Body/Manifest/Reference";
    private static final String FRAGMENT_CID_PATH = "/Envelope/Body/Manifest/Reference[position()=2]";

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
                    int fragments = extractFragmentsAndUploadToPatientAttachmentLog(migrationRequest, ebXmlDocument, patientAttachmentLog
                        , conversationId);
                    // check if both children have been added to db, if they have then do merge of fragments here
                    // patientAttachmentService.getFragments(patientAttachmentLog.Mid, conversationId)
                    List<PatientAttachmentLog> fragmentLogs = patientAttachmentLogService.findAttachmentLogsByParentMid(conversationId,
                        patientAttachmentLog.getMid());
                    List<PatientAttachmentLog> uploadedFragments = fragmentLogs.stream()
                        .filter(PatientAttachmentLog::getUploaded)
                        .toList();

                    if (fragments == uploadedFragments.size()) {
                        mergeFragments(uploadedFragments);
                    }
                } else {

                    storeEhrExtractAttachment(patientAttachmentLog, inboundMessage, conversationId);
                    patientAttachmentLog.setUploaded(true);
                    patientAttachmentLogService.updateAttachmentLog(patientAttachmentLog, conversationId);

                    List<PatientAttachmentLog> attachments = patientAttachmentLogService.findAttachmentLogsByParentMid(conversationId,
                            patientAttachmentLog.getParentMid()).stream()
                        .filter(PatientAttachmentLog::getUploaded)
                        .toList();

                    if (attachments.size() > 1) {
                        mergeFragments(attachments);
                    }
                }
            } else {
                insertAndUploadFragmentFile(inboundMessage, conversationId, payload, ebXmlDocument);
            }
            sendAckMessage(payload, conversationId, migrationRequest.getLosingPracticeOdsCode());
        } catch (ParseException | SAXException e) {
            LOGGER.error("failed to parse COPC_IN000001UK01 ebxml: "
                + "failed to extract \"mid:\" from xlink:href, before sending the continue message", e);
            sendNackMessage(EHR_EXTRACT_CANNOT_BE_PROCESSED, payload, conversationId);
        }
    }

    private void mergeFragments(List<PatientAttachmentLog> uploadedFragments) {

        //TODO - Merge Fragments here
    }

    private Document getEbXmlDocument(InboundMessage inboundMessage) throws SAXException {
        return xPathService.parseDocumentFromXml(inboundMessage.getEbXML());
    }

    private void insertAndUploadFragmentFile(InboundMessage inboundMessage, String conversationId, COPCIN000001UK01Message payload,
        Document ebXmlDocument) throws ParseException, ValidationException {
        String fragmentMid = getFragmentMidId(ebXmlDocument);
        String fileName = getFileNameForFragment(inboundMessage, payload);

        PatientAttachmentLog fragmentAttachmentLog
            = buildFragmentAttachmentLog(fragmentMid, fileName, inboundMessage.getAttachments().get(0).getContentType());
        storeEhrExtractAttachment(fragmentAttachmentLog, inboundMessage, conversationId);
        fragmentAttachmentLog.setUploaded(true);

        patientAttachmentLogService.addAttachmentLog(fragmentAttachmentLog);
    }

    private void storeEhrExtractAttachment(PatientAttachmentLog fragmentAttachmentLog, InboundMessage inboundMessage,
        String conversationId) throws ValidationException {
        attachmentHandlerService.storeEhrExtract(fragmentAttachmentLog.getFilename(),
            inboundMessage.getAttachments().get(0).getPayload(), conversationId, fragmentAttachmentLog.getContentType());
    }

    private String getFileNameForFragment(InboundMessage inboundMessage, COPCIN000001UK01Message payload) throws ParseException {
        if (!inboundMessage.getAttachments().get(0).getDescription().isEmpty()) {
            return parseFilename(inboundMessage.getAttachments().get(0).getDescription());
        } else {
            return retrieveFileNameFromPayload(payload);
        }
    }

    private PatientAttachmentLog buildFragmentAttachmentLog(String fragmentMid, String fileName, String contentType) {
        return PatientAttachmentLog.builder()
            .mid(fragmentMid)
            .filename(fileName)
            .contentType(contentType)
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
        return xPathService.getNodes(ebXmlDocument, DESCRIPTION_PATH)
            .item(0)
            .getAttributes()
            .getNamedItem("xlink:href")
            .getNodeValue()
            .replace("cid:", "");
    }

    private boolean isManifestMessage(Document ebXmlDocument) {
        return xPathService.getNodeValue(ebXmlDocument, DESCRIPTION_PATH).contains("Filename=");
    }

    private int extractFragmentsAndUploadToPatientAttachmentLog(PatientMigrationRequest migrationRequest, Document ebXmlDocument,
        PatientAttachmentLog parentAttachmentLog, String conversationId) throws ParseException {
        int fragmentCount = 0;
        NodeList referencesAttachment = xPathService.getNodes(ebXmlDocument, REFERENCES_ATTACHMENTS_PATH);
        if (referencesAttachment != null) {

            for (int index = 0; index < referencesAttachment.getLength(); index++) {

                Node referenceNode = referencesAttachment.item(index);
                if (referenceNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element reference = (Element) referenceNode;
                    if (reference.getAttribute("xlink:href").contains("mid:")) {

                        String mid = reference.getAttribute("xlink:href").replace("mid:", "");

                        PatientAttachmentLog fragmentLog = patientAttachmentLogService.findAttachmentLog(mid, conversationId);
                        String descriptionString = reference.getFirstChild().getNextSibling().getFirstChild().getNodeValue();
                        int orderNum = index == 0 ? index : index - 1;
                        if (fragmentLog != null) {
                            updateFragmentLog(fragmentLog, parentAttachmentLog, descriptionString, orderNum);
                            patientAttachmentLogService.updateAttachmentLog(fragmentLog, conversationId);
                        } else {
                            PatientAttachmentLog newFragmentLog = buildPatientAttachmentLog(mid, descriptionString,
                                parentAttachmentLog.getMid(), migrationRequest.getId(), parentAttachmentLog.getSkeleton(), orderNum);

                            patientAttachmentLogService.addAttachmentLog(newFragmentLog);
                        }
                    }
                }
                fragmentCount = index;
            }
        }
        return fragmentCount;
    }

    private void updateFragmentLog(PatientAttachmentLog childLog, PatientAttachmentLog parentLog, String descriptionString, int orderNum) throws ParseException {
        childLog.setParentMid(parentLog.getParentMid());
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

    private String parseFilename(String description) throws ParseException {
        Pattern pattern = Pattern.compile("Filename=\"([A-Za-z\\d\\-_. ]*)\"");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new ParseException("Unable to parse originalFilename", 0);
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
        return gp2gpElement.getFirstChild().getNextSibling().getNextSibling().getFirstChild().getNodeValue();
    }
}