package uk.nhs.adaptors.pss.translator;

import static org.awaitility.Awaitility.await;

import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;
import static uk.nhs.adaptors.pss.util.JsonPathIgnoreGeneratorUtil.generateJsonPathIgnores;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.xml.bind.JAXBException;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.nhs.adaptors.pss.util.BaseEhrHandler;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class E2EMappingIT extends BaseEhrHandler {

    private static final String PSS_ADAPTOR_URL = "https://PSSAdaptor/";
    private static final String EBXML_PART_PATH = "/xml/RCMR_IN030000UK06/ebxml_part.xml";
    //these are programming language special characters, not to be confused with line endings
    private static final String SPECIAL_CHARS = "\\\\n|\\\\t|\\\\b|\\\\r";

    private String nhsNumberToBeReplaced;

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

    @Override
    @BeforeEach
    public void setUp() {
        setPatientNhsNumber(generatePatientNhsNumber());
        setConversationId(generateConversationId().toUpperCase(Locale.ROOT));
        setLosingODSCode("B83002");
        setWiningODSCode("C81007");
        startPatientMigrationJourney();
    }

    private static List<String> staticIgnoredJsonPaths = List.of(
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


        List<String> ignoredFields = List.of(
                "entry[4].resource.location[0].location.reference",
                "entry[5].resource.location[0].location.reference",
                "entry[6].resource.location[0].location.reference",
                "entry[7].resource.location[0].location.reference",
                "entry[8].resource.location[0].location.reference",
                "entry[9].resource.location[0].location.reference",
                "entry[10].resource.location[0].location.reference",
                "entry[11].resource.location[0].location.reference",
                "entry[12].resource.location[0].location.reference",
                "entry[49].resource.id",
                "entry[49].resource.identifier[0].value",
                "entry[50].resource.id",
                "entry[50].resource.identifier[0].value"

        );

        executeTest(inputFileName, ignoredFields);
    }

    @Test
    public void handlePWTP3EhrExtractFromQueue() throws JSONException, JAXBException {
        String inputFileName = "PWTP3";

        List<String> ignoredFields = List.of(
                "entry[3].resource.location[0].location.reference",
                "entry[11].resource.id",
                "entry[11].resource.identifier[0].value"
        );

        executeTest(inputFileName, ignoredFields);
    }

    @Test
    public void handlePWTP4EhrExtractFromQueue() throws JSONException, JAXBException {
        String inputFileName = "PWTP4";

        List<String> ignoredFields = List.of(
                "entry[6].resource.location[0].location.reference",
                "entry[7].resource.location[0].location.reference",
                "entry[8].resource.location[0].location.reference",
                "entry[9].resource.location[0].location.reference",
                "entry[10].resource.location[0].location.reference",
                "entry[11].resource.location[0].location.reference",
                "entry[44].resource.id",
                "entry[44].resource.identifier[0].value"
        );

        executeTest(inputFileName, ignoredFields);
    }

    @Test
    public void handlePWTP5EhrExtractFromQueue() throws JSONException, JAXBException {
        String inputFileName = "PWTP5";

        List<String> ignoredFields = List.of(
                "entry[2].resource.location[0].location.reference",
                "entry[3].resource.location[0].location.reference",
                "entry[4].resource.location[0].location.reference",
                "entry[5].resource.location[0].location.reference",
                "entry[6].resource.location[0].location.reference",
                "entry[7].resource.location[0].location.reference",
                "entry[8].resource.location[0].location.reference",
                "entry[9].resource.location[0].location.reference",
                "entry[10].resource.location[0].location.reference",
                "entry[45].resource.id",
                "entry[45].resource.identifier[0].value"

        );

        executeTest(inputFileName, ignoredFields);
    }

    @Test
    public void handlePWTP6EhrExtractFromQueue() throws JSONException, JAXBException {
        String inputFileName = "PWTP6";

        List<String> ignoredFields = List.of(
                "entry[3].resource.location[0].location.reference",
                "entry[4].resource.location[0].location.reference",
                "entry[5].resource.location[0].location.reference",
                "entry[6].resource.location[0].location.reference",
                "entry[7].resource.location[0].location.reference",
                "entry[8].resource.location[0].location.reference",
                "entry[35].resource.id",
                "entry[35].resource.identifier[0].value",
                "entry[36].resource.id",
                "entry[36].resource.identifier[0].value"
        );

        executeTest(inputFileName, ignoredFields);
    }

    @Test
    public void handlePWTP7visEhrExtractFromQueue() throws JSONException, JAXBException {
        String inputFileName = "PWTP7_vis";

        List<String> ignoredFields = List.of(
                "entry[4].resource.location[0].location.reference",
                "entry[5].resource.location[0].location.reference",
                "entry[6].resource.location[0].location.reference",
                "entry[7].resource.location[0].location.reference",
                "entry[8].resource.location[0].location.reference",
                "entry[9].resource.location[0].location.reference",
                "entry[10].resource.location[0].location.reference",
                "entry[11].resource.location[0].location.reference",
                "entry[12].resource.location[0].location.reference",
                "entry[13].resource.location[0].location.reference",
                "entry[14].resource.location[0].location.reference",
                "entry[48].resource.id",
                "entry[48].resource.identifier[0].value"
        );

        executeTest(inputFileName, ignoredFields);
    }

    @Test
    public void handlePWTP9EhrExtractFromQueue() throws JSONException, JAXBException {
        String inputFileName = "PWTP9";

        List<String> ignoredFields = List.of(
                "entry[23].resource.location[0].location.reference",
                "entry[24].resource.location[0].location.reference",
                "entry[89].resource.id",
                "entry[89].resource.identifier[0].value"
        );

        executeTest(inputFileName, ignoredFields);
    }

    @Test
    public void handlePWTP10EhrExtractFromQueue() throws JSONException, JAXBException {
        String inputFileName = "PWTP10";

        List<String> ignoredFields = List.of(
                "entry[3].resource.location[0].location.reference",
                "entry[4].resource.location[0].location.reference",
                "entry[5].resource.location[0].location.reference",
                "entry[6].resource.location[0].location.reference",
                "entry[7].resource.location[0].location.reference",
                "entry[8].resource.location[0].location.reference",
                "entry[9].resource.location[0].location.reference",
                "entry[10].resource.location[0].location.reference",
                "entry[48].resource.id",
                "entry[48].resource.identifier[0].value",
                "entry[49].resource.id",
                "entry[49].resource.identifier[0].value"
        );

        executeTest(inputFileName, ignoredFields);
    }

    @Test
    public void handlePWTP11EhrExtractFromQueue() throws JSONException, JAXBException {
        String inputFileName = "PWTP11";

        List<String> ignoredFields = List.of(
                "entry[2].resource.location[0].location.reference",
                "entry[3].resource.location[0].location.reference",
                "entry[4].resource.location[0].location.reference",
                "entry[5].resource.location[0].location.reference",
                "entry[6].resource.location[0].location.reference",
                "entry[7].resource.location[0].location.reference",
                "entry[8].resource.location[0].location.reference",
                "entry[48].resource.id",
                "entry[48].resource.identifier[0].value"

        );

        executeTest(inputFileName, ignoredFields);
    }

    private void executeTest(String inputFileName, List<String> ignoredFields) throws JAXBException, JSONException {
        // process starts with consuming a message from MHS queue
        sendInboundMessageToQueue("/e2e-mapping/input-xml/" + inputFileName + ".xml");
        // wait until EHR extract is translated to bundle resource and saved to the DB
        await().until(this::isEhrMigrationCompleted);
        // verify generated bundle resource
        verifyBundle("/e2e-mapping/output-json/" + inputFileName + "-output.json", ignoredFields);
    }

    private void sendInboundMessageToQueue(String payloadPartPath) throws JAXBException {
        var inboundMessage = createInboundMessage(payloadPartPath);
        mhsJmsTemplate.send(session -> session.createTextMessage(parseMessageToString(inboundMessage)));
    }

    private InboundMessage createInboundMessage(String payloadPartPath) throws JAXBException {

        var inboundMessage = new InboundMessage();

        var payload = readResourceAsString(payloadPartPath);
        var ebXml = readResourceAsString(EBXML_PART_PATH).replace(CONVERSATION_ID_PLACEHOLDER, getConversationId());

        RCMRIN030000UK06Message payloadObject = unmarshallString(payload, RCMRIN030000UK06Message.class);

        nhsNumberToBeReplaced = getNhsNumberToBeReplaced(payloadObject);

        inboundMessage.setPayload(payload.replaceAll(nhsNumberToBeReplaced, getPatientNhsNumber()));
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


    protected void verifyBundle(String path, List<String> ignoredFields) throws JSONException {
        var patientMigrationRequest = patientMigrationRequestDao.getMigrationRequest(getConversationId());
        var expectedBundle = readResourceAsString(path).replaceAll(nhsNumberToBeReplaced, getPatientNhsNumber());
        var odsCodeToBeReplaced = getOdsToBeReplaced(expectedBundle);

        if (OVERWRITE_EXPECTED_JSON) {
            overwriteExpectJson(path, patientMigrationRequest.getBundleResource());
        }

        var bundle = fhirParserService.parseResource(patientMigrationRequest.getBundleResource(), Bundle.class);
        var combinedList = Stream.of(generateJsonPathIgnores(bundle), staticIgnoredJsonPaths)
                .flatMap(List::stream)
                .toList();

        expectedBundle = expectedBundle.replaceAll(odsCodeToBeReplaced, this.getLosingODSCode()).replaceAll(SPECIAL_CHARS, "");
        var actualBundle = patientMigrationRequest.getBundleResource().replaceAll(SPECIAL_CHARS, "");

        assertBundleContent(
                actualBundle,
                expectedBundle,
                Stream.concat(combinedList.stream(), ignoredFields.stream()).toList()
        );
    }

    private String getOdsToBeReplaced(String expectedBundle) {
        var startIndex = expectedBundle.toLowerCase().indexOf(PSS_ADAPTOR_URL.toLowerCase()) + PSS_ADAPTOR_URL.length();
        var endIndex = expectedBundle.toLowerCase().indexOf("\"", startIndex);

        return expectedBundle.substring(startIndex, endIndex);
    }

}