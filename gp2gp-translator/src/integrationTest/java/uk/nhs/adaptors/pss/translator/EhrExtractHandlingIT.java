package uk.nhs.adaptors.pss.translator;

import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.waitAtMost;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_REQUEST_ACCEPTED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.MIGRATION_COMPLETED;
import static uk.nhs.adaptors.pss.util.JsonPathIgnoreGeneratorUtil.generateJsonPathIgnores;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith({SpringExtension.class})
@DirtiesContext
@AutoConfigureMockMvc
public class EhrExtractHandlingIT {

    private static final boolean OVERWRITE_EXPECTED_JSON = false;
    private static final int NHS_NUMBER_MIN_MAX_LENGTH = 10;
    private static final String EBXML_PART_PATH = "/xml/RCMR_IN030000UK06/ebxml_part.xml";
    private static final String NHS_NUMBER_PLACEHOLDER = "{{nhsNumber}}";
    private static final String CONVERSATION_ID_PLACEHOLDER = "{{conversationId}}";
    private static final String LOSING_ODS_CODE = "D5445";
    private static final String WINNING_ODS_CODE = "ABC";
    //these are programming language special characters, not to be confused with line endings
    private static final String SPECIAL_CHARS = "\\\\n|\\\\t|\\\\b|\\\\r";

    private static final List<String> STATIC_IGNORED_JSON_PATHS = List.of(
        "id",
        "entry[0].resource.id",
        "entry[0].resource.identifier[0].value",
        "entry[1].resource.id",
        "entry[*].resource.subject.reference",
        "entry[*].resource.patient.reference",
        "entry[21].resource.location[0].location.reference",
        "entry[22].resource.location[0].location.reference",
        "entry[23].resource.location[0].location.reference",
        "entry[24].resource.location[0].location.reference",
        "entry[25].resource.location[0].location.reference",
        "entry[26].resource.location[0].location.reference",
        "entry[29].resource.location[0].location.reference",
        "entry[31].resource.location[0].location.reference",
        "entry[59].resource.id",
        "entry[59].resource.identifier[0].value",
        "entry[60].resource.id",
        "entry[60].resource.identifier[0].value"
    );

    @Autowired
    private PatientMigrationRequestDao patientMigrationRequestDao;

    @Autowired
    private MigrationStatusLogService migrationStatusLogService;

    @Qualifier("jmsTemplateMhsQueue")
    @Autowired
    private JmsTemplate mhsJmsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FhirParser fhirParserService;

    private String patientNhsNumber;
    private String conversationId;
    static final int WAITING_TIME = 10;

    @BeforeEach
    public void setUp() {
        patientNhsNumber = generatePatientNhsNumber();
        conversationId = generateConversationId().toUpperCase(Locale.ROOT);
        startPatientMigrationJourney();
    }

    @Test
    public void handleEhrExtractFromQueue() throws JSONException {
        // process starts with consuming a message from MHS queue
        sendInboundMessageToQueue("/xml/RCMR_IN030000UK06/payload_part.xml", EBXML_PART_PATH);

        // wait until EHR extract is translated to bundle resource and saved to the DB
        waitAtMost(Duration.ofSeconds(WAITING_TIME)).until(this::isEhrMigrationCompleted);

        // verify generated bundle resource
        verifyBundle("/json/expectedBundle.json");
    }

    @Test
    public void handleEhrExtractWithConfidentialityCodeFromQueue() throws JSONException {
        final String ebxmlPartPath = "/xml/RCMR_IN030000UK07/ebxml_part.xml";
        // process starts with consuming a message from MHS queue
        sendInboundMessageToQueue("/xml/RCMR_IN030000UK07/payload_part_with_confidentiality_code.xml", ebxmlPartPath);

        // wait until EHR extract is translated to bundle resource and saved to the DB
        waitAtMost(Duration.ofSeconds(WAITING_TIME)).until(this::isEhrMigrationCompleted);

        // verify generated bundle resource
        verifyBundle("/json/expectedBundle.json");
    }

    private void startPatientMigrationJourney() {
        patientMigrationRequestDao.addNewRequest(patientNhsNumber, conversationId, LOSING_ODS_CODE, WINNING_ODS_CODE);
        migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_REQUEST_ACCEPTED, conversationId, null);
    }

    private String generatePatientNhsNumber() {
        return RandomStringUtils.randomNumeric(NHS_NUMBER_MIN_MAX_LENGTH, NHS_NUMBER_MIN_MAX_LENGTH);
    }

    private String generateConversationId() {
        return UUID.randomUUID().toString();
    }

    private void sendInboundMessageToQueue(String payloadPartPath, String ebxmlPartPath) {
        var inboundMessage = createInboundMessage(payloadPartPath, ebxmlPartPath);
        mhsJmsTemplate.send(session -> session.createTextMessage(parseMessageToString(inboundMessage)));
    }

    private InboundMessage createInboundMessage(String payloadPartPath, String ebxmlPartPath) {
        var inboundMessage = new InboundMessage();
        var payload = readResourceAsString(payloadPartPath).replace(NHS_NUMBER_PLACEHOLDER, patientNhsNumber);
        var ebXml = readResourceAsString(ebxmlPartPath).replace(CONVERSATION_ID_PLACEHOLDER, conversationId);
        inboundMessage.setPayload(payload);
        inboundMessage.setEbXML(ebXml);
        return inboundMessage;
    }

    private boolean isEhrMigrationCompleted() {
        var migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);
        return MIGRATION_COMPLETED.equals(migrationStatusLog.getMigrationStatus());
    }

    private void verifyBundle(String path) throws JSONException {
        var patientMigrationRequest = patientMigrationRequestDao.getMigrationRequest(conversationId);
        var expectedBundle = readResourceAsString(path).replace(NHS_NUMBER_PLACEHOLDER, patientNhsNumber);

        if (OVERWRITE_EXPECTED_JSON) {
            overwriteExpectJson(patientMigrationRequest.getBundleResource());
        }

        var bundle = fhirParserService.parseResource(patientMigrationRequest.getBundleResource(), Bundle.class);
        var combinedList = Stream.of(generateJsonPathIgnores(bundle), STATIC_IGNORED_JSON_PATHS)
            .flatMap(List::stream)
            .toList();

        assertBundleContent(
            patientMigrationRequest.getBundleResource().replaceAll(SPECIAL_CHARS, ""),
            expectedBundle.replaceAll(SPECIAL_CHARS, ""),
            combinedList
        );
    }

    private void assertBundleContent(String actual, String expected, List<String> ignoredPaths) throws JSONException {
        // when comparing json objects, this will ignore various json paths that contain random values like ids or timestamps
        var customizations = ignoredPaths.stream()
            .map(jsonPath -> new Customization(jsonPath, (o1, o2) -> true))
            .toArray(Customization[]::new);

        JSONAssert.assertEquals(expected, actual,
            new CustomComparator(JSONCompareMode.STRICT, customizations));
    }

    @SneakyThrows
    private void overwriteExpectJson(String newExpected) {
        try (PrintWriter printWriter = new PrintWriter("src/integrationTest/resources/json/expectedBundle.json", StandardCharsets.UTF_8)) {
            printWriter.print(newExpected);
        }
        fail("Re-run the tests with OVERWRITE_EXPECTED_JSON=false");
    }

    @SneakyThrows
    private String parseMessageToString(InboundMessage inboundMessage) {
        return objectMapper.writeValueAsString(inboundMessage);
    }
}
