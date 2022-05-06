package uk.nhs.adaptors.pss.translator.task;

import ca.uhn.fhir.parser.DataFormatException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
import uk.nhs.adaptors.pss.translator.exception.SkeletonEhrProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.ContinueRequestData;
import uk.nhs.adaptors.pss.translator.service.AttachmentHandlerService;
import uk.nhs.adaptors.pss.translator.service.AttachmentReferenceUpdaterService;
import uk.nhs.adaptors.pss.translator.service.BundleMapperService;
import uk.nhs.adaptors.pss.translator.service.NackAckPreparationService;
import uk.nhs.adaptors.pss.translator.storage.StorageException;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.XmlParseUtil;

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
    private final NackAckPreparationService nackAckPreparationService;

    public void handleMessage(InboundMessage inboundMessage, String conversationId)
            throws
            JAXBException,
            JsonProcessingException,
            InlineAttachmentProcessingException,
            BundleMappingException,
            AttachmentNotFoundException,
            ParseException,
            SkeletonEhrProcessingException {

        RCMRIN030000UK06Message payload = unmarshallString(inboundMessage.getPayload(), RCMRIN030000UK06Message.class);
        PatientMigrationRequest migrationRequest = migrationRequestDao.getMigrationRequest(conversationId);
        MigrationStatusLog migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);

        migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_RECEIVED, conversationId);

        try {
            boolean hasExternalAttachment = !(inboundMessage.getExternalAttachments() != null
                && inboundMessage.getExternalAttachments().isEmpty());

            attachmentHandlerService.storeAttachments(inboundMessage.getAttachments(), conversationId);

            if (!hasExternalAttachment) {
                var newPayloadStr = attachmentReferenceUpdaterService.updateReferenceToAttachment(
                        inboundMessage.getAttachments(),
                        conversationId,
                        inboundMessage.getPayload()
                );

                inboundMessage.setPayload(newPayloadStr);
                payload = unmarshallString(inboundMessage.getPayload(), RCMRIN030000UK06Message.class);

                var bundle = bundleMapperService.mapToBundle(payload, migrationRequest.getLosingPracticeOdsCode());
                migrationStatusLogService.updatePatientMigrationRequestAndAddMigrationStatusLog(
                        conversationId,
                        fhirParser.encodeToJson(bundle),
                        objectMapper.writeValueAsString(inboundMessage),
                        EHR_EXTRACT_TRANSLATED
                );
            }

            //sending continue message
            if (hasExternalAttachment) {
                String patientNhsNumber = XmlParseUtil.parseNhsNumber(payload);
                String extractFileName = String.format("%s_%s_payload", conversationId, XmlParseUtil.parseMessageRef(payload));

                attachmentHandlerService.storeEhrExtract(
                        extractFileName,
                        inboundMessage.getPayload(),
                        conversationId,
                        "application/xml; charset=UTF-8"
                );

                for (InboundMessage.ExternalAttachment externalAttachment: inboundMessage.getExternalAttachments()) {
                    PatientAttachmentLog patientAttachmentLog;
                    if (XmlParseUtil.parseIsSkeleton(externalAttachment.getDescription())) {
                        //save 06 Messages extract skeleton
                        patientAttachmentLog = buildPatientAttachmentSkeletonLog(payload, migrationRequest, extractFileName);

                    } else {
                        //save COPC_UK01 messages
                        patientAttachmentLog = buildPatientAttachmentLog(payload, migrationRequest, externalAttachment);
                    }
                    patientAttachmentLogService.addAttachmentLog(patientAttachmentLog);
                }

                sendContinueRequest(
                    payload,
                    conversationId,
                    patientNhsNumber,
                    migrationRequest.getWinningPracticeOdsCode(),
                    migrationStatusLog.getDate().toInstant()
                );
            } else {
                nackAckPreparationService.sendAckMessage(payload, conversationId);
            }

        } catch (BundleMappingException
                    | DataFormatException
                    | JsonProcessingException
                    | InlineAttachmentProcessingException
                    | AttachmentNotFoundException
                    | SkeletonEhrProcessingException
                    | StorageException ex
        ) {
            nackAckPreparationService.sendNackMessage(EHR_EXTRACT_CANNOT_BE_PROCESSED, payload, conversationId);
            throw ex;
        } catch (ParseException ex) {
            throw ex;
        }
    }

    private PatientAttachmentLog buildPatientAttachmentLog(RCMRIN030000UK06Message payload, PatientMigrationRequest migrationRequest,
        InboundMessage.ExternalAttachment externalAttachment) throws ParseException {
        return PatientAttachmentLog.builder()
                .mid(externalAttachment.getMessageId())
                .filename(XmlParseUtil.parseFilename(externalAttachment.getDescription()))
                .parentMid(XmlParseUtil.parseMessageRef(payload))
                .patientMigrationReqId(migrationRequest.getId())
                .contentType(XmlParseUtil.parseContentType(externalAttachment.getDescription()))
                .compressed(XmlParseUtil.parseCompressed(externalAttachment.getDescription()))
                .largeAttachment(XmlParseUtil.parseLargeAttachment(externalAttachment.getDescription()))
                .base64(XmlParseUtil.parseBase64(externalAttachment.getDescription()))
                .skeleton(false)
                .uploaded(false)
                .lengthNum(XmlParseUtil.parseFileLength(externalAttachment.getDescription()))
                .orderNum(0)
                .build();
    }

    private PatientAttachmentLog buildPatientAttachmentSkeletonLog(RCMRIN030000UK06Message payload,
        PatientMigrationRequest migrationRequest, String extractFileName) {
        return PatientAttachmentLog.builder()
                .mid(XmlParseUtil.parseMessageRef(payload))
                .filename(extractFileName)
                .parentMid(null)
                .patientMigrationReqId(migrationRequest.getId())
                .contentType("application/xml; charset=UTF-8")
                .compressed(false)
                .largeAttachment(true)
                .base64(false)
                .skeleton(true)
                .uploaded(true)
                .lengthNum(0)
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
        var fromAsid = XmlParseUtil.parseFromAsid(payload);
        var toAsid = XmlParseUtil.parseToAsid(payload);
        var toOdsCode = XmlParseUtil.parseToOdsCode(payload);
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
}
