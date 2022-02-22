package uk.nhs.adaptors.pss.translator.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.service.XPathService;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AcknowledgmentMessageHandler {
    private static final String ACK_TYPE_CODE_XPATH = "//MCCI_IN010000UK13/acknowledgement/@typeCode";

    private final XPathService xPathService;
    private final MigrationStatusLogService migrationStatusLogService;

    public void handleMessage(InboundMessage inboundMessage, String conversationId) throws SAXException {
        Document document = xPathService.parseDocumentFromXml(inboundMessage.getPayload());
        String ackTypeCode = xPathService.getNodeValue(document, ACK_TYPE_CODE_XPATH);

        migrationStatusLogService.addMigrationStatusLog(decideMigrationStatus, conversationId);
    }
    
    private MigrationStatus decideMigrationStatus(String ackTypeCode) {

    }
}
