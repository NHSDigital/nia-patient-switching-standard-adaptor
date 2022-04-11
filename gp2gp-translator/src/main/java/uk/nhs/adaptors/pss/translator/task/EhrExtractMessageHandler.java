package uk.nhs.adaptors.pss.translator.task;

import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_GENERAL_PROCESSING_ERROR;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_LRG_MSG_ATTACHMENTS_NOT_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_LRG_MSG_GENERAL_FAILURE;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_LRG_MSG_REASSEMBLY_FAILURE;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_LRG_MSG_TIMEOUT;
import static uk.nhs.adaptors.pss.translator.model.NACKReason.EHR_EXTRACT_CANNOT_BE_PROCESSED;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import javax.xml.bind.JAXBException;

import lombok.extern.slf4j.Slf4j;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.MigrationStatusLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.ContinueRequestData;
import uk.nhs.adaptors.pss.translator.model.NACKMessageData;
import uk.nhs.adaptors.pss.translator.model.NACKReason;
import uk.nhs.adaptors.pss.translator.service.AttachmentHandlerService;
import uk.nhs.adaptors.pss.translator.service.BundleMapperService;
import uk.nhs.adaptors.pss.translator.service.XPathService;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

import java.time.Instant;

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



    public void handleMessage(InboundMessage inboundMessage, String conversationId) throws JAXBException, JsonProcessingException,
        SAXException, InlineAttachmentProcessingException {

        RCMRIN030000UK06Message payload = unmarshallString(inboundMessage.getPayload(), RCMRIN030000UK06Message.class);
        PatientMigrationRequest migrationRequest = migrationRequestDao.getMigrationRequest(conversationId);
        MigrationStatusLog migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);

        migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_RECEIVED, conversationId);

        try {
            var bundle = bundleMapperService.mapToBundle(payload, migrationRequest.getLoosingPracticeOdsCode());
            attachmentHandlerService.storeAttachments(inboundMessage.getAttachments(), conversationId);
            migrationStatusLogService.updatePatientMigrationRequestAndAddMigrationStatusLog(
                conversationId,
                fhirParser.encodeToJson(bundle),
                objectMapper.writeValueAsString(inboundMessage),
                EHR_EXTRACT_TRANSLATED
            );

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
                    sendContinueRequest(
                            payload,
                            conversationId,
                            patientNhsNumber,
                            migrationRequest.getWinningPracticeOdsCode(),
                            migrationStatusLog.getDate().toInstant()
                    );
                }
            }
        } catch (JsonProcessingException | InlineAttachmentProcessingException ex) {
            sendNackMessage(EHR_EXTRACT_CANNOT_BE_PROCESSED, payload, conversationId);
            throw ex;
        } catch (SAXException e) {
            LOGGER.error("failed to parse RCMR_IN030000UK06 ebxml: "
                + "failed to extract \"mid:\" from xlink:href, before sending the continue message", e);
            sendNackMessage(EHR_EXTRACT_CANNOT_BE_PROCESSED, payload, conversationId);
            throw e;
        }
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

    public boolean sendNackMessage(NACKReason reason, RCMRIN030000UK06Message payload, String conversationId) {

        LOGGER.debug("Sending NACK message with acknowledgement code [{}]", reason.getCode());

        MigrationStatus migrationStatus = switch (reason) {
            case LARGE_MESSAGE_ATTACHMENTS_NOT_RECEIVED -> ERROR_LRG_MSG_ATTACHMENTS_NOT_RECEIVED;
            case LARGE_MESSAGE_GENERAL_FAILURE -> ERROR_LRG_MSG_GENERAL_FAILURE;
            case LARGE_MESSAGE_REASSEMBLY_FAILURE -> ERROR_LRG_MSG_REASSEMBLY_FAILURE;
            case LARGE_MESSAGE_TIMEOUT -> ERROR_LRG_MSG_TIMEOUT;
            case CLINICAL_SYSTEM_INTEGRATION_FAILURE, UNEXPECTED_CONDITION, EHR_EXTRACT_CANNOT_BE_PROCESSED -> EHR_GENERAL_PROCESSING_ERROR;
        };

        migrationStatusLogService.addMigrationStatusLog(migrationStatus, conversationId);

        return sendNACKMessageHandler.prepareAndSendMessage(prepareNackMessageData(
            reason,
            payload,
            conversationId
        ));
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

    private String parseToAsid(RCMRIN030000UK06Message payload) {
        return payload.getCommunicationFunctionSnd()
            .getDevice()
            .getId()
            .get(0)
            .getExtension();
    }

    private String parseFromAsid(RCMRIN030000UK06Message payload) {
        return payload.getCommunicationFunctionRcv()
            .get(0)
            .getDevice()
            .getId()
            .get(0)
            .getExtension();
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
        var fromAsid = parseFromAsid(payload);
        var toAsid = parseToAsid(payload);
        var toOdsCode = parseToOdsCode(payload);
        var mcciIN010000UK13creationTimeToHl7Format = DateFormatUtil.toHl7Format(mcciIN010000UK13creationTime);

        return ContinueRequestData.builder()
            .conversationId(conversationId)
            .nhsNumber(patientNhsNumber)
            .fromAsid(fromAsid)
            .toAsid(toAsid) //losing practice ods code
            .toOdsCode(toOdsCode)
            .fromOdsCode(winningPracticeOdsCode)
            .mcciIN010000UK13creationTime(mcciIN010000UK13creationTimeToHl7Format)
            .build();
    }
}
