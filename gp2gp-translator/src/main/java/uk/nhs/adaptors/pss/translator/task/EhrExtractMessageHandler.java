package uk.nhs.adaptors.pss.translator.task;

import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.pss.translator.model.NACKReason.EHR_EXTRACT_CANNOT_BE_PROCESSED;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
import uk.nhs.adaptors.pss.translator.exception.BundleMappingException;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.*;
import uk.nhs.adaptors.pss.translator.service.AttachmentHandlerService;
import uk.nhs.adaptors.pss.translator.service.BundleMapperService;
import uk.nhs.adaptors.pss.translator.service.XPathService;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

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
    private final SendNACKMessageHandler sendNACKMessageHandler;
    private final SendACKMessageHandler sendACKMessageHandler;
    private final PatientAttachmentLogService patientAttachmentLogService;

    public void handleMessage(InboundMessage inboundMessage, String conversationId) throws JAXBException, JsonProcessingException, SAXException, InlineAttachmentProcessingException, BundleMappingException, ParseException {

        RCMRIN030000UK06Message payload = unmarshallString(inboundMessage.getPayload(), RCMRIN030000UK06Message.class);
        PatientMigrationRequest migrationRequest = migrationRequestDao.getMigrationRequest(conversationId);
        MigrationStatusLog migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);

        migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_RECEIVED, conversationId);

        try {
            List<EbxmlReference> attachmentReferenceDescription = new ArrayList<>();
            attachmentReferenceDescription.addAll(getEbxmlAttachmentsData(inboundMessage));

            //need to test if bellow
            if(!checkIfMessageHasASkeleton(attachmentReferenceDescription)){
                var bundle = bundleMapperService.mapToBundle(payload, migrationRequest.getLosingPracticeOdsCode());
                attachmentHandlerService.storeAttachments(inboundMessage.getAttachments(), conversationId);
                migrationStatusLogService.updatePatientMigrationRequestAndAddMigrationStatusLog(
                        conversationId,
                        fhirParser.encodeToJson(bundle),
                        objectMapper.writeValueAsString(inboundMessage),
                        EHR_EXTRACT_TRANSLATED
                );
            }

            ////sending continue message
            if (inboundMessage.getEbXML().contains("mid:")) {
                String patientNhsNumber = payload
                    .getControlActEvent()
                    .getSubject()
                    .getEhrExtract()
                    .getRecordTarget()
                    .getPatient()
                    .getId()
                    .getExtension();

                //change test
                if (checkIfEhrExtractIsHasAttachments(attachmentReferenceDescription)) {
                    //need to test if bellow
                    if(checkIfMessageHasASkeleton(attachmentReferenceDescription)){

                        String extractFileName = conversationId + "_" + parseMessageRef(payload) + "_payload";

                        //save extract in storage
                        attachmentHandlerService.storeEhrExtract(
                                extractFileName,
                                inboundMessage.getPayload(),
                                conversationId,
                                "application/xml; charset=UTF-8"
                        );

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

                        patientAttachmentLogService.addAttachmentLog(patientExtractAttachmentLog);

                        //save COPC_UK01 messages

                        for (int index = 0; index < attachmentReferenceDescription.size(); index++) {

                            if(attachmentReferenceDescription.get(index).getHref().contains("mid:")){
                                PatientAttachmentLog patientAttachmentLog = PatientAttachmentLog.builder()
                                        .mid(attachmentReferenceDescription.get(index).getHref().replace("mid:", ""))
                                        .filename(parseFilename(attachmentReferenceDescription.get(index).getDescription()))
                                        .parentMid(parseMessageRef(payload))
                                        .patientMigrationReqId(migrationRequest.getId())
                                        .contentType(parseContentType(attachmentReferenceDescription.get(index).getDescription()))
                                        .compressed(parseIsCompressed(attachmentReferenceDescription.get(index).getDescription()))
                                        .largeAttachment(parseIsLargeAttachment(attachmentReferenceDescription.get(index).getDescription())) //need to ask
                                        .base64(parseIsOriginalBase64(attachmentReferenceDescription.get(index).getDescription()))
                                        .skeleton(parseIsSkeleton(attachmentReferenceDescription.get(index).getDescription()))
                                        .uploaded(false)
                                        .lengthNum(parseFileLength(attachmentReferenceDescription.get(index).getDescription()))
                                        .orderNum(index)
                                        .build();
                                patientAttachmentLogService.addAttachmentLog(patientAttachmentLog);
                            }

                        }
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
            }

        } catch (BundleMappingException | DataFormatException | JsonProcessingException | InlineAttachmentProcessingException ex) {
            sendNackMessage(EHR_EXTRACT_CANNOT_BE_PROCESSED, payload, conversationId);
            throw ex;
        } catch (SAXException e) {
            LOGGER.error("failed to parse RCMR_IN030000UK06 ebxml: "
                + "failed to extract \"mid:\" from xlink:href, before sending the continue message", e);
            sendNackMessage(EHR_EXTRACT_CANNOT_BE_PROCESSED, payload, conversationId);
            throw e;
        } catch (ParseException ex) {
            throw ex;
        }
    }

    private boolean checkIfMessageHasASkeleton(List<EbxmlReference> ebxmlReferenceList) throws SAXException {
        for (EbxmlReference description: ebxmlReferenceList) {
            if (parseIsSkeleton(description.getDescription())){
                return true;
            }
        }
        return false;
    }

    public boolean checkIfEhrExtractIsHasAttachments(List<EbxmlReference> ebxmlReferenceList) throws SAXException {
        for (int index = 0; index < ebxmlReferenceList.size(); index++) {

            String hrefAttribute2 = ebxmlReferenceList.get(index).getHref();

            if (hrefAttribute2.startsWith("mid:")) {
                return true;
            }
        }
        return false;
    }

    private List<EbxmlReference> getEbxmlAttachmentsData(InboundMessage inboundMessage) throws SAXException {
        List<EbxmlReference> ebxmlAttachmentsIds = new ArrayList<>();
        final String REFERENCES_ATTACHMENTS_PATH = "/Envelope/Body/Manifest/Reference";
        Document ebXmlDocument = xPathService.parseDocumentFromXml(inboundMessage.getEbXML());
        NodeList referencesAttachment = xPathService.getNodes(ebXmlDocument, REFERENCES_ATTACHMENTS_PATH);

        for (int index = 0; index < referencesAttachment.getLength(); index++) {

            Node referenceNode = referencesAttachment.item(index); //Reference

            if (referenceNode.getNodeType() == Node.ELEMENT_NODE) {

                Element referenceElement = (Element) referenceNode; //description

                String description = referenceElement.getTextContent();
                String hrefAttribute = referenceElement.getAttribute("xlink:href");

                ebxmlAttachmentsIds.add(new EbxmlReference(description, hrefAttribute));
            }
        }

        return ebxmlAttachmentsIds;
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


    private String parseFilename(String description) throws ParseException {
        Pattern pattern = Pattern.compile("Filename=\"([A-Za-z\\d\\-_. ]*)\"");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new ParseException("Unable to parse originalFilename", 0);
    }

    private String parseContentType(String description) throws ParseException {
        Pattern pattern = Pattern.compile("ContentType=([A-Za-z\\d\\-/]*)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new ParseException("Unable to parse ContentType", 0);
    }

    private boolean parseIsCompressed(String description) throws ParseException {
        Pattern pattern = Pattern.compile("Compressed=(Yes|No)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1).equals("Yes");
        }

        throw new ParseException("Unable to parse isCompressed", 0);
    }

    private boolean parseIsLargeAttachment(String description) throws ParseException {
        Pattern pattern = Pattern.compile("LargeAttachment=(Yes|No)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1).equals("Yes");
        }

        throw new ParseException("Unable to parse isLargeAttachment", 0);
    }

    private boolean parseIsOriginalBase64(String description) throws ParseException {
        Pattern pattern = Pattern.compile("OriginalBase64=(Yes|No)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1).equals("Yes");
        }

        throw new ParseException("Unable to parse isOriginalBase64", 0);
    }

    private int parseFileLength(String description) throws ParseException {
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

    private boolean parseIsSkeleton(String description) {

        final String EB_SKELETON_PROP = "X-GP2GP-Skeleton:Yes";

        if (description.replaceAll("\\s+","").contains(EB_SKELETON_PROP)) {
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
