package uk.nhs.adaptors.pss.translator;

import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;
import static uk.nhs.adaptors.connector.model.MigrationStatus.CONTINUE_REQUEST_ACCEPTED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_ACCEPTED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.pss.util.JsonPathIgnoreGeneratorUtil.generateJsonPathIgnores;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.storage.StorageManagerService;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith({SpringExtension.class})
@DirtiesContext
@AutoConfigureMockMvc
public class LargeMessagingIT {
    private static final int NHS_NUMBER_MIN_MAX_LENGTH = 10;
    private static final boolean OVERWRITE_EXPECTED_JSON = false;

    private static final List<String> STATIC_IGNORED_JSON_PATHS = List.of(
        "id",
        "entry[0].resource.id",
        "entry[0].resource.identifier[0].value",
        "entry[*].resource.id",
        "entry[*].resource.subject.reference",
        "entry[*].resource.patient.reference",
        "entry[*].resource.performer[0].reference",
        "entry[*].resource.content[0].attachment.title",
        "entry[*].resource.content[0].attachment.url",
        "entry[*].resource.description"
    );
    private static final String LOSING_ODS_CODE = "D5445";
    private static final String WINNING_ODS_CODE = "ABC";
    private static final String NHS_NUMBER_PLACEHOLDER = "{{nhsNumber}}";
    private static final String CONVERSATION_ID_PLACEHOLDER = "{{conversationId}}";

    @Autowired
    private PatientMigrationRequestDao patientMigrationRequestDao;

    @Autowired
    private MigrationStatusLogService migrationStatusLogService;

    @Autowired
    private PatientAttachmentLogService patientAttachmentLogService;

    @Qualifier("jmsTemplateMhsQueue")
    @Autowired
    private JmsTemplate mhsJmsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FhirParser fhirParserService;

    private String patientNhsNumber;
    private String conversationId;


    @BeforeEach
    public void setUp() {
        patientNhsNumber = generatePatientNhsNumber();
        conversationId = generateConversationId();
        startPatientMigrationJourney();
    }

    // Test case 1: UK06 with cid attachment
    @Test
    public void handleUk06WithCidAttachment() throws JSONException {
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_1/uk06.json");

        await().until(this::isEhrExtractTranslated);

        verifyBundle("/json/LargeMessage/expectedBundleScenario1.json");
    }

    // Test case 2: UK06 with compressed cid attachment
    @Test
    public void handleUk06WithCompressedCidAttachmement() throws JSONException {
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_2/uk06.json");

        await().until(this::isEhrExtractTranslated);

        verifyBundle("/json/LargeMessage/expectedBundleScenario2.json");
    }

    // Test case 3: UK06 with 1 mid attachment
    @Test
    public void handleUk06WithOneMidAttachment() throws JSONException {
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_3/uk06.json");

        await().until(this::hasContinueMessageBeenRecieved);

        sendInboundMessageToQueue("/json/LargeMessage/Scenario_3/copc.json");

        await().until(this::isEhrExtractTranslated);

        verifyBundle("/json/LargeMessage/expectedBundleScenario3.json");
    }

    // Test case 4: UK06 with fragment mid attachments
    @Test
    public void handleUk06WithFragmentedMids() throws JSONException {
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_4/uk06.json");

        await().until(this::hasContinueMessageBeenRecieved);

        sendInboundMessageToQueue("/json/LargeMessage/Scenario_4/copc_index.json");
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_4/copc0.json");
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_4/copc1.json");

        await().until(this::isEhrExtractTranslated);

        verifyBundle("/json/LargeMessage/expectedBundleScenario4.json");
    }

    // Test case 5: UK06 with skeleton
    @Test
    public void handleUk06WithSkeleton() throws JSONException {
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_5/uk06.json");

        await().until(this::hasContinueMessageBeenRecieved);

        sendInboundMessageToQueue("/json/LargeMessage/Scenario_5/copc.json");

        await().until(this::isEhrExtractTranslated);

        verifyBundle("/json/LargeMessage/expectedBundleScenario5.json");
    }

    // Test case 6: UK06 with mid attachment with cid mid combo - java.lang.IllegalArgumentException: Input byte array has incorrect ending byte at 3448921 - Attachment Handler Service - line 48
    // Think its merging all 3 even though theres only 2 that need to be merged
    // Doesnt bundle because it gets all 3 but the first attachment is an index so it doesnt get set to "uploaded" so when it does the check only 2 of 3 are true and doesnt pass into bundle method
    @Test
    public void handleUk06WithMidAttachmentsWithCidAndMidCombo() throws JSONException {
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_6/uk06.json");

        await().until(this::hasContinueMessageBeenRecieved);

        sendInboundMessageToQueue("/json/LargeMessage/Scenario_6/copc_index.json");
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_6/copc0.json");
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_6/copc1.json");

        await().until(this::isEhrExtractTranslated);

        verifyBundle("/json/LargeMessage/expectedBundleScenario6.json");
    }

