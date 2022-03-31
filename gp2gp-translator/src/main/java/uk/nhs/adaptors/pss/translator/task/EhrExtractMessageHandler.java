package uk.nhs.adaptors.pss.translator.task;

import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import javax.xml.bind.JAXBException;

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
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.ContinueRequestData;
import uk.nhs.adaptors.pss.translator.service.BundleMapperService;
import uk.nhs.adaptors.pss.translator.service.XPathService;

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

    public void handleMessage(InboundMessage inboundMessage, String conversationId) throws JAXBException, JsonProcessingException {
        RCMRIN030000UK06Message payload = unmarshallString(inboundMessage.getPayload(), RCMRIN030000UK06Message.class);
        migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_RECEIVED, conversationId);
        PatientMigrationRequest migrationRequest = migrationRequestDao.getMigrationRequest(conversationId);

        var bundle = bundleMapperService.mapToBundle(payload, migrationRequest.getLoosingPracticeOdsCode());
        migrationStatusLogService.updatePatientMigrationRequestAndAddMigrationStatusLog(
            conversationId,
            fhirParser.encodeToJson(bundle),
            objectMapper.writeValueAsString(inboundMessage),
            EHR_EXTRACT_TRANSLATED
        );


        try {
            String REFERENCES_ATTACHMENTS_PATH = "/Envelope/Body/Manifest/Reference";
            Document ebXmlDocument = xPathService.parseDocumentFromXml(inboundMessage.getEbXML()); //xml document
            NodeList referencesAttachment = xPathService.getNodes(ebXmlDocument, REFERENCES_ATTACHMENTS_PATH); //node of references

            for (int index = 0; index < referencesAttachment.getLength() ; index++)
            {
                Node referenceNode = referencesAttachment.item(index);
                if (referenceNode.getNodeType() == Node.ELEMENT_NODE){
                    Element reference = (Element) referenceNode;

                    String hrefAttribute2 = reference.getAttribute("xlink:href");

                    if(hrefAttribute2.startsWith("mid:")){
                        String patientNhsNumber = payload.getControlActEvent().getSubject().getEhrExtract().getRecordTarget().getPatient().getId().getExtension();
                        sendContinueRequest(payload, conversationId, patientNhsNumber);
                        break;
                    }
                }
                System.out.println("Ado" + index + "Ado MARTINS 34");
            }
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    //TODO: this method is related to the large messaging epic and should be called after saving translated Boundle resource.
    //  Can be used during implementation of NIAD-2045
    private boolean sendContinueRequest(RCMRIN030000UK06Message payload, String conversationId, String patientNhsNumber) {
        return sendContinueRequestHandler.prepareAndSendRequest(prepareContinueRequestData(payload, conversationId, patientNhsNumber));
    }
    /*
    Requirement

        As a consumer of PSS adaptorI want the adaptor to send CONTINUE message when large
        messages existSo that the remaining large messages could be sent by the losing practice.

    Prerequisites

        EHR Extract message is received - either large or simple.

    Acceptance Criteria

        If there exists an eb:Reference element in ebXML SOAP payload that points
        to a message via xlink:href="mid:<message_id>" then the COPC_IN000001UK01       //some files have a cid reference. what to do here?
        Continue message is sent


     */

    //TODO: this method is only used inside sendContinueRequest() method above
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
