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
import org.xmlsoap.schemas.soap.envelope.Envelope;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.amqp.JmsReader;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.service.EhrExtractTranslator;

@Slf4j
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
            InboundMessage inboundMessage = readMessage(message);
            RCMRIN030000UK06Message payload = unmarshallString(inboundMessage.getPayload(), RCMRIN030000UK06Message.class);
            Envelope ebXml = unmarshallString(inboundMessage.getEbXML(), Envelope.class);
            // todo wyciagnac SOAP env i zapisac info do bazy z ebXML i zdecydowac co jest handlowane
            var patientNhsNumber = retrieveNhsNumber(payload);
            migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_RECEIVED, patientNhsNumber);

            var bundle = ehrExtractTranslator.translateEhrToFhirBundle(payload);
            patientMigrationRequestDao.saveFhirResource(patientNhsNumber, fhirParser.encodeToJson(bundle));
            migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_TRANSLATED, patientNhsNumber);

            return true;
        } catch (JMSException | JAXBException e) {
            LOGGER.error("Unable to read the content of the inbound MHS message", e);
            return false;
        } catch (JsonProcessingException e) {
            LOGGER.error("Content of the inbound MHS message is not valid JSON", e);
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
}
