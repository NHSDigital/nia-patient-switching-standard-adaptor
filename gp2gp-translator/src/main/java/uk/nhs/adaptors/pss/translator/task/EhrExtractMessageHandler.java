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
import uk.nhs.adaptors.connector.model.MigrationStatusLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.ContinueRequestData;
import uk.nhs.adaptors.pss.translator.service.BundleMapperService;
import uk.nhs.adaptors.pss.translator.service.XPathService;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

import java.time.Instant;

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
        PatientMigrationRequest migrationRequest = migrationRequestDao.getMigrationRequest(conversationId);
        MigrationStatusLog migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);

        migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_RECEIVED, conversationId);

        var bundle = bundleMapperService.mapToBundle(payload, migrationRequest.getLoosingPracticeOdsCode());
        migrationStatusLogService.updatePatientMigrationRequestAndAddMigrationStatusLog(
            conversationId,
            fhirParser.encodeToJson(bundle),
            objectMapper.writeValueAsString(inboundMessage),
            EHR_EXTRACT_TRANSLATED
        );

        try {
            final String REFERENCES_ATTACHMENTS_PATH = "/Envelope/Body/Manifest/Reference";
            Document ebXmlDocument = xPathService.parseDocumentFromXml(inboundMessage.getEbXML()); //xml document

            if (ebXmlDocument == null) {
                return;
            }
            NodeList referencesAttachment = xPathService.getNodes(ebXmlDocument, REFERENCES_ATTACHMENTS_PATH); //node of references

            if (referencesAttachment != null) {
                for (int index = 0; index < referencesAttachment.getLength(); index++) {

                    Node referenceNode = referencesAttachment.item(index);
                    if (referenceNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element reference = (Element) referenceNode;

                        String hrefAttribute2 = reference.getAttribute("xlink:href");

                        if (hrefAttribute2.startsWith("mid:")) {
                            String patientNhsNumber = payload
                                .getControlActEvent()
                                .getSubject()
                                .getEhrExtract()
                                .getRecordTarget()
                                .getPatient()
                                .getId()
                                .getExtension();

                            sendContinueRequest(
                                    payload,
                                    conversationId,
                                    patientNhsNumber,
                                    migrationRequest.getWinningPracticeOdsCode(),
                                    migrationStatusLog.getDate().toInstant()
                            );
                            break;
                        }
                    }
                }
            }
        } catch (SAXException e) {
            //LOGGER.debug("failed to extract \"mid:\" from xlink:href. ");
            e.printStackTrace(); //need to change
        }
    }

    private boolean sendContinueRequest(
            RCMRIN030000UK06Message payload,
            String conversationId,
            String patientNhsNumber,
            String winningPracticeOdsCode,
            Instant mcciIN010000UK13creationTime
    ) {
        return sendContinueRequestHandler.prepareAndSendRequest(
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

        var fromOdsCode = winningPracticeOdsCode;

        var mcciIN010000UK13creationTimeToHl7Format = DateFormatUtil.toHl7Format(mcciIN010000UK13creationTime);

        return ContinueRequestData.builder()
            .conversationId(conversationId)
            .nhsNumber(patientNhsNumber)
            .fromAsid(fromAsid)
            .toAsid(toAsid) //losing practice ods code
            .toOdsCode(toOdsCode)
            .fromOdsCode(fromOdsCode)
            .mcciIN010000UK13creationTime(mcciIN010000UK13creationTimeToHl7Format)
            .build();
    }
}
