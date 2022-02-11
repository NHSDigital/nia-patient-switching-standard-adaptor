package uk.nhs.adaptors.pss.translator.task;

import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.bind.JAXBException;

import org.hl7.v3.RCMRIN030000UK06Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.amqp.JmsReader;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.ContinueRequestData;
import uk.nhs.adaptors.pss.translator.model.EbXml;
import uk.nhs.adaptors.pss.translator.model.Reference;
import uk.nhs.adaptors.pss.translator.service.XPathService;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MhsQueueMessageHandler {
    private static final String CONVERSATION_ID_PATH = "/Envelope/Header/MessageHeader/ConversationId";
    private static final String REFERENCE_PATH = "/Envelope/Body/Manifest/Reference";
    private static final String DESCRIPTION_PATH = "/Description";
    private static final String ID_ATTRIBUTE_NAME = "id";
    private static final String HREF_ATTRIBUTE_NAME = "href";

    private final PatientMigrationRequestDao patientMigrationRequestDao;
    private final MigrationStatusLogService migrationStatusLogService;
    private final FhirParser fhirParser;
    private final ObjectMapper objectMapper;
    private final XPathService xPathService;
    private final SendContinueRequestHandler sendContinueRequestHandler;
//    private final BundleMapperService bundleMapperService;

    public boolean handleMessage(Message message) {
        try {
            InboundMessage inboundMessage = readMessage(message);
            RCMRIN030000UK06Message payload = unmarshallString(inboundMessage.getPayload(), RCMRIN030000UK06Message.class);
            EbXml ebXml = getEbXmlData(inboundMessage.getEbXML());

            // todo wyciagnac SOAP env i zapisac info do bazy z ebXML i zdecydowac co jest handlowane
            var patientNhsNumber = retrieveNhsNumber(payload);
            migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_RECEIVED, patientNhsNumber);

//            var bundle = bundleMapperService.mapToBundle(payload);
//            patientMigrationRequestDao.saveFhirResource(patientNhsNumber, fhirParser.encodeToJson(bundle));
            migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_TRANSLATED, patientNhsNumber);

            return sendContinueRequest(payload, ebXml, patientNhsNumber);
        } catch (JMSException | JAXBException e) {
            LOGGER.error("Unable to read the content of the inbound MHS message", e);
            return false;
        } catch (JsonProcessingException e) {
            LOGGER.error("Content of the inbound MHS message is not valid JSON", e);
            return false;
        } catch (SAXException e) {
            LOGGER.error("Unable to read the content of the XML envelope (ebxml) of the inbound MHS message", e);
            return false;
        }
    }

    private InboundMessage readMessage(Message message) throws JMSException, JsonProcessingException {
        var body = JmsReader.readMessage(message);
        return objectMapper.readValue(body, InboundMessage.class);
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

    private EbXml getEbXmlData(String ebXmlString) throws SAXException {
        Document ebXmlDocument = xPathService.parseDocumentFromXml(ebXmlString);
        String conversationId = xPathService.getNodeValue(ebXmlDocument, CONVERSATION_ID_PATH);
        NodeList referenceNodes = xPathService.getNodes(ebXmlDocument, REFERENCE_PATH);
        List<Reference> references = retrieveReferences(referenceNodes);

        var ebXml = new EbXml();
        ebXml.setConversationId(conversationId);
        ebXml.setReferences(references);
        return ebXml;
    }

    private List<Reference> retrieveReferences(NodeList referenceNodes) {
        List<Reference> references = new ArrayList<>();
        for (int i = 0; i < referenceNodes.getLength(); i++) {
            Node referenceNode = referenceNodes.item(i);
            NamedNodeMap nodeAttributes = referenceNode.getAttributes();
            String description = xPathService.getNodeValue((Document) referenceNode, DESCRIPTION_PATH);
            Reference reference = Reference.builder()
                .description(description)
                .id(nodeAttributes.getNamedItem(ID_ATTRIBUTE_NAME).getNodeValue())
                .href(nodeAttributes.getNamedItem(HREF_ATTRIBUTE_NAME).getNodeValue())
                .build();
            references.add(reference);
        }

        return references;
    }

    private boolean sendContinueRequest(RCMRIN030000UK06Message payload, EbXml ebXml, String patientNhsNumber) {
        var continueRequestData = prepareContinueRequestData(payload, ebXml, patientNhsNumber);
        return sendContinueRequestHandler.prepareAndSendRequest(continueRequestData);
    }

    private ContinueRequestData prepareContinueRequestData(RCMRIN030000UK06Message payload, EbXml ebXml, String patientNhsNumber) {
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
            .conversationId(ebXml.getConversationId())
            .nhsNumber(patientNhsNumber)
            .fromAsid(fromAsid)
            .toAsid(toAsid)
            .toOdsCode(toOdsCode)
            .build();
    }
}
