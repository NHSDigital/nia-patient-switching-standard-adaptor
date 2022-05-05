package uk.nhs.adaptors.pss.translator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.v3.COPCIN000001UK01Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.exception.AttachmentLogException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;

import javax.xml.bind.JAXBException;
import java.util.Comparator;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InboundMessageMergingService {
    private static final String MESSAGE_ID_PATH = "/Envelope/Header/MessageHeader/MessageData/MessageId";
    private final PatientAttachmentLogService patientAttachmentLogService;
    private final XPathService xPathService;
    private final BundleMapperService bundleMapperService;
    private final PatientMigrationRequestDao migrationRequestDao;

    public void mergeAndBundleMessage(InboundMessage inboundMessage, String conversationId) throws AttachmentLogException, SAXException, JAXBException {

        Document ebXmlDocument = xPathService.parseDocumentFromXml(inboundMessage.getEbXML());
        var inboundMessageId = xPathService.getNodeValue(ebXmlDocument, MESSAGE_ID_PATH);
        var currentAttachmentLog = patientAttachmentLogService.findAttachmentLog(inboundMessageId, conversationId);

        if (currentAttachmentLog == null) {
            throw new AttachmentLogException("Given COPC message is missing an attachment log");
        }

        var conversationAttachmentLogs = patientAttachmentLogService.findAttachmentLogs(conversationId);
        var attachmentLogFragments = conversationAttachmentLogs.stream()
                .sorted(Comparator.comparingInt(PatientAttachmentLog::getOrderNum))
                .filter(log -> log.getParentMid().equals(currentAttachmentLog.getParentMid()))
                .toList();

        var parentLogMessageId = attachmentLogFragments.size() == 1
                ? currentAttachmentLog.getMid()
                : currentAttachmentLog.getParentMid();

        // is this correct pull?
        // if yes, copied from rio's code, need refactor to reuse
        attachmentLogFragments = conversationAttachmentLogs.stream()
                .sorted(Comparator.comparingInt(PatientAttachmentLog::getOrderNum))
                .filter(log -> log.getParentMid().equals(parentLogMessageId))
                .toList();


        var allFragmentsHaveUploaded = attachmentLogFragments.stream()
                .allMatch(PatientAttachmentLog::getUploaded);

        if (!allFragmentsHaveUploaded) {
            return;
        }
        
        if (currentAttachmentLog.getSkeleton()) {
            // merge ehr extracts and put into Narrative statement

            // update inbound message

        }

        // update attachment links

        // map to bundle
        COPCIN000001UK01Message payload = unmarshallString(inboundMessage.getPayload(), COPCIN000001UK01Message.class);
        // PatientMigrationRequest migrationRequest = migrationRequestDao.getMigrationRequest(conversationId);
        // var bundle = bundleMapperService.mapToBundle(payload, migrationRequest.getLosingPracticeOdsCode());
    }
}
