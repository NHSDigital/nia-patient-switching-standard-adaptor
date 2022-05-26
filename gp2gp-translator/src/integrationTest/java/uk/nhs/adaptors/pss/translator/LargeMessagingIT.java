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
import org.junit.jupiter.params.provider.Arguments;
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
import uk.nhs.adaptors.connector.service.PatientAttachmentLogService;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.util.BaseEhrHandler;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith({SpringExtension.class})
@DirtiesContext
@AutoConfigureMockMvc
public class LargeMessagingIT extends BaseEhrHandler {

    static {
        STATIC_IGNORED_JSON_PATHS = List.of(
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

        await().until(this::hasContinueMessageBeenReceived);

        sendInboundMessageToQueue("/json/LargeMessage/Scenario_3/copc.json");

        await().until(this::isEhrExtractTranslated);

        verifyBundle("/json/LargeMessage/expectedBundleScenario3.json");
    }

    // Test case 4: UK06 with fragment mid attachments
    @Test
    public void handleUk06WithFragmentedMids() throws JSONException {
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_4/uk06.json");

        await().until(this::hasContinueMessageBeenReceived);

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

        await().until(this::hasContinueMessageBeenReceived);

        sendInboundMessageToQueue("/json/LargeMessage/Scenario_5/copc.json");

        await().until(this::isEhrExtractTranslated);

        verifyBundle("/json/LargeMessage/expectedBundleScenario5.json");
    }

    // Test case 6: UK06 with mid attachment with cid mid combo
    @Test
    public void handleUk06WithMidAttachmentsWithCidAndMidCombo() throws JSONException {
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_6/uk06.json");

        await().until(this::hasContinueMessageBeenReceived);

        sendInboundMessageToQueue("/json/LargeMessage/Scenario_6/copc_index.json");
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_6/copc0.json");
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_6/copc1.json");

        await().until(this::isEhrExtractTranslated);

        verifyBundle("/json/LargeMessage/expectedBundleScenario6.json");
    }

    // Test case 7: UK06 with skeleton with fragments
    @Test
    public void handleUk06WithSkeletonFragments() throws JSONException {
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_7/uk06.json");

        await().until(this::hasContinueMessageBeenReceived);

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

        await().until(this::hasContinueMessageBeenReceived);

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

        await().until(this::hasContinueMessageBeenReceived);

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
    
    private boolean hasContinueMessageBeenReceived() {
        var migrationStatusLog = migrationStatusLogService.getLatestMigrationStatusLog(conversationId);
        return CONTINUE_REQUEST_ACCEPTED.equals(migrationStatusLog.getMigrationStatus());
    }
}