    // Test case 7: UK06 with skeleton with fragments
    // fails in MhqQueueHandler parse ebxml doc, The markup in the document following the root element must be well-formed.
    // Both copc0/1 work just index file that fails
    @Test
    public void handleUk06WithSkeletonFragments() throws JSONException {
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_7/uk06.json");

        await().until(this::hasContinueMessageBeenRecieved);

        sendInboundMessageToQueue("/json/LargeMessage/Scenario_7/copc_index.json");
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_7/copc0.json");
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_7/copc1.json");

        await().until(this::isEhrExtractTranslated);

        verifyBundle("/json/LargeMessage/expectedBundleScenario7.json");
    }

    // Test case 8: UK06 with with fragmented mid/cid combo
    @Test
    public void handleUk06WithFragmentedMidCidCombo() throws JSONException {
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_8/uk06.json");

        await().until(this::hasContinueMessageBeenRecieved);

        sendInboundMessageToQueue("/json/LargeMessage/Scenario_8/copc_index.json");
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_8/copc0.json");
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_8/copc1.json");

        await().until(this::isEhrExtractTranslated);

        verifyBundle("/json/LargeMessage/expectedBundleScenario8.json");
    }

    // Test case 9: UK06 with 1 mid attachment
    @Test
    public void handleUk06WithOneMidAttachmentCheck() throws JSONException {
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_9/uk06.json");

        await().until(this::hasContinueMessageBeenRecieved);

        sendInboundMessageToQueue("/json/LargeMessage/Scenario_9/copc_index.json");
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_9/copc0.json");
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_9/copc1.json");

        await().until(this::isEhrExtractTranslated);

        verifyBundle("/json/LargeMessage/expectedBundleScenario9.json");
    }

    private void sendInboundMessageToQueue(String json) {
        var jsonMessage = readResourceAsString(json)
            .replace(NHS_NUMBER_PLACEHOLDER, patientNhsNumber)
            .replace(CONVERSATION_ID_PLACEHOLDER, conversationId);
        mhsJmsTemplate.send(session -> session.createTextMessage(jsonMessage));
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
            .collect(Collectors.toList());

        assertBundleContent(patientMigrationRequest.getBundleResource(), expectedBundle, combinedList);
    }

    private void assertBundleContent(String actual, String expected, List<String> ignoredPaths) throws JSONException {
        // when comparing json objects, this will ignore various json paths that contain random values like ids or timestamps
        var customizations = ignoredPaths.stream()
            .map(jsonPath -> new Customization(jsonPath, (o1, o2) -> true))
            .toArray(Customization[]::new);

        JSONAssert.assertEquals(expected, actual,
            new CustomComparator(JSONCompareMode.STRICT, customizations));
    }

    private boolean isAttachmentInserted(String id) {
        var attachmentLog = patientAttachmentLogService.findAttachmentLog(id, conversationId);
        return attachmentLog != null;
    }
    private boolean hasContinueMessageBeenRecieved() {
        var migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);
        return CONTINUE_REQUEST_ACCEPTED.equals(migrationStatusLog.getMigrationStatus());
    }
    private boolean isEhrExtractTranslated() {
        var migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);
        return EHR_EXTRACT_TRANSLATED.equals(migrationStatusLog.getMigrationStatus());
    }

    private void startPatientMigrationJourney() {
        patientMigrationRequestDao.addNewRequest(patientNhsNumber, conversationId, LOSING_ODS_CODE, WINNING_ODS_CODE);
        migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_REQUEST_ACCEPTED, conversationId);
    }

    @SneakyThrows
    private void overwriteExpectJson(String newExpected) {
        try (PrintWriter printWriter = new PrintWriter("src/integrationTest/resources/json/expectedBundle.json", StandardCharsets.UTF_8)) {
            printWriter.print(newExpected);
        }
        fail("Re-run the tests with OVERWRITE_EXPECTED_JSON=false");
    }

    private String generatePatientNhsNumber() {
        return RandomStringUtils.randomNumeric(NHS_NUMBER_MIN_MAX_LENGTH, NHS_NUMBER_MIN_MAX_LENGTH);
    }

    private String generateConversationId() {
        return UUID.randomUUID().toString();
    }
    @SneakyThrows
    private String parseMessageToString(InboundMessage inboundMessage) {
        return objectMapper.writeValueAsString(inboundMessage);
    }
}
