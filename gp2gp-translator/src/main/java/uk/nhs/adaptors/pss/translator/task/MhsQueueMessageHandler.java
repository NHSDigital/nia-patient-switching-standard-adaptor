package uk.nhs.adaptors.pss.translator.task;

import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;

import javax.jms.JMSException;
import javax.jms.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.amqp.JmsReader;
import uk.nhs.adaptors.pss.translator.mhs.exception.InvalidInboundMessageException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.service.EhrExtractTranslator;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MhsQueueMessageHandler {
    private final EhrExtractTranslator ehrExtractTranslator;
    private final PatientMigrationRequestDao patientMigrationRequestDao;
    private final MigrationStatusLogService migrationStatusLogService;
    private final FhirParser fhirParser;
    private final ObjectMapper objectMapper;

    public boolean handleMessage(Message message) {
        try {
            var messageBody = message.getBody(String.class); // todo sprawdzic w gp2gp
            // get nhs number from message?
            var patientNhsNumber = "";
            migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_RECEIVED, patientNhsNumber);

            var bundle = ehrExtractTranslator.translateEhrToFhirBundle(message.getBody(String.class));
            patientMigrationRequestDao.saveFhirResource(patientNhsNumber, fhirParser.encodeToJson(bundle));
            migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_TRANSLATED, patientNhsNumber);

            return true;
        } catch (JMSException e) {
            e.printStackTrace();
            return false;
        }
        // send continue
    }

    private ParsedInboundMessage parseMessage(Message message) {
        InboundMessage inboundMessage = unmarshallMessage(message);

        var ebXmlDocument = getMessageEnvelope(inboundMessage);
        var payloadDocument = getMessagePayload(inboundMessage);
        var conversationId = getConversationId(ebXmlDocument);
        var interactionId = getInteractionId(ebXmlDocument);
        var messageTimestamp = getJmsMessageTimestamp(message);

        return ParsedInboundMessage.builder()
            .ebXMLDocument(ebXmlDocument)
            .payloadDocument(payloadDocument)
            .rawPayload(inboundMessage.getPayload())
            .conversationId(conversationId)
            .interactionId(interactionId)
            .messageTimestamp(messageTimestamp)
            .build();
    }

    private InboundMessage unmarshallMessage(Message message) {
        try {
            var body = JmsReader.readMessage(message);
            return objectMapper.readValue(body, InboundMessage.class);
        } catch (JMSException e) {
            throw new InvalidInboundMessageException("Unable to read the content of the inbound MHS message", e);
        } catch (JsonProcessingException e) {
            throw new InvalidInboundMessageException("Content of the inbound MHS message is not valid JSON", e);
        }
    }
}
