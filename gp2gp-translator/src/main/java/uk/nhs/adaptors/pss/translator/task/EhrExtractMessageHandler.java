package uk.nhs.adaptors.pss.translator.task;

import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.pss.translator.model.NACKReason.EHR_EXTRACT_CANNOT_BE_PROCESSED;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import java.time.Instant;

import javax.xml.bind.JAXBException;

import org.hl7.v3.RCMRIN030000UK06Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.parser.DataFormatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.nhs.adaptors.pss.translator.model.ACKMessageData;
import uk.nhs.adaptors.pss.translator.model.ContinueRequestData;
import uk.nhs.adaptors.pss.translator.model.NACKMessageData;
import uk.nhs.adaptors.pss.translator.model.NACKReason;
import uk.nhs.adaptors.pss.translator.service.AttachmentHandlerService;
import uk.nhs.adaptors.pss.translator.service.AttachmentReferenceUpdaterService;
import uk.nhs.adaptors.pss.translator.service.BundleMapperService;
import uk.nhs.adaptors.pss.translator.service.XPathService;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.ParseException;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EhrExtractMessageHandler {
    private final MigrationStatusLogService migrationStatusLogService;
    private final PatientMigrationRequestDao migrationRequestDao;
    private final FhirParser fhirParser;
    private final BundleMapperService bundleMapperService;
    private final ObjectMapper objectMapper;
    private final XPathService xPathService;
    private final SendContinueRequestHandler sendContinueRequestHandler;
    private final AttachmentHandlerService attachmentHandlerService;
    private final AttachmentReferenceUpdaterService attachmentReferenceUpdaterService;
    private final SendNACKMessageHandler sendNACKMessageHandler;
    private final SendACKMessageHandler sendACKMessageHandler;
    private final PatientAttachmentLogService patientAttachmentLogService;

    public void handleMessage(InboundMessage inboundMessage, String conversationId) throws JAXBException, JsonProcessingException,
            SAXException, InlineAttachmentProcessingException, BundleMappingException, AttachmentNotFoundException, ParseException {

        RCMRIN030000UK06Message payload = unmarshallString(inboundMessage.getPayload(), RCMRIN030000UK06Message.class);
        PatientMigrationRequest migrationRequest = migrationRequestDao.getMigrationRequest(conversationId);
        MigrationStatusLog migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);

        migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_RECEIVED, conversationId);

        try {
            boolean hasExternalAttachment = inboundMessage.getExternalAttachments().size() > 0;
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
            ////sending continue message
            if (hasExternalAttachment) {
                String patientNhsNumber = payload
                    .getControlActEvent()
                    .getSubject()
                    .getEhrExtract()
                    .getRecordTarget()
                    .getPatient()
                    .getId()
                    .getExtension();

                //save extract in storage
                String extractFileName = conversationId + "_" + parseMessageRef(payload) + "_payload";
                attachmentHandlerService.storeEhrExtract(
                        extractFileName,
                        inboundMessage.getPayload(),
                        conversationId,
                        "application/xml; charset=UTF-8"
                ); //TEST

                //save 06 Messages extract skeleton
                PatientAttachmentLog patientExtractAttachmentLog = PatientAttachmentLog.builder()
                        .mid(parseMessageRef(payload))
                        .filename(extractFileName)
                        .parentMid(null)
                        .patientMigrationReqId(migrationRequest.getId())
                        .contentType("application/xml; charset=UTF-8")
                        .compressed(false)
                        .largeAttachment(true) //need to ask
                        .base64(false)
                        .skeleton(true)
                        .uploaded(true)
                        .lengthNum(inboundMessage.getPayload().length())
                        .orderNum(0)
                        .build();

                patientAttachmentLogService.addAttachmentLog(patientExtractAttachmentLog); //TEST

                //test/ if passed array of 4 items, verify patientAttachmentLogService.addAttachmentLog 5 times
                //save COPC_UK01 messages
                int index = 0;
                for (InboundMessage.ExternalAttachment externalAttachment: inboundMessage.getExternalAttachments()) {
                    PatientAttachmentLog patientAttachmentLog = PatientAttachmentLog.builder()
                            .mid(externalAttachment.getMessageId())
                            .filename(parseFilename(externalAttachment.getDescription()))
                            .parentMid(parseMessageRef(payload))
                            .patientMigrationReqId(migrationRequest.getId())
                            .contentType(parseContentType(externalAttachment.getDescription()))
                            .compressed(parseIsCompressed(externalAttachment.getDescription()))
                            .largeAttachment(parseIsLargeAttachment(externalAttachment.getDescription())) //need to ask
                            .base64(parseIsOriginalBase64(externalAttachment.getDescription()))
                            .skeleton(parseIsSkeleton(externalAttachment.getDescription()))
                            .uploaded(false)
                            .lengthNum(parseFileLength(externalAttachment.getDescription()))
                            .orderNum(index++)
                            .build();
                    patientAttachmentLogService.addAttachmentLog(patientAttachmentLog); //TEST
                }

                sendContinueRequest(
                    payload,
                    conversationId,
                    patientNhsNumber,
                    migrationRequest.getWinningPracticeOdsCode(),
                    migrationStatusLog.getDate().toInstant()
                );
            }else{
                sendAckMessage(payload, conversationId);
            }

        } catch (BundleMappingException | DataFormatException | JsonProcessingException
                 | InlineAttachmentProcessingException | AttachmentNotFoundException ex) {
            sendNackMessage(EHR_EXTRACT_CANNOT_BE_PROCESSED, payload, conversationId);
            throw ex;
        } catch (ParseException ex) {
            throw ex;
        }
    }

    private boolean checkIfMessageHasASkeleton(List<InboundMessage.ExternalAttachment> externalAttachmentList) throws SAXException {
        for (InboundMessage.ExternalAttachment externalAttachment: externalAttachmentList) {
            if (parseIsSkeleton(externalAttachment.getDescription())){
                return true;
            }
        }
        return false;
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

    public boolean sendNackMessage(NACKReason reason, RCMRIN030000UK06Message payload, String conversationId) {

        LOGGER.debug("Sending NACK message with acknowledgement code [{}] for message EHR Extract message [{}]", reason.getCode(),
            payload.getId().getRoot());

        migrationStatusLogService.addMigrationStatusLog(reason.getMigrationStatus(), conversationId);

        return sendNACKMessageHandler.prepareAndSendMessage(prepareNackMessageData(
            reason,
            payload,
            conversationId
        ));
    }

    public boolean sendAckMessage(RCMRIN030000UK06Message payload, String conversationId) {

        LOGGER.debug("Sending ACK message for message with Conversation ID: [{}]", conversationId);

        return sendACKMessageHandler.prepareAndSendMessage(prepareAckMessageData(
            payload,
            conversationId
        ));
    }

    private ContinueRequestData prepareContinueRequestData(
        RCMRIN030000UK06Message payload,
        String conversationId,
        String patientNhsNumber,
        String winningPracticeOdsCode,
        Instant mcciIN010000UK13creationTime
    ) {
        var fromAsid = parseFromAsid(payload);
        var toAsid = parseToAsid(payload);
        var toOdsCode = parseToOdsCode(payload);
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

    private ACKMessageData prepareAckMessageData(RCMRIN030000UK06Message payload,
        String conversationId) {

        String toOdsCode = parseToOdsCode(payload);
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

    private NACKMessageData prepareNackMessageData(NACKReason reason, RCMRIN030000UK06Message payload,
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

    private String parseFromAsid(RCMRIN030000UK06Message payload) {
        return payload.getCommunicationFunctionRcv()
            .get(0)
            .getDevice()
            .getId()
            .get(0)
            .getExtension();
    }

    private String parseToAsid(RCMRIN030000UK06Message payload) {
        return payload.getCommunicationFunctionSnd()
            .getDevice()
            .getId()
            .get(0)
            .getExtension();
    }

    private String parseToOdsCode(RCMRIN030000UK06Message payload) {
        return payload.getControlActEvent()
            .getSubject()
            .getEhrExtract()
            .getAuthor()
            .getAgentOrgSDS()
            .getAgentOrganizationSDS()
            .getId()
            .getExtension();
    }

    private String parseMessageRef(RCMRIN030000UK06Message payload) {
        return payload.getId().getRoot();
    }


    public String parseFilename(String description) throws ParseException {
        Pattern pattern = Pattern.compile("Filename=\"([A-Za-z\\d\\-_. ]*)\"");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new ParseException("Unable to parse originalFilename", 0);
    }

    public String parseContentType(String description) throws ParseException {
        Pattern pattern = Pattern.compile("ContentType=([A-Za-z\\d\\-/]*)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new ParseException("Unable to parse ContentType", 0);
    }

    public boolean parseIsCompressed(String description) throws ParseException {
        Pattern pattern = Pattern.compile("Compressed=(Yes|No)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1).equals("Yes");
        }
        throw new ParseException("Unable to parse isCompressed", 0);
    }

    public boolean parseIsLargeAttachment(String description) throws ParseException {
        Pattern pattern = Pattern.compile("LargeAttachment=(Yes|No)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1).equals("Yes");
        }
        throw new ParseException("Unable to parse isLargeAttachment", 0);
    }

    public boolean parseIsOriginalBase64(String description) throws ParseException {
        Pattern pattern = Pattern.compile("OriginalBase64=(Yes|No)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1).equals("Yes");
        }
        throw new ParseException("Unable to parse isOriginalBase64", 0);
    }

    public int parseFileLength(String description) throws ParseException {
        Pattern pattern = Pattern.compile("Length=([\\d]*)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public boolean parseIsSkeleton(String description) {

        final String EB_SKELETON_PROP = "X-GP2GP-Skeleton:Yes".toLowerCase();

        if (description.replaceAll("\\s+","").toLowerCase().contains(EB_SKELETON_PROP)) {
            return true;
        }
        return false;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private class EbxmlReference {
        private String description;
        private String href;
    }
}
