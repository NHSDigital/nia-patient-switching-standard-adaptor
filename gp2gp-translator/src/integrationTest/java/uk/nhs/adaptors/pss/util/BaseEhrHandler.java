package uk.nhs.adaptors.pss.util;

import static org.assertj.core.api.Assertions.fail;

import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;
import static uk.nhs.adaptors.common.enums.MigrationStatus.COPC_FAILED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_REQUEST_ACCEPTED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.MIGRATION_COMPLETED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.COPC_MESSAGE_PROCESSING;
import static uk.nhs.adaptors.common.enums.MigrationStatus.ERROR_LRG_MSG_GENERAL_FAILURE;
import static uk.nhs.adaptors.pss.util.JsonPathIgnoreGeneratorUtil.generateJsonPathIgnores;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;

@Getter
public abstract class BaseEhrHandler {
    private static final boolean OVERWRITE_EXPECTED_JSON = false;

    private List<String> ignoredJsonPaths;
    private static final int NHS_NUMBER_MIN_MAX_LENGTH = 10;

    @Getter
    protected static final String NHS_NUMBER_PLACEHOLDER = "{{nhsNumber}}";

    @Getter
    protected static final String CONVERSATION_ID_PLACEHOLDER = "{{conversationId}}";

    @Getter @Setter
    private String losingODSCode = "D5445";
    @Getter @Setter
    private String winingODSCode = "ABC";
    @Getter @Setter
    private String patientNhsNumber;
    @Getter @Setter
    private String conversationId;

    @Autowired
    private PatientMigrationRequestDao patientMigrationRequestDao;

    @Autowired
    private MigrationStatusLogService migrationStatusLogService;

    @Autowired
    private FhirParser fhirParserService;

    @Qualifier("jmsTemplateMhsQueue")
    @Autowired
    private JmsTemplate mhsJmsTemplate;

    @Qualifier("jmsTemplatePssQueue")
    @Autowired
    private JmsTemplate pssJmsTemplate;

    @BeforeEach
    public void setUp() {
        patientNhsNumber = generatePatientNhsNumber();
        conversationId = generateConversationId();
        startPatientMigrationJourney();
    }

    protected void startPatientMigrationJourney() {
        patientMigrationRequestDao.addNewRequest(patientNhsNumber, conversationId, losingODSCode, winingODSCode);
        migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_REQUEST_ACCEPTED, conversationId, null);
    }

    protected boolean isEhrExtractTranslated() {
        var migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);
        return EHR_EXTRACT_TRANSLATED.equals(migrationStatusLog.getMigrationStatus());
    }

    protected boolean isEhrMigrationCompleted() {
        var migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);
        return MIGRATION_COMPLETED.equals(migrationStatusLog.getMigrationStatus());
    }
    protected boolean isCOPCMessageProcessing() {
        var migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);
        return COPC_MESSAGE_PROCESSING.equals(migrationStatusLog.getMigrationStatus());
    }
    protected boolean isLargeGeneralMessageFailure() {
        var migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);
        return ERROR_LRG_MSG_GENERAL_FAILURE.equals(migrationStatusLog.getMigrationStatus());
    }

    protected boolean isCopcFailed() {
        var migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);
        return COPC_FAILED.equals(migrationStatusLog.getMigrationStatus());
    }

    protected void verifyBundle(String path) throws JSONException {
        var patientMigrationRequest = patientMigrationRequestDao.getMigrationRequest(conversationId);
        var expectedBundle = readResourceAsString(path).replace(NHS_NUMBER_PLACEHOLDER, patientNhsNumber);

        if (OVERWRITE_EXPECTED_JSON) {
            overwriteExpectJson(patientMigrationRequest.getBundleResource());
        }

        var bundle = fhirParserService.parseResource(patientMigrationRequest.getBundleResource(), Bundle.class);
        var combinedList = Stream.of(generateJsonPathIgnores(bundle), ignoredJsonPaths)
            .flatMap(List::stream)
            .toList();

        assertBundleContent(patientMigrationRequest.getBundleResource(), expectedBundle, combinedList);
    }

    @SneakyThrows
    private void overwriteExpectJson(String newExpected) {
        try (PrintWriter printWriter = new PrintWriter("src/integrationTest/resources/json/expectedBundle.json", StandardCharsets.UTF_8)) {
            printWriter.print(newExpected);
        }
        fail("Re-run the tests with OVERWRITE_EXPECTED_JSON=false");
    }

    private void assertBundleContent(String actual, String expected, List<String> ignoredPaths) throws JSONException {
        // when comparing json objects, this will ignore various json paths that contain random values like ids or timestamps
        var customizations = ignoredPaths.stream()
            .map(jsonPath -> new Customization(jsonPath, (o1, o2) -> true))
            .toArray(Customization[]::new);

        JSONAssert.assertEquals(expected, actual,
            new CustomComparator(JSONCompareMode.STRICT, customizations));
    }

    protected String generatePatientNhsNumber() {
        return RandomStringUtils.randomNumeric(NHS_NUMBER_MIN_MAX_LENGTH, NHS_NUMBER_MIN_MAX_LENGTH);
    }

    protected void setIgnoredJsonPaths(List<String> ignoredJsonPaths) {
        this.ignoredJsonPaths = ignoredJsonPaths;
    }

    protected String generateConversationId() {
        return UUID.randomUUID().toString();
    }
}
