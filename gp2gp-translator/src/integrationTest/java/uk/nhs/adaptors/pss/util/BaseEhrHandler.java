package uk.nhs.adaptors.pss.util;

import static org.assertj.core.api.Assertions.fail;

import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_ACCEPTED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.pss.util.JsonPathIgnoreGeneratorUtil.generateJsonPathIgnores;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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

import lombok.SneakyThrows;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;

public abstract class BaseEhrHandler {
    private static final boolean OVERWRITE_EXPECTED_JSON = false;
    protected static List<String> staticIgnoredJsonPaths;
    private static final int NHS_NUMBER_MIN_MAX_LENGTH = 10;
    private static final String LOSING_ODS_CODE = "D5445";
    private static final String WINNING_ODS_CODE = "ABC";

    protected static final String NHS_NUMBER_PLACEHOLDER = "{{nhsNumber}}";
    protected static final String CONVERSATION_ID_PLACEHOLDER = "{{conversationId}}";
    protected String patientNhsNumber;
    protected String conversationId;

    @Autowired
    private PatientMigrationRequestDao patientMigrationRequestDao;

    @Autowired
    protected MigrationStatusLogService migrationStatusLogService;

    @Autowired
    private FhirParser fhirParserService;

    @Qualifier("jmsTemplateMhsQueue")
    @Autowired
    protected JmsTemplate mhsJmsTemplate;

    @BeforeEach
    public void setUp() {
        patientNhsNumber = generatePatientNhsNumber();
        conversationId = generateConversationId();
        startPatientMigrationJourney();
    }

    private void startPatientMigrationJourney() {
        patientMigrationRequestDao.addNewRequest(patientNhsNumber, conversationId, LOSING_ODS_CODE, WINNING_ODS_CODE);
        migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_REQUEST_ACCEPTED, conversationId);
    }

    protected boolean isEhrExtractTranslated() {
        var migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);
        return EHR_EXTRACT_TRANSLATED.equals(migrationStatusLog.getMigrationStatus());
    }

    protected void verifyBundle(String path) throws JSONException {
        var patientMigrationRequest = patientMigrationRequestDao.getMigrationRequest(conversationId);
        var expectedBundle = readResourceAsString(path).replace(NHS_NUMBER_PLACEHOLDER, patientNhsNumber);

        if (OVERWRITE_EXPECTED_JSON) {
            overwriteExpectJson(patientMigrationRequest.getBundleResource());
        }

        var bundle = fhirParserService.parseResource(patientMigrationRequest.getBundleResource(), Bundle.class);
        var combinedList = Stream.of(generateJsonPathIgnores(bundle), staticIgnoredJsonPaths)
            .flatMap(List::stream)
            .collect(Collectors.toList());

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

    private String generatePatientNhsNumber() {
        return RandomStringUtils.randomNumeric(NHS_NUMBER_MIN_MAX_LENGTH, NHS_NUMBER_MIN_MAX_LENGTH);
    }

    private String generateConversationId() {
        return UUID.randomUUID().toString();
    }
}
