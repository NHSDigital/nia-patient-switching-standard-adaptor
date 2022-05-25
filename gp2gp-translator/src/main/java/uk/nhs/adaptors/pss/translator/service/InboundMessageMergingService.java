package uk.nhs.adaptors.pss.translator.service;

import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.pss.translator.model.NACKReason.EHR_EXTRACT_CANNOT_BE_PROCESSED;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationException;
import javax.xml.transform.TransformerException;

import org.hl7.v3.RCMRIN030000UK06Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.exception.AttachmentNotFoundException;
import uk.nhs.adaptors.pss.translator.exception.BundleMappingException;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.EbxmlReference;
import uk.nhs.adaptors.pss.translator.util.XmlParseUtilService;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InboundMessageMergingService {

    private final PatientAttachmentLogService patientAttachmentLogService;
    private final AttachmentHandlerService attachmentHandlerService;
    private final AttachmentReferenceUpdaterService attachmentReferenceUpdaterService;
    private final MigrationStatusLogService migrationStatusLogService;
    private final XmlParseUtilService xmlParseUtilService;
    private final ObjectMapper objectMapper;
    private final XPathService xPathService;
    private final FhirParser fhirParser;
    private final BundleMapperService bundleMapperService;
    private final PatientMigrationRequestDao migrationRequestDao;
    private final NackAckPreparationService nackAckPreparationService;
    private static final String CONVERSATION_ID_HAS_NOT_BEEN_GIVEN = "Conversation Id has not been given";

    public boolean canMergeCompleteBundle(String conversationId) throws ValidationException {

        if (!StringUtils.hasText(conversationId)) {
            throw new ValidationException(CONVERSATION_ID_HAS_NOT_BEEN_GIVEN);
        }

        var undeletedLogs = getUndeletedLogsForConversation(conversationId);
        return undeletedLogs.stream().allMatch(log -> log.getUploaded().equals(true));
    }

    private void findAndReplaceSkeleton(List<PatientAttachmentLog> attachmentLogs, InboundMessage inboundMessage, String conversationId)
            throws SAXException, TransformerException {
        // merge skeleton message into original payload
        var skeletonLogs = attachmentLogs.stream().filter(log -> log.getSkeleton().equals(true)).toList();
        var skeletonFileName = skeletonLogs.stream().findFirst().map(PatientAttachmentLog::getFilename).orElseThrow();
        var skeletonFileAsString = new String(attachmentHandlerService.getAttachment(
            skeletonFileName, conversationId), StandardCharsets.UTF_8);

        // get ebxml references to find document id from skeleton message
        List<EbxmlReference> attachmentReferenceDescription = xmlParseUtilService.getEbxmlAttachmentsData(inboundMessage);
        var ebxmlSkeletonReference = attachmentReferenceDescription
                .stream()
                .filter(reference -> reference.getHref().contains(skeletonLogs.get(0).getMid()))
                .findFirst();

        if (ebxmlSkeletonReference.isEmpty()) {
            return;
        }

        var skeletonDocumentId = ebxmlSkeletonReference.get().getDocumentId();

        var payloadXml = xPathService.parseDocumentFromXml(inboundMessage.getPayload());

        var valueNodes = xPathService.getNodes(payloadXml, "//*/@*[.='" + skeletonDocumentId + "']/parent::*/parent::*");

        var payloadNodeToReplace = valueNodes.item(0);
        var payloadNodeToReplaceParent = payloadNodeToReplace.getParentNode();

        var skeletonExtractDocument = xPathService.parseDocumentFromXml(skeletonFileAsString);
        var skeletonExtractNodes = skeletonExtractDocument.getElementsByTagName("*");
        var primarySkeletonNode = skeletonExtractNodes.item(0);

        // using xPathServices breaks the xml document pointer, reset it
        payloadXml = payloadNodeToReplaceParent.getOwnerDocument();
        var importedToPayloadNode = payloadXml.importNode(primarySkeletonNode, true);
        payloadNodeToReplaceParent.replaceChild(importedToPayloadNode, payloadNodeToReplace);
        inboundMessage.setPayload(xmlParseUtilService.getStringFromDocument(payloadXml));

    }

    public void mergeAndBundleMessage(String conversationId) throws JAXBException, JsonProcessingException {

        if (!StringUtils.hasText(conversationId)) {
            throw new ValidationException(CONVERSATION_ID_HAS_NOT_BEEN_GIVEN);
        }

        PatientMigrationRequest migrationRequest = migrationRequestDao.getMigrationRequest(conversationId);
        var inboundMessage = objectMapper.readValue(migrationRequest.getInboundMessage(), InboundMessage.class);
        RCMRIN030000UK06Message payload = unmarshallString(inboundMessage.getPayload(), RCMRIN030000UK06Message.class);

        try {

            var attachmentLogs = getUndeletedLogsForConversation(conversationId);
            var attachmentsContainSkeletonMessage = attachmentLogs.stream().anyMatch(log -> log.getSkeleton().equals(true));

            if (attachmentsContainSkeletonMessage) {
                findAndReplaceSkeleton(attachmentLogs, inboundMessage, conversationId);
            }

            // process attachments
            var bypassPayloadLoadingArray = new String[attachmentLogs.size()];
            Arrays.fill(bypassPayloadLoadingArray, "");
            var messageAttachments = attachmentHandlerService.buildInboundAttachmentsFromAttachmentLogs(
                    attachmentLogs,
                    Arrays.asList(bypassPayloadLoadingArray),
                    conversationId
            );
            var newPayloadStr = attachmentReferenceUpdaterService.updateReferenceToAttachment(
                    messageAttachments,
                    conversationId,
                    inboundMessage.getPayload()
            );

            // process bundle
            inboundMessage.setPayload(newPayloadStr);
            payload = unmarshallString(inboundMessage.getPayload(), RCMRIN030000UK06Message.class);


            var bundle = bundleMapperService.mapToBundle(payload, migrationRequest.getLosingPracticeOdsCode());
            migrationStatusLogService.updatePatientMigrationRequestAndAddMigrationStatusLog(
                    conversationId,
                    fhirParser.encodeToJson(bundle),
                    objectMapper.writeValueAsString(inboundMessage),
                    EHR_EXTRACT_TRANSLATED
            );

        } catch (InlineAttachmentProcessingException | SAXException | TransformerException
                 | BundleMappingException | JAXBException | AttachmentNotFoundException | JsonProcessingException e) {

            LOGGER.error("failed to merge Large Message Parts", e);
            nackAckPreparationService.sendNackMessage(EHR_EXTRACT_CANNOT_BE_PROCESSED, payload, conversationId);
        }
    }

    private List<PatientAttachmentLog> getUndeletedLogsForConversation(String conversationId) throws ValidationException {

        if (!StringUtils.hasText(conversationId)) {
            throw new ValidationException(CONVERSATION_ID_HAS_NOT_BEEN_GIVEN);
        }

        var conversationAttachmentLogs = patientAttachmentLogService.findAttachmentLogs(conversationId);
        return conversationAttachmentLogs.stream()
            .filter(log -> log.getDeleted().equals(false))
            .toList();
    }
}
