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
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
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

    public void handleMessage(InboundMessage inboundMessage, String conversationId) throws JAXBException, JsonProcessingException,
        SAXException, InlineAttachmentProcessingException, BundleMappingException, AttachmentNotFoundException {

        RCMRIN030000UK06Message payload = unmarshallString(inboundMessage.getPayload(), RCMRIN030000UK06Message.class);
        PatientMigrationRequest migrationRequest = migrationRequestDao.getMigrationRequest(conversationId);
        MigrationStatusLog migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);

        migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_RECEIVED, conversationId);


        try {
            if(checkIfMessageHasASkeleton(inboundMessage)) {
                attachmentHandlerService.storeAttachments(inboundMessage.getAttachments(), conversationId);

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
            if (inboundMessage.getEbXML().contains("mid:")) {
                String patientNhsNumber = payload
                    .getControlActEvent()
                    .getSubject()
                    .getEhrExtract()
                    .getRecordTarget()
                    .getPatient()
                    .getId()
                    .getExtension();

                if (checkIfEHRExtractIsHasAttachments(inboundMessage)) {
                    // Check for DomainData="X-GP2GP-Skeleton: Yes"

                    // Check each reference node for mid, then insert each
                    // mid =mid, filename=filename
                    // patientMigration= get from conversationId on patient_migration_request
                    // contentType=contentType
                    // compress=compressed
                    // largeAttachment=largeAttachment,base64=base64,skeleton=is this file a DomainData-skeleton
                    // ordernum=order of what it comes in
                    // lengthNum=how many mid attachments are there?
                    sendContinueRequest(
                        payload,
                        conversationId,
                        patientNhsNumber,
                        migrationRequest.getWinningPracticeOdsCode(),
                        migrationStatusLog.getDate().toInstant()
                    );
                }
            }else{
                sendAckMessage(payload, conversationId);
            }

        } catch (BundleMappingException | DataFormatException | JsonProcessingException
                 | InlineAttachmentProcessingException | AttachmentNotFoundException ex) {
            sendNackMessage(EHR_EXTRACT_CANNOT_BE_PROCESSED, payload, conversationId);
            throw ex;
        } catch (SAXException e) {
            LOGGER.error("failed to parse RCMR_IN030000UK06 ebxml: "
                    + "failed to extract \"mid:\" from xlink:href, before sending the continue message", e);
            sendNackMessage(EHR_EXTRACT_CANNOT_BE_PROCESSED, payload, conversationId);
            throw e;
        }
    }

    private boolean checkIfMessageHasASkeleton(InboundMessage inboundMessage) throws SAXException {

        final String REFERENCES_ATTACHMENTS_PATH = "/Envelope/Body/Manifest/Reference";
        final String EB_SKELETON_PROP = "X-GP2GP-Skeleton:Yes";

        Document ebXmlDocument = xPathService.parseDocumentFromXml(inboundMessage.getEbXML());

        NodeList referencesAttachment = xPathService.getNodes(ebXmlDocument, REFERENCES_ATTACHMENTS_PATH);

        int sizeA = referencesAttachment.getLength();
        for (int index = 0; index < referencesAttachment.getLength(); index++) {

            Node referenceNode = referencesAttachment.item(index); //Reference

            if (referenceNode.getNodeType() == Node.ELEMENT_NODE) {

                Element referenceElement = (Element) referenceNode; //description
                String description = referenceElement.getTextContent();

                if (description.replaceAll("\\s+","").contains(EB_SKELETON_PROP)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkIfEHRExtractIsHasAttachments(InboundMessage inboundMessage) throws SAXException {
        final String REFERENCES_ATTACHMENTS_PATH = "/Envelope/Body/Manifest/Reference";
        Document ebXmlDocument = xPathService.parseDocumentFromXml(inboundMessage.getEbXML());

        if (ebXmlDocument == null) {
            return false;
        }
        NodeList referencesAttachment = xPathService.getNodes(ebXmlDocument, REFERENCES_ATTACHMENTS_PATH);

        if (referencesAttachment != null) {
            for (int index = 0; index < referencesAttachment.getLength(); index++) {

                Node referenceNode = referencesAttachment.item(index);
                if (referenceNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element reference = (Element) referenceNode;

                    String hrefAttribute2 = reference.getAttribute("xlink:href");

                    if (hrefAttribute2.startsWith("mid:")) {
                        return true;
                    }
                }
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
}
