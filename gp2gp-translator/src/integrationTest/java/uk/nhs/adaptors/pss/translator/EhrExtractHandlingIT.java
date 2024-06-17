package uk.nhs.adaptors.pss.translator;

import static org.awaitility.Awaitility.waitAtMost;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_REQUEST_ACCEPTED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.MIGRATION_COMPLETED;

import java.time.Duration;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.service.IdGeneratorService;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith({SpringExtension.class})
@DirtiesContext
@AutoConfigureMockMvc
public class EhrExtractHandlingIT {

    private static final int TEN = 10;
    private static final String NHS_NUMBER_PLACEHOLDER = "{{nhsNumber}}";

    @MockBean
    private IdGeneratorService idGeneratorService;

    @Autowired
    private PatientMigrationRequestDao patientMigrationRequestDao;

    @Autowired
    private MigrationStatusLogService migrationStatusLogService;

    @Qualifier("jmsTemplateMhsQueue")
    @Autowired
    private JmsTemplate mhsJmsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String patientNhsNumber;
    private String conversationId;

    public void setUpDeterministicRandomIds() {
        when(idGeneratorService.generateUuid()).thenAnswer(
                new Answer<String>() {
                    private int invocationCount = 0;
                    @Override
                    public String answer(InvocationOnMock invocation) {
                        return String.format("00000000-0000-0000-0000-%012d", invocationCount++);
                    }
                }
        );
    }

    @BeforeEach
    public void setUp() {
        patientNhsNumber = RandomStringUtils.randomNumeric(TEN, TEN);
        conversationId = UUID.randomUUID().toString().toUpperCase();
        setUpDeterministicRandomIds();
        startPatientMigrationJourney();
    }

    @Test
    public void handleEhrExtractFromQueue() throws JSONException {
        var expectedBundle = readResourceAsString("/json/expectedBundle.json")
            .replace(NHS_NUMBER_PLACEHOLDER, patientNhsNumber);

        var actualBundle = sendInboundMessageAndWaitForBundle(
            "/xml/RCMR_IN030000UK06/ebxml_part.xml",
            "/xml/RCMR_IN030000UK06/payload_part.xml"
        );

        JSONAssert.assertEquals(expectedBundle, actualBundle, true);
    }

    @Test
    public void handleEhrExtractWithConfidentialityCodeFromQueue() throws JSONException {
        var expectedBundle = readResourceAsString("/json/expectedBundle.json")
            .replace(NHS_NUMBER_PLACEHOLDER, patientNhsNumber);

        var actualBundle = sendInboundMessageAndWaitForBundle(
            "/xml/RCMR_IN030000UK07/ebxml_part.xml",
            "/xml/RCMR_IN030000UK07/payload_part_with_confidentiality_code.xml"
        );

        JSONAssert.assertEquals(expectedBundle, actualBundle, true);
    }

    private String sendInboundMessageAndWaitForBundle(String ebXmlPartPath, String payloadPartPath) {
        var inboundMessage = createInboundMessage(ebXmlPartPath, payloadPartPath);
        mhsJmsTemplate.send(session -> session.createTextMessage(parseMessageToString(inboundMessage)));
        waitAtMost(Duration.ofSeconds(TEN)).until(this::isEhrMigrationCompleted);

        var patientMigrationRequest = patientMigrationRequestDao.getMigrationRequest(conversationId);
        return patientMigrationRequest.getBundleResource();
    }

    private void startPatientMigrationJourney() {
        patientMigrationRequestDao.addNewRequest(patientNhsNumber, conversationId, "D5445", "ABC");
        migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_REQUEST_ACCEPTED, conversationId, null, null);
    }

    private InboundMessage createInboundMessage(String ebXmlPartPath, String payloadPartPath) {
        var inboundMessage = new InboundMessage();
        var payload = readResourceAsString(payloadPartPath)
            .replace(NHS_NUMBER_PLACEHOLDER, patientNhsNumber);
        var ebXml = readResourceAsString(ebXmlPartPath)
            .replace("{{conversationId}}", conversationId);

        inboundMessage.setPayload(payload);
        inboundMessage.setEbXML(ebXml);
        return inboundMessage;
    }

    private boolean isEhrMigrationCompleted() {
        var migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);
        return MIGRATION_COMPLETED.equals(migrationStatusLog.getMigrationStatus());
    }

    @SneakyThrows
    private String parseMessageToString(InboundMessage inboundMessage) {
        return objectMapper.writeValueAsString(inboundMessage);
    }
}
