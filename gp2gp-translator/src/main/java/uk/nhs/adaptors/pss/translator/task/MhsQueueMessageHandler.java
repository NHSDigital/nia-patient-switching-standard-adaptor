package uk.nhs.adaptors.pss.translator.task;

import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.bind.JAXBException;

import org.hl7.v3.RCMRIN030000UK06Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.common.service.MDCService;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.amqp.JmsReader;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.ContinueRequestData;
import uk.nhs.adaptors.pss.translator.service.BundleMapperService;
import uk.nhs.adaptors.pss.translator.service.XPathService;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MhsQueueMessageHandler {
    private static final String EHR_EXTRACT_INTERACTION_ID = "RCMR_IN030000UK06";
    private static final String CONVERSATION_ID_PATH = "/Envelope/Header/MessageHeader/ConversationId";
    private static final String INTERACTION_ID_PATH = "/Envelope/Header/MessageHeader/Action";

    private final PatientMigrationRequestDao patientMigrationRequestDao;
    private final MigrationStatusLogService migrationStatusLogService;
    private final FhirParser fhirParser;
    private final ObjectMapper objectMapper;
    private final BundleMapperService bundleMapperService;
    private final JmsReader jmsReader;
    private final XPathService xPathService;
    private final MDCService mdcService;

    public boolean handleMessage(Message message) {
        try {
            InboundMessage inboundMessage = readMessage(message);
            Document ebXmlDocument = xPathService.parseDocumentFromXml(inboundMessage.getEbXML());
            extractAndApplyConversationId(ebXmlDocument);

            String interactionId = xPathService.getNodeValue(ebXmlDocument, INTERACTION_ID_PATH);

            if (EHR_EXTRACT_INTERACTION_ID.equals(interactionId)) {
                handleEhrExtractMessage(inboundMessage);
            } else {
                LOGGER.info("Handling message of [{}] interaction id not implemented", interactionId);
            }
            return true;
        } catch (JMSException | JAXBException | SAXException e) {
            LOGGER.error("Unable to read the content of the inbound MHS message", e);
            return false;
        } catch (JsonProcessingException e) {
            LOGGER.error("Content of the inbound MHS message is not valid JSON", e);
            return false;
        }
    }

    private void extractAndApplyConversationId(Document ebXmlDocument) {
        mdcService.applyConversationId(xPathService.getNodeValue(ebXmlDocument, CONVERSATION_ID_PATH));
    }

    private InboundMessage readMessage(Message message) throws JMSException, JsonProcessingException {
        var body = jmsReader.readMessage(message);
        return objectMapper.readValue(body, InboundMessage.class);
    }

    private void handleEhrExtractMessage(InboundMessage inboundMessage) throws JAXBException, JsonProcessingException {
        RCMRIN030000UK06Message payload = unmarshallString(inboundMessage.getPayload(), RCMRIN030000UK06Message.class);
        var patientNhsNumber = retrieveNhsNumber(payload);
        migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_RECEIVED, patientNhsNumber);

        var bundle = bundleMapperService.mapToBundle(payload);
        patientMigrationRequestDao.saveBundleAndInboundMessageData(
            patientNhsNumber,
            fhirParser.encodeToJson(bundle),
            objectMapper.writeValueAsString(inboundMessage));
        migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_TRANSLATED, patientNhsNumber);
    }

    private String retrieveNhsNumber(RCMRIN030000UK06Message message) {
        return message
            .getControlActEvent()
            .getSubject()
            .getEhrExtract()
            .getRecordTarget()
            .getPatient()
            .getId()
            .getExtension();
    }

    // TODO: this method is related to the large messaging epic and should be called after saving translated Boundle resource.
    //  Can be used during implementation of NIAD-2045
    private boolean sendContinueRequest(RCMRIN030000UK06Message payload, String conversationId, String patientNhsNumber) {
        // TODO: Should call
        //  sendContinueRequestHandler.prepareAndSendRequest(prepareContinueRequestData(payload, conversationId, patientNhsNumber));
        return true;
    }

    // TODO: this method is only used inside sendContinueRequest() method above
    private ContinueRequestData prepareContinueRequestData(
        RCMRIN030000UK06Message payload, String conversationId, String patientNhsNumber) {
        var fromAsid = payload.getCommunicationFunctionRcv()
            .get(0)
            .getDevice()
            .getId()
            .get(0)
            .getExtension();

        var toAsid = payload.getCommunicationFunctionSnd()
            .getDevice()
            .getId()
            .get(0)
            .getExtension();

        var toOdsCode = payload.getControlActEvent()
            .getSubject()
            .getEhrExtract()
            .getAuthor()
            .getAgentOrgSDS()
            .getAgentOrganizationSDS()
            .getId()
            .getExtension();

        return ContinueRequestData.builder()
            .conversationId(conversationId)
            .nhsNumber(patientNhsNumber)
            .fromAsid(fromAsid)
            .toAsid(toAsid)
            .toOdsCode(toOdsCode)
            .build();
    }
}
