package uk.nhs.adaptors.pss.translator.task;

import ca.uhn.fhir.parser.DataFormatException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.MigrationStatusLog;
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.exception.AttachmentNotFoundException;
import uk.nhs.adaptors.pss.translator.exception.BundleMappingException;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.ContinueRequestData;
import uk.nhs.adaptors.pss.translator.service.AttachmentHandlerService;
import uk.nhs.adaptors.pss.translator.service.AttachmentReferenceUpdaterService;
import uk.nhs.adaptors.pss.translator.service.BundleMapperService;
import uk.nhs.adaptors.pss.translator.service.NackAckPreparationService;
import uk.nhs.adaptors.pss.translator.service.XPathService;
import uk.nhs.adaptors.pss.translator.storage.StorageException;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.XmlParseUtilService;

import javax.xml.bind.JAXBException;
import java.text.ParseException;
import java.time.Instant;

import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.pss.translator.model.NACKReason.EHR_EXTRACT_CANNOT_BE_PROCESSED;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EhrExtractMessageHandler {
    private final MigrationStatusLogService migrationStatusLogService;
    private final PatientMigrationRequestDao migrationRequestDao;
    private final FhirParser fhirParser;
    private final BundleMapperService bundleMapperService;
    private final ObjectMapper objectMapper;
    private final SendContinueRequestHandler sendContinueRequestHandler;
    private final AttachmentHandlerService attachmentHandlerService;
    private final AttachmentReferenceUpdaterService attachmentReferenceUpdaterService;
    private final PatientAttachmentLogService patientAttachmentLogService;
    private final XPathService xPathService;
    private final NackAckPreparationService nackAckPreparationService;

    private static final String MESSAGE_ID_PATH = "/Envelope/Header/MessageHeader/MessageData/MessageId";

    public void handleMessage(InboundMessage inboundMessage, String conversationId)
        throws
        JAXBException,
        JsonProcessingException,
        InlineAttachmentProcessingException,
        BundleMappingException,
        AttachmentNotFoundException,
        ParseException,
        SAXException {

        RCMRIN030000UK06Message payload = unmarshallString(inboundMessage.getPayload(), RCMRIN030000UK06Message.class);
        PatientMigrationRequest migrationRequest = migrationRequestDao.getMigrationRequest(conversationId);
        MigrationStatusLog migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);

        migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_RECEIVED, conversationId);

        try {
            Document ebXmlDocument = getEbXmlDocument(inboundMessage);
            String messageId = xPathService.getNodeValue(ebXmlDocument, MESSAGE_ID_PATH);

            boolean hasExternalAttachment = !(inboundMessage.getExternalAttachments() == null
                || inboundMessage.getExternalAttachments().isEmpty());

            // Manage attachments against the EHR message
            var attachments = inboundMessage.getAttachments();
            if (attachments != null) {
                attachmentHandlerService.storeAttachments(attachments, conversationId);

                for (var i = 0; i < attachments.size(); i++) {
                    var attachment = attachments.get(i);
                    PatientAttachmentLog newAttachmentLog =
                        buildPatientAttachmentLogFromAttachment(messageId, migrationRequest, attachment);
                    patientAttachmentLogService.addAttachmentLog(newAttachmentLog);
                }
            }

            if (!hasExternalAttachment) {
                var fileUpdatedPayload = attachmentReferenceUpdaterService.updateReferenceToAttachment(
                        inboundMessage.getAttachments(),
                        conversationId,
                        inboundMessage.getPayload()
                );

                inboundMessage.setPayload(fileUpdatedPayload);
                payload = unmarshallString(inboundMessage.getPayload(), RCMRIN030000UK06Message.class);

                var bundle = bundleMapperService.mapToBundle(payload, migrationRequest.getLosingPracticeOdsCode());
                migrationStatusLogService.updatePatientMigrationRequestAndAddMigrationStatusLog(
                        conversationId,
                        fhirParser.encodeToJson(bundle),
                        objectMapper.writeValueAsString(inboundMessage),
                        EHR_EXTRACT_TRANSLATED
                );

                nackAckPreparationService.sendAckMessage(payload, conversationId);
            }

            //sending continue message
            if (hasExternalAttachment) {
                String patientNhsNumber = XmlParseUtilService.parseNhsNumber(payload);

                for (InboundMessage.ExternalAttachment externalAttachment: inboundMessage.getExternalAttachments()) {
                    PatientAttachmentLog patientAttachmentLog;

                    //save COPC_UK01 messages
                    patientAttachmentLog = buildPatientAttachmentLogFromExternalAttachment(migrationRequest, externalAttachment);

                    patientAttachmentLogService.addAttachmentLog(patientAttachmentLog);
                }

                migrationStatusLogService.updatePatientMigrationRequestAndAddMigrationStatusLog(
                    conversationId,
                    null,
                    objectMapper.writeValueAsString(inboundMessage),
                    EHR_EXTRACT_TRANSLATED
                );

                sendContinueRequest(
                    payload,
                    conversationId,
                    patientNhsNumber,
                    migrationRequest.getWinningPracticeOdsCode(),
                    migrationStatusLog.getDate().toInstant()
                );
            }

        } catch (BundleMappingException
                    | DataFormatException
                    | JsonProcessingException
                    | InlineAttachmentProcessingException
                    | AttachmentNotFoundException
                    | SAXException
                    | StorageException ex
        ) {
            nackAckPreparationService.sendNackMessage(EHR_EXTRACT_CANNOT_BE_PROCESSED, payload, conversationId);
            throw ex;
        } catch (ParseException ex) {
            throw ex;
        }
    }

    // Parent MID should be null against an EHR message so that they are not detected in the merge process
    private PatientAttachmentLog buildPatientAttachmentLogFromAttachment(
        String messageId,
        PatientMigrationRequest migrationRequest,
        InboundMessage.Attachment attachment) throws ParseException {

        return PatientAttachmentLog.builder()
            .mid(messageId)
            .filename(XmlParseUtilService.parseFilename(attachment.getDescription()))
            .parentMid(null)
            .patientMigrationReqId(migrationRequest.getId())
            .contentType(XmlParseUtilService.parseContentType(attachment.getDescription()))
            .compressed(XmlParseUtilService.parseCompressed(attachment.getDescription()))
            .largeAttachment(XmlParseUtilService.parseLargeAttachment(attachment.getDescription()))
            .base64(XmlParseUtilService.parseBase64(attachment.getDescription()))
            .skeleton(false)
            .uploaded(true)
            .lengthNum(XmlParseUtilService.parseFileLength(attachment.getDescription()))
            .orderNum(0)
            .build();
    }

    private PatientAttachmentLog buildPatientAttachmentLogFromExternalAttachment(
        PatientMigrationRequest migrationRequest,
        InboundMessage.ExternalAttachment externalAttachment) throws ParseException {
        return PatientAttachmentLog.builder()
                .mid(externalAttachment.getMessageId())
                .filename(XmlParseUtilService.parseFilename(externalAttachment.getDescription()))
                .parentMid(null)
                .patientMigrationReqId(migrationRequest.getId())
                .contentType(XmlParseUtilService.parseContentType(externalAttachment.getDescription()))
                .compressed(XmlParseUtilService.parseCompressed(externalAttachment.getDescription()))
                .largeAttachment(XmlParseUtilService.parseLargeAttachment(externalAttachment.getDescription()))
                .base64(XmlParseUtilService.parseBase64(externalAttachment.getDescription()))
                .skeleton(XmlParseUtilService.parseIsSkeleton(externalAttachment.getDescription()))
                .uploaded(false)
                .lengthNum(XmlParseUtilService.parseFileLength(externalAttachment.getDescription()))
                .orderNum(0)
                .build();
    }

    public void sendContinueRequest(
        RCMRIN030000UK06Message payload,
        String conversationId,
        String patientNhsNumber,
        String winningPracticeOdsCode,
        Instant mcciIN010000UK13creationTime
    ) {
        sendContinueRequestHandler.prepareAndSendRequest(
            prepareContinueRequestData(payload, conversationId, patientNhsNumber, winningPracticeOdsCode, mcciIN010000UK13creationTime)
        );
    }

    private ContinueRequestData prepareContinueRequestData(
        RCMRIN030000UK06Message payload,
        String conversationId,
        String patientNhsNumber,
        String winningPracticeOdsCode,
        Instant mcciIN010000UK13creationTime
    ) {
        var fromAsid = XmlParseUtilService.parseFromAsid(payload);
        var toAsid = XmlParseUtilService.parseToAsid(payload);
        var toOdsCode = XmlParseUtilService.parseToOdsCode(payload);
        var mcciIN010000UK13creationTimeToHl7Format = DateFormatUtil.toHl7Format(mcciIN010000UK13creationTime);

        return ContinueRequestData.builder()
            .conversationId(conversationId)
            .nhsNumber(patientNhsNumber)
            .fromAsid(fromAsid)
            .toAsid(toAsid)
            .toOdsCode(toOdsCode)
            .fromOdsCode(winningPracticeOdsCode)
            .mcciIN010000UK13creationTime(mcciIN010000UK13creationTimeToHl7Format)
            .build();
    }

    private Document getEbXmlDocument(InboundMessage inboundMessage) throws SAXException {
        return xPathService.parseDocumentFromXml(inboundMessage.getEbXML());
    }
}
