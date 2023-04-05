package uk.nhs.adaptors.pss.translator.task;

import static uk.nhs.adaptors.common.enums.MigrationStatus.COPC_MESSAGE_PROCESSING;
import static uk.nhs.adaptors.pss.translator.model.NACKReason.LARGE_MESSAGE_GENERAL_FAILURE;
import static uk.nhs.adaptors.common.enums.MigrationStatus.COPC_MESSAGE_RECEIVED;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationException;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.hl7.v3.COPCIN000001UK01Message;
import org.jdbi.v3.core.ConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.config.SupportedFileTypes;
import uk.nhs.adaptors.pss.translator.exception.AttachmentLogException;
import uk.nhs.adaptors.pss.translator.exception.AttachmentNotFoundException;
import uk.nhs.adaptors.pss.translator.exception.BundleMappingException;
import uk.nhs.adaptors.pss.translator.exception.ExternalAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.exception.UnsupportedFileTypeException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.EbxmlReference;
import uk.nhs.adaptors.pss.translator.service.AttachmentHandlerService;
import uk.nhs.adaptors.pss.translator.service.FailedProcessHandlingService;
import uk.nhs.adaptors.pss.translator.service.InboundMessageMergingService;
import uk.nhs.adaptors.pss.translator.service.NackAckPreparationService;
import uk.nhs.adaptors.pss.translator.service.XPathService;
import uk.nhs.adaptors.pss.translator.util.XmlParseUtilService;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class COPCMessageHandler {

    private static final String DESCRIPTION_PATH = "/Envelope/Body/Manifest/Reference[position()=2]/Description";
    private static final String MESSAGE_ID_PATH = "/Envelope/Header/MessageHeader/MessageData/MessageId";

    private final MigrationStatusLogService migrationStatusLogService;
    private final PatientMigrationRequestDao migrationRequestDao;
    private final NackAckPreparationService nackAckPreparationService;
    private final PatientAttachmentLogService patientAttachmentLogService;
    private final AttachmentHandlerService attachmentHandlerService;
    private final InboundMessageMergingService inboundMessageMergingService;
    private final XPathService xPathService;
    private final XmlParseUtilService xmlParseUtilService;
    private final SupportedFileTypes supportedFileTypes;
    private final FailedProcessHandlingService failedProcessHandlingService;

    public void handleMessage(InboundMessage inboundMessage, String conversationId)
            throws JAXBException, InlineAttachmentProcessingException, SAXException, AttachmentLogException,
                AttachmentNotFoundException, BundleMappingException, JsonProcessingException {

        COPCIN000001UK01Message payload = unmarshallString(inboundMessage.getPayload(), COPCIN000001UK01Message.class);

        if (failedProcessHandlingService.hasProcessFailed(conversationId)) {
            failedProcessHandlingService.handleFailedProcess(payload, conversationId);
            return;
        }

        PatientMigrationRequest migrationRequest = migrationRequestDao.getMigrationRequest(conversationId);
        migrationStatusLogService.addMigrationStatusLog(COPC_MESSAGE_RECEIVED, conversationId, null);

        try {
            Document ebXmlDocument = getEbXmlDocument(inboundMessage);
            String messageId = xPathService.getNodeValue(ebXmlDocument, MESSAGE_ID_PATH);
            PatientAttachmentLog patientAttachmentLog = patientAttachmentLogService.findAttachmentLog(messageId, conversationId);
            migrationStatusLogService.addMigrationStatusLog(COPC_MESSAGE_PROCESSING, conversationId, messageId);

            // If there is no PatientAttachmentLog for this message then we have received a message out of order
            if (patientAttachmentLog == null) {
                addLogForEarlyFragmentAndStore(inboundMessage, conversationId, payload, ebXmlDocument, migrationRequest.getId());
            } else {
                if (isManifestMessage(inboundMessage.getAttachments(), inboundMessage.getExternalAttachments())) {
                    extractFragmentsAndLog(migrationRequest, patientAttachmentLog, conversationId, inboundMessage);
                } else {
                    storeCOPCAttachment(patientAttachmentLog, inboundMessage, conversationId);
                    patientAttachmentLog.setUploaded(true);

                    var size = (Integer) inboundMessage.getAttachments()
                            .stream()
                            .mapToInt(a -> a.getPayload().length()).sum();

                    patientAttachmentLog.setPostProcessedLengthNum(size);
                    patientAttachmentLogService.updateAttachmentLog(patientAttachmentLog, conversationId);
                }
            }

            nackAckPreparationService.sendAckMessage(payload, conversationId, migrationRequest.getLosingPracticeOdsCode());
            checkAndMergeFileParts(inboundMessage, conversationId);

            // merge and uncompress large EHR message
            if (inboundMessageMergingService.canMergeCompleteBundle(conversationId)) {
                inboundMessageMergingService.mergeAndBundleMessage(conversationId);

            }
        } catch (WebClientRequestException | ConnectionException e) {
            throw e;
        } catch (ParseException | InlineAttachmentProcessingException | ValidationException
                 | SAXException | ExternalAttachmentProcessingException | UnsupportedFileTypeException e) {
            LOGGER.error("failed to parse COPC_IN000001UK01 ebxml: "
                + "failed to extract \"mid:\" from xlink:href, before sending the continue message", e);
            nackAckPreparationService.sendNackMessage(LARGE_MESSAGE_GENERAL_FAILURE, payload, conversationId);
        } catch (Exception e) {
            LOGGER.error("Unexpected exception", e);
            nackAckPreparationService.sendNackMessage(LARGE_MESSAGE_GENERAL_FAILURE, payload, conversationId);
        }
    }

    public void checkAndMergeFileParts(InboundMessage inboundMessage, String conversationId)
        throws SAXException, AttachmentLogException, ValidationException,
        InlineAttachmentProcessingException, ExternalAttachmentProcessingException, UnsupportedFileTypeException {

        Document ebXmlDocument = xPathService.parseDocumentFromXml(inboundMessage.getEbXML());
        var inboundMessageId = xPathService.getNodeValue(ebXmlDocument, MESSAGE_ID_PATH);
        var currentAttachmentLog = patientAttachmentLogService.findAttachmentLog(inboundMessageId, conversationId);

        if (currentAttachmentLog == null) {
            throw new AttachmentLogException("Given COPC message is missing an attachment log");
        }

        var conversationAttachmentLogs = patientAttachmentLogService.findAttachmentLogs(conversationId);
        // if a message has arrived early, it will not have a parent ID, so we can cancel early.
        // However, an index created by the ehr message will also have no parent id
        // Logic below manages that case to make sure index COPC messages trigger the merge check
        var indexMidReference = currentAttachmentLog.getMid();
        if (currentAttachmentLog.getParentMid() == null) {
            // it could also be an index file folowing design changes, check to see if it has any child attachments before returning
            var childFragments = conversationAttachmentLogs.stream()
                .filter(log -> (log.getParentMid() != null) && log.getParentMid().equals(indexMidReference))
                .toList();

            // if there are no child records then we can return
            if (childFragments.isEmpty()) {
                return;
            } else {
                // otherwise let's select the first child fragment to run the process as normal
                currentAttachmentLog = childFragments.get(0);
            }
        }

        PatientAttachmentLog finalCurrentAttachmentLog = currentAttachmentLog;
        var attachmentLogFragments = conversationAttachmentLogs.stream()
            .sorted(Comparator.comparingInt(PatientAttachmentLog::getOrderNum))
            .filter(log -> (log.getParentMid() != null) && log.getParentMid().equals(finalCurrentAttachmentLog.getParentMid()))
            .toList();

        var parentLogMessageId = attachmentLogFragments.size() == 1
            ? currentAttachmentLog.getMid()
            : currentAttachmentLog.getParentMid();

        attachmentLogFragments = conversationAttachmentLogs.stream()
            .sorted(Comparator.comparingInt(PatientAttachmentLog::getOrderNum))
            .filter(log -> (log.getParentMid() != null) && log.getParentMid().equals(parentLogMessageId))
            .toList();

        var allFragmentsHaveUploaded = attachmentLogFragments.stream()
            .allMatch(PatientAttachmentLog::getUploaded);

        if (allFragmentsHaveUploaded) {

            String payload = attachmentHandlerService
                .buildSingleFileStringFromPatientAttachmentLogs(attachmentLogFragments, conversationId);

            var parentLogFile = conversationAttachmentLogs.stream()
                .filter(log ->  log.getMid().equals(parentLogMessageId))
                .findAny()
                .orElse(null);

            // if we have been given a file length, validate it
            var canCheckAttachmentLength = parentLogFile.getLengthNum() > 0;

            if (canCheckAttachmentLength) {
                if (payload.length() != parentLogFile.getLengthNum()) {
                    throw new ExternalAttachmentProcessingException("Illegal file length detected");
                }
            }

            if (payload != null) {
                parentLogFile.setPostProcessedLengthNum(payload.length());
            }

            var mergedLargeAttachment = createNewLargeAttachmentInList(parentLogFile, payload);
            attachmentHandlerService.storeAttachments(mergedLargeAttachment, conversationId);

            var updatedLog = PatientAttachmentLog.builder()
                .mid(parentLogFile.getMid())
                .uploaded(true)
                .postProcessedLengthNum(parentLogFile.getPostProcessedLengthNum())
                .build();

            patientAttachmentLogService.updateAttachmentLog(updatedLog, conversationId);

            attachmentLogFragments.forEach((PatientAttachmentLog log) -> {
                attachmentHandlerService.removeAttachment(log.getFilename(), conversationId);
                patientAttachmentLogService.deleteAttachmentLog(log.getMid(), conversationId);
            });
        }
    }

    public List<InboundMessage.Attachment> createNewLargeAttachmentInList(PatientAttachmentLog largeFileLog, String payload) {

        var fileDescription =
            "Filename=" + "\"" + largeFileLog.getFilename()  + "\" "
                + "ContentType=" + largeFileLog.getContentType() + " "
                + "Compressed=" + largeFileLog.getCompressed().toString() + " "
                + "LargeAttachment=" + largeFileLog.getLargeAttachment().toString() + " "
                + "OriginalBase64=" + largeFileLog.getBase64().toString() + " "
                + "Length=" + largeFileLog.getLengthNum();

        return Collections.singletonList(
                InboundMessage.Attachment.builder()
                        .payload(payload)
                        .isBase64(largeFileLog
                                .getBase64()
                                .toString())
                        .contentType(largeFileLog.getContentType())
                        .description(fileDescription)
                        .build()
        );
    }

    private Document getEbXmlDocument(InboundMessage inboundMessage) throws SAXException {
        return xPathService.parseDocumentFromXml(inboundMessage.getEbXML());
    }

    private void addLogForEarlyFragmentAndStore(InboundMessage inboundMessage, String conversationId, COPCIN000001UK01Message payload,
        Document ebXmlDocument, int patientId) throws ValidationException, InlineAttachmentProcessingException,
        UnsupportedFileTypeException {
        String fragmentMid = getFragmentMidId(ebXmlDocument);
        String fileName = getFileNameForFragment(inboundMessage, payload);
        var attachment = inboundMessage.getAttachments().get(0);
        var postProcessedLength = inboundMessage.getAttachments().get(0).getPayload().length();

        PatientAttachmentLog fragmentAttachmentLog = buildFragmentAttachmentLog(fragmentMid, fileName,
                attachment.getContentType(), patientId, postProcessedLength);

        storeCOPCAttachment(fragmentAttachmentLog, inboundMessage, conversationId);
        fragmentAttachmentLog.setUploaded(true);

        patientAttachmentLogService.addAttachmentLog(fragmentAttachmentLog);
    }

    private void storeCOPCAttachment(PatientAttachmentLog fragmentAttachmentLog, InboundMessage inboundMessage,
                                     String conversationId)
        throws ValidationException, InlineAttachmentProcessingException, UnsupportedFileTypeException {

        if (inboundMessage.getAttachments().isEmpty()) {
            throw new InlineAttachmentProcessingException("COPC message does not contain an inline attachment");
        }

        if (fragmentAttachmentLog.getLargeAttachment() == null || fragmentAttachmentLog.getLargeAttachment()) {
            attachmentHandlerService.storeAttachmentWithoutProcessing(fragmentAttachmentLog.getFilename(),
                inboundMessage.getAttachments().get(0).getPayload(), conversationId,
                fragmentAttachmentLog.getContentType(), fragmentAttachmentLog.getLengthNum());
        } else {
            var attachment = attachmentHandlerService.buildInboundAttachmentsFromAttachmentLogs(
                List.of(fragmentAttachmentLog),
                List.of(inboundMessage.getAttachments().get(0).getPayload()),
                conversationId
            );

            attachmentHandlerService.storeAttachments(attachment, conversationId);
        }
    }

    private boolean checkIfFileTypeSupported(String fileType) {
        return supportedFileTypes.getAccepted() != null
            && supportedFileTypes.getAccepted().contains(fileType);
    }

    private boolean isManifestMessage(List<InboundMessage.Attachment> attachments,
        List<InboundMessage.ExternalAttachment> externalAttachments) {
        int attachmentsCount = attachments != null ? attachments.size() : 0;
        int externalAttachmentsCount = externalAttachments != null
            ? externalAttachments.size() : 0;
        return attachmentsCount + externalAttachmentsCount > 1;
    }

    private String getFileNameForFragment(InboundMessage inboundMessage, COPCIN000001UK01Message payload) {
        // confirm filename in payload on future examples
        if (!inboundMessage.getAttachments().get(0).getDescription().isEmpty()
            && inboundMessage.getAttachments().get(0).getDescription().contains("Filename")) {
            return XmlParseUtilService.parseFragmentFilename(inboundMessage.getAttachments().get(0).getDescription());
        } else {
            return retrieveFileNameFromPayload(payload);
        }
    }

    private PatientAttachmentLog buildFragmentAttachmentLog(
            String fragmentMid, String fileName, String contentType, int patientId, int postProcessedLength) {
        return PatientAttachmentLog.builder()
            .mid(fragmentMid)
            .filename(fileName)
            .contentType(contentType)
            .patientMigrationReqId(patientId)
            .postProcessedLengthNum(postProcessedLength)
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

    private void extractFragmentsAndLog(PatientMigrationRequest migrationRequest,
        PatientAttachmentLog parentAttachmentLog, String conversationId, InboundMessage message) throws ParseException, SAXException,
        ValidationException, InlineAttachmentProcessingException {

        List<EbxmlReference> attachmentReferenceDescription = new ArrayList<>(
                xmlParseUtilService.getEbxmlAttachmentsData(message));

        // first item is always the message payload reference so skip it
        for (var index = 1; index < attachmentReferenceDescription.size(); index++) {

            var payloadReference = attachmentReferenceDescription.get(index);
            var descriptionString = "";
            var messageId = "";
            var fileUpload = false;

            // in this instance there should only ever be one CID on a fragment index file
            if (payloadReference.getHref().contains("cid:")) {
                messageId = payloadReference.getHref().substring(payloadReference.getHref().indexOf("cid:") + "cid:".length());
                descriptionString = message.getAttachments().get(0).getDescription();

                // upload the file
                attachmentHandlerService.storeAttachmentWithoutProcessing(
                    XmlParseUtilService.parseFragmentFilename(descriptionString),
                    message.getAttachments().get(0).getPayload(),
                    conversationId,
                    message.getAttachments().get(0).getContentType(),
                    XmlParseUtilService.parseFileLength(descriptionString)
                );
                fileUpload = true;
            } else {
                var localMessageId = payloadReference.getHref().substring(payloadReference.getHref().indexOf("mid:") + "mid:".length());
                messageId = localMessageId;

                var externalAttachmentResult = message.getExternalAttachments()
                    .stream()
                    .filter(attachment -> attachment.getMessageId().equals(localMessageId))
                    .findFirst();

                if (externalAttachmentResult.isEmpty()) {
                    throw new ValidationException("External Attachment in payload header does not match a received External Attachment ID");
                }

                var externalAttachment = externalAttachmentResult.get();
                descriptionString = externalAttachment.getDescription();
            }

            PatientAttachmentLog fragmentLog = patientAttachmentLogService.findAttachmentLog(messageId, conversationId);

            if (fragmentLog != null) {
                updateFragmentLog(fragmentLog, parentAttachmentLog, descriptionString, index - 1, parentAttachmentLog.getLargeAttachment());
                patientAttachmentLogService.updateAttachmentLog(fragmentLog, conversationId);
            } else {
                PatientAttachmentLog newFragmentLog = buildPatientAttachmentLog(
                        messageId,
                        descriptionString,
                        parentAttachmentLog.getMid(),
                        migrationRequest.getId(),
                        index - 1,
                        fileUpload,
                        parentAttachmentLog.getLargeAttachment()
                );
                patientAttachmentLogService.addAttachmentLog(newFragmentLog);
            }
        }
    }

    private void updateFragmentLog(PatientAttachmentLog childLog, PatientAttachmentLog parentLog, String descriptionString,
                                   int orderNum, Boolean isLargeAttachment) throws ParseException {
        childLog.setParentMid(parentLog.getMid());
        childLog.setCompressed(XmlParseUtilService.parseCompressed(descriptionString));
        childLog.setLargeAttachment(isLargeAttachment);
        childLog.setSkeleton(parentLog.getSkeleton());
        childLog.setBase64(XmlParseUtilService.parseBase64(descriptionString));
        childLog.setOrderNum(orderNum);
    }

    private PatientAttachmentLog buildPatientAttachmentLog(String mid, String description, String parentMid, Integer patientId,
                Integer attachmentOrder, boolean uploaded, Boolean isLargeAttachment) throws ParseException {

        return PatientAttachmentLog.builder()
            .mid(mid)
            .filename(XmlParseUtilService.parseFragmentFilename(description))
            .parentMid(parentMid)
            .patientMigrationReqId(patientId)
            .contentType(XmlParseUtilService.parseContentType(description))
            .compressed(XmlParseUtilService.parseCompressed(description))
            .largeAttachment(isLargeAttachment)
            .base64(XmlParseUtilService.parseBase64(description))
            .skeleton(false)
            .uploaded(uploaded)
            .orderNum(attachmentOrder)
            .build();
    }
}