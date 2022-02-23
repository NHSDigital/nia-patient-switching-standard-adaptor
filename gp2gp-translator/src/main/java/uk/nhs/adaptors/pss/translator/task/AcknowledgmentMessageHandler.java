package uk.nhs.adaptors.pss.translator.task;

import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_ACKNOWLEDGED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.service.XPathService;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AcknowledgmentMessageHandler {
    private static final String ACK_TYPE_CODE_XPATH = "//MCCI_IN010000UK13/acknowledgement/@typeCode";
    private static final String ACK_TYPE_CODE = "AA";
    private static final String NACK_TYPE_CODE = "AE";

    private final XPathService xPathService;
    private final MigrationStatusLogService migrationStatusLogService;

    public void handleMessage(InboundMessage inboundMessage, String conversationId) throws SAXException {
        Document document = xPathService.parseDocumentFromXml(inboundMessage.getPayload());
        String ackTypeCode = xPathService.getNodeValue(document, ACK_TYPE_CODE_XPATH);
        MigrationStatus migrationStatus = getMigrationStatus(ackTypeCode);

        if (migrationStatus != null) {
            migrationStatusLogService.addMigrationStatusLog(migrationStatus, conversationId);
        } else {
            LOGGER.info("Unknown acknowledgement typeCode [{}]", ackTypeCode);
        }
    }
    
    private MigrationStatus getMigrationStatus(String ackTypeCode) {
        return switch (ackTypeCode) {
            case ACK_TYPE_CODE -> EHR_EXTRACT_REQUEST_ACKNOWLEDGED;
            case NACK_TYPE_CODE -> EHR_EXTRACT_REQUEST_NEGATIVE_ACK;
            default -> null;
        };
    }
}
