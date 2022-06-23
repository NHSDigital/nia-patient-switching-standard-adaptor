package uk.nhs.adaptors.pss.translator;

import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;

import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_ACCEPTED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.MIGRATION_COMPLETED;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;
import static uk.nhs.adaptors.pss.util.JsonPathIgnoreGeneratorUtil.generateJsonPathIgnores;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.RandomStringUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.v3.RCMRIN030000UK06Message;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class E2EMappingIT {

    private static final boolean OVERWRITE_EXPECTED_JSON = false;
    private static final int NHS_NUMBER_MIN_MAX_LENGTH = 10;
    private static final String EBXML_PART_PATH = "/xml/RCMR_IN030000UK06/ebxml_part.xml";
    private static final String CONVERSATION_ID_PLACEHOLDER = "{{conversationId}}";
    private static final String LOSING_ODS_CODE = "B83002";
    private static final String WINNING_ODS_CODE = "C81007";
    //these are programming language special characters, not to be confused with line endings
    private static final String SPECIAL_CHARS = "\\\\n|\\\\t|\\\\b|\\\\r";

    private String nhsNumberToBeReplaced;
    private String patientNhsNumber;
    private String conversationId;

    @Autowired
    private ObjectMapper objectMapper;

    @Qualifier("jmsTemplateMhsQueue")
    @Autowired
    private JmsTemplate mhsJmsTemplate;

    @Autowired
    private MigrationStatusLogService migrationStatusLogService;

    @Autowired
    private PatientMigrationRequestDao patientMigrationRequestDao;

    @Autowired
    private FhirParser fhirParserService;

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

    private String generatePatientNhsNumber() {
        return RandomStringUtils.randomNumeric(NHS_NUMBER_MIN_MAX_LENGTH, NHS_NUMBER_MIN_MAX_LENGTH);
    }

    private String generateConversationId() {
        return UUID.randomUUID().toString();
    }


    private static final List<String> STATIC_IGNORED_JSON_PATHS = List.of(
            "id",
            "entry[0].resource.id",
            "entry[0].resource.identifier[0].value",
            "entry[1].resource.id",
            "entry[*].resource.subject.reference",
            "entry[*].resource.patient.reference"
    );

    @Test
    public void handlePWTP2EhrExtractFromQueue() throws JSONException, JAXBException {
        String inputFileName = "PWTP2";
        executeTest(inputFileName);
    }

    @Test
    public void handlePWTP3EhrExtractFromQueue() throws JSONException, JAXBException {
        String inputFileName = "PWTP3";
        executeTest(inputFileName);
    }

    @Test
    public void handlePWTP4EhrExtractFromQueue() throws JSONException, JAXBException {
        String inputFileName = "PWTP4";
        executeTest(inputFileName);
    }

    @Test
    public void handlePWTP5EhrExtractFromQueue() throws JSONException, JAXBException {
        String inputFileName = "PWTP5";
        executeTest(inputFileName);
    }

    @Test
    public void handlePWTP6EhrExtractFromQueue() throws JSONException, JAXBException {
        String inputFileName = "PWTP6";
        executeTest(inputFileName);
    }

    @Test
    public void handlePWTP7visEhrExtractFromQueue() throws JSONException, JAXBException {
        String inputFileName = "PWTP7_vis";
        executeTest(inputFileName);
    }

    @Test
    public void handlePWTP9EhrExtractFromQueue() throws JSONException, JAXBException {
        String inputFileName = "PWTP9";
        executeTest(inputFileName);
    }

    @Test
    public void handlePWTP10EhrExtractFromQueue() throws JSONException, JAXBException {
        String inputFileName = "PWTP10";
        executeTest(inputFileName);
    }

    @Test
    public void handlePWTP11EhrExtractFromQueue() throws JSONException, JAXBException {
        String inputFileName = "PWTP11";
        executeTest(inputFileName);
    }

    private void executeTest(String inputFileName) throws JAXBException, JSONException {
        // process starts with consuming a message from MHS queue
        sendInboundMessageToQueue("/e2e-mapping/input-xml/" + inputFileName + ".xml");

        // wait until EHR extract is translated to bundle resource and saved to the DB
        await().until(this::isEhrExtractTranslated);

        // verify generated bundle resource
        verifyBundle("/e2e-mapping/output-json/" + inputFileName + "-output.json");
    }

    private void sendInboundMessageToQueue(String payloadPartPath) throws JAXBException {
        var inboundMessage = createInboundMessage(payloadPartPath);
        mhsJmsTemplate.send(session -> session.createTextMessage(parseMessageToString(inboundMessage)));
    }

    private InboundMessage createInboundMessage(String payloadPartPath) throws JAXBException {

        var inboundMessage = new InboundMessage();

        var payload = readResourceAsString(payloadPartPath);
        var ebXml = readResourceAsString(EBXML_PART_PATH).replace(CONVERSATION_ID_PLACEHOLDER, conversationId);

        RCMRIN030000UK06Message payloadObject = unmarshallString(payload, RCMRIN030000UK06Message.class);

        nhsNumberToBeReplaced = getNhsNumberToBeReplaced(payloadObject);

        inboundMessage.setPayload(payload.replaceAll(nhsNumberToBeReplaced, patientNhsNumber));
        inboundMessage.setEbXML(ebXml);
        return inboundMessage;
    }

    private String getNhsNumberToBeReplaced(RCMRIN030000UK06Message payloadObject) {
        return payloadObject
                .getControlActEvent()
                .getSubject()
                .getEhrExtract()
                .getRecordTarget()
                .getPatient()
                .getId()
                .getRoot();
    }

    @SneakyThrows
    private String parseMessageToString(InboundMessage inboundMessage) {
        return objectMapper.writeValueAsString(inboundMessage);
    }

    private boolean isEhrExtractTranslated() {
        var migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);
        return MIGRATION_COMPLETED.equals(migrationStatusLog.getMigrationStatus());
    }

    private void verifyBundle(String path) throws JSONException {
        var patientMigrationRequest = patientMigrationRequestDao.getMigrationRequest(conversationId);
        var expectedBundle = readResourceAsString(path).replaceAll(nhsNumberToBeReplaced, patientNhsNumber);
        var odsCodeToBeReplaced = getOdsToBeReplaced(expectedBundle);

        if (OVERWRITE_EXPECTED_JSON) {
            overwriteExpectJson(patientMigrationRequest.getBundleResource());
        }

        var bundle = fhirParserService.parseResource(patientMigrationRequest.getBundleResource(), Bundle.class);
        var combinedList = Stream.of(generateJsonPathIgnores(bundle), STATIC_IGNORED_JSON_PATHS)
                .flatMap(List::stream)
                .toList();

        expectedBundle = expectedBundle.replaceAll(odsCodeToBeReplaced, LOSING_ODS_CODE);

        assertBundleContent(
                patientMigrationRequest.getBundleResource().replaceAll(SPECIAL_CHARS, ""),
                expectedBundle.replaceAll(SPECIAL_CHARS, ""),
                combinedList
        );
    }

    private String getOdsToBeReplaced(String expectedBundle) {
        var startIndex = expectedBundle.toLowerCase().indexOf("https://PSSAdaptor/".toLowerCase()) + "https://PSSAdaptor/".length();
        var endIndex = expectedBundle.toLowerCase().indexOf("\"", startIndex);

        return expectedBundle.substring(startIndex, endIndex);
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
}