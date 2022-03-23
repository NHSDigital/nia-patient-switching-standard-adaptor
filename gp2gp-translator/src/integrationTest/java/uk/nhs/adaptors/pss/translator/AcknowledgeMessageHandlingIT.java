package uk.nhs.adaptors.pss.translator;

import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_ACKNOWLEDGED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK;

import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith({SpringExtension.class})
@DirtiesContext
@AutoConfigureMockMvc
public class AcknowledgeMessageHandlingIT {
    private static final int NHS_NUMBER_MIN_MAX_LENGTH = 10;
    private static final String EBXML_PART_PATH = "/xml/MCCI_IN010000UK13/ebxml_part.xml";
    private static final String PAYLOAD_PART_PATH = "/xml/MCCI_IN010000UK13/payload_part.xml";
    private static final String TYPE_CODE_PLACEHOLDER = "{{typeCode}}";
    private static final String CONVERSATION_ID_PLACEHOLDER = "{{conversationId}}";
    private static final String LOOSING_ODS_CODE = "K547";

    @Autowired
    private PatientMigrationRequestDao patientMigrationRequestDao;

    @Autowired
    private MigrationStatusLogService migrationStatusLogService;

    @Qualifier("jmsTemplateMhsQueue")
    @Autowired
    private JmsTemplate mhsJmsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String conversationId;

    @BeforeEach
    public void setUp() {
        conversationId = generateConversationId();
        patientMigrationRequestDao.addNewRequest(generatePatientNhsNumber(), conversationId, LOOSING_ODS_CODE);
    }

    @Test
    public void handlePositiveAcknowledgeMessageFromQueue() {
        sendAcknowledgementMessageToQueue("AA");

        // verify if correct status is set in the DB
        await().until(() -> isEhrExtractTranslated(EHR_EXTRACT_REQUEST_ACKNOWLEDGED));
    }

    @Test
    public void handleNegativeAcknowledgeMessageFromQueue() {
        sendAcknowledgementMessageToQueue("AE");

        // verify if correct status is set in the DB
        await().until(() -> isEhrExtractTranslated(EHR_EXTRACT_REQUEST_NEGATIVE_ACK));
    }

    private String generatePatientNhsNumber() {
        return RandomStringUtils.randomNumeric(NHS_NUMBER_MIN_MAX_LENGTH, NHS_NUMBER_MIN_MAX_LENGTH);
    }

    private String generateConversationId() {
        return UUID.randomUUID().toString();
    }

    private void sendAcknowledgementMessageToQueue(String typeCode) {
        var inboundMessage = createInboundMessage(typeCode);
        mhsJmsTemplate.send(session -> session.createTextMessage(parseMessageToString(inboundMessage)));
    }

    private InboundMessage createInboundMessage(String typeCode) {
        var inboundMessage = new InboundMessage();
        var payload = readResourceAsString(PAYLOAD_PART_PATH).replace(TYPE_CODE_PLACEHOLDER, typeCode);
        var ebXml = readResourceAsString(EBXML_PART_PATH).replace(CONVERSATION_ID_PLACEHOLDER, conversationId);
        inboundMessage.setPayload(payload);
        inboundMessage.setEbXML(ebXml);
        return inboundMessage;
    }

    private boolean isEhrExtractTranslated(MigrationStatus expectedStatus) {
        var migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);
        return expectedStatus.equals(migrationStatusLog.getMigrationStatus());
    }

    @SneakyThrows
    private String parseMessageToString(InboundMessage inboundMessage) {
        return objectMapper.writeValueAsString(inboundMessage);
    }
}
