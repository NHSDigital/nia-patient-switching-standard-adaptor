package uk.nhs.adaptors.pss.translator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;

import javax.xml.bind.ValidationException;
import java.util.List;

import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InboundMessageMergingService {

    private static final String MESSAGE_ID_PATH = "/Envelope/Header/MessageHeader/MessageData/MessageId";

    private final PatientAttachmentLogService patientAttachmentLogService;
    private final AttachmentHandlerService attachmentHandlerService;
    private final AttachmentReferenceUpdaterService attachmentReferenceUpdaterService;
    private final MigrationStatusLogService migrationStatusLogService;

    private final ObjectMapper objectMapper;
    private final XPathService xPathService;
    private final FhirParser fhirParser;
    private final BundleMapperService bundleMapperService;
    private final PatientMigrationRequestDao migrationRequestDao;

    public boolean canMergeCompleteBundle(String conversationId) throws ValidationException {

        var undeletedLogs = getUndeletedLogsForConversation(conversationId);
        return undeletedLogs.stream().allMatch(log -> log.getUploaded().equals(true));
    }

    public void mergeAndBundleMessage(String conversationId) {

        try {
            var attachmentLogs = getUndeletedLogsForConversation(conversationId);
            var attachmentsContainSkeletonMessage = attachmentLogs.stream().anyMatch(log -> log.getSkeleton().equals(true));

            PatientMigrationRequest migrationRequest = migrationRequestDao.getMigrationRequest(conversationId);
            var inboundMessage = objectMapper.readValue(migrationRequest.getInboundMessage(), InboundMessage.class);

            if (attachmentsContainSkeletonMessage) {
                // merge skeleton message into original payload
                var skeletonLogs = attachmentLogs.stream().filter(log -> log.getSkeleton().equals(true)).toList();
                var skeletonFileName = skeletonLogs.stream().findFirst().get().getFilename();
                var skeletonSingleStr = attachmentHandlerService.buildSingleFileStringFromPatientAttachmentLogs(skeletonLogs);

                // replace narrative statement that matches eb:id with skeletonSingleStr
                // find narrative statement
                var ebXml = xPathService.parseDocumentFromXml(inboundMessage.getEbXML());
                var nodes = xPathService.getNodes(ebXml, "?");
            }

            // process attachments
            var messageAttachments = attachmentHandlerService.buildInboundAttachmentsFromAttachmentLogs(attachmentLogs, null);
            var newPayloadStr = attachmentReferenceUpdaterService.updateReferenceToAttachment(
                messageAttachments,
                conversationId,
                inboundMessage.getPayload()
            );

            // process bundle
            inboundMessage.setPayload(newPayloadStr);
            var payload = unmarshallString(inboundMessage.getPayload(), RCMRIN030000UK06Message.class);
            var bundle = bundleMapperService.mapToBundle(payload, migrationRequest.getLosingPracticeOdsCode());
            migrationStatusLogService.updatePatientMigrationRequestAndAddMigrationStatusLog(
                conversationId,
                fhirParser.encodeToJson(bundle),
                objectMapper.writeValueAsString(inboundMessage),
                EHR_EXTRACT_TRANSLATED
            );

            // move to new service
            //sendAckMessage(payload, conversationId);
        } catch (Exception ex) {
            // send a NACK
//            sendNackMessage(EHR_EXTRACT_CANNOT_BE_PROCESSED, payload, conversationId);
        }

    }

    private List<PatientAttachmentLog> getUndeletedLogsForConversation(String conversationId) throws ValidationException {
        if (conversationId.isEmpty()) {
            throw new ValidationException("Conversation Id has not been given");
        }

        var conversationAttachmentLogs = patientAttachmentLogService.findAttachmentLogs(conversationId);
        return conversationAttachmentLogs.stream()
            .filter(log -> log.getDeleted().equals(false))
            .toList();
    }
}
