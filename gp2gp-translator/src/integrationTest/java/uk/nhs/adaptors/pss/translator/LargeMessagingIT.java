package uk.nhs.adaptors.pss.translator;

import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;
import static uk.nhs.adaptors.connector.model.MigrationStatus.CONTINUE_REQUEST_ACCEPTED;

import java.util.List;
import java.util.stream.Stream;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.nhs.adaptors.pss.util.BaseEhrHandler;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith({SpringExtension.class})
@DirtiesContext
@AutoConfigureMockMvc
public final class LargeMessagingIT extends BaseEhrHandler {

    private LargeMessagingIT() {
        setIgnoredJsonPaths(List.of(
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
        ));
    }

    @Test
    public void handleUk06WithCidAttachment() throws JSONException {
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_1/uk06.json");

        await().until(this::isEhrMigrationCompleted);

        verifyBundle("/json/LargeMessage/expectedBundleScenario1.json");
    }

    @Test
    public void handleUk06WithCompressedCidAttachmement() throws JSONException {
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_2/uk06.json");

        await().until(this::isEhrMigrationCompleted);

        verifyBundle("/json/LargeMessage/expectedBundleScenario2.json");
    }

    @Test
    public void handleUk06WithOneMidAttachment() throws JSONException {
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_3/uk06.json");

        await().until(this::hasContinueMessageBeenReceived);

        sendInboundMessageToQueue("/json/LargeMessage/Scenario_3/copc.json");

        await().until(this::isEhrMigrationCompleted);

        verifyBundle("/json/LargeMessage/expectedBundleScenario3.json");
    }

    @Test
    public void handleUk06WithSkeletonAsMid() throws JSONException {
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_5/uk06.json");

        await().until(this::hasContinueMessageBeenReceived);

        sendInboundMessageToQueue("/json/LargeMessage/Scenario_5/copc.json");

        await().until(this::isEhrMigrationCompleted);

        verifyBundle("/json/LargeMessage/expectedBundleScenario5.json");
    }

    @Test
    public void handleUk06WithSkeletonAsEntireRCMRInEHR() throws JSONException {
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_10/uk06.json");
        await().until(this::isEhrMigrationCompleted);

        verifyBundle("/json/LargeMessage/expectedBundleScenario10.json");
    }

    @Test
    public void handleUk06WithSkeletonAsEntireRCMRInCOPC() throws JSONException {
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_11/uk06.json");

        await().until(this::hasContinueMessageBeenReceived);

        sendInboundMessageToQueue("/json/LargeMessage/Scenario_11/copc.json");

        await().until(this::isEhrMigrationCompleted);

        verifyBundle("/json/LargeMessage/expectedBundleScenario11.json");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("ehrAndCopcMessageResourceFiles")
    public void uk06WithCopcMessages(String testName, String scenarioDirectory, String expectedBundleName) throws JSONException {
        sendInboundMessageToQueue(scenarioDirectory + "uk06.json");

        await().until(this::hasContinueMessageBeenReceived);

        sendInboundMessageToQueue(scenarioDirectory + "copc_index.json");
        sendInboundMessageToQueue(scenarioDirectory + "copc0.json");
        sendInboundMessageToQueue(scenarioDirectory + "copc1.json");

        await().until(this::isEhrMigrationCompleted);

        verifyBundle(expectedBundleName);
    }

    private static Stream<Arguments> ehrAndCopcMessageResourceFiles() {
        return Stream.of(
            Arguments.of("handleUk06WithFragmentedMids", "/json/LargeMessage/Scenario_4/",
                "/json/LargeMessage/expectedBundleScenario4.json"),
            Arguments.of("handleUk06WithMidAttachmentsWithCidAndMidCombo", "/json/LargeMessage/Scenario_6/",
                "/json/LargeMessage/expectedBundleScenario6.json"),
            Arguments.of("handleUk06WithSkeletonFragments", "/json/LargeMessage/Scenario_7/",
                "/json/LargeMessage/expectedBundleScenario7.json"),
            Arguments.of("handleUk06WithFragmentedMidCidCombo", "/json/LargeMessage/Scenario_8/",
                "/json/LargeMessage/expectedBundleScenario8.json"),
            Arguments.of("handleUk06WithOneMidAttachmentCheck", "/json/LargeMessage/Scenario_9/",
                "/json/LargeMessage/expectedBundleScenario9.json")
        );
    }

    private void sendInboundMessageToQueue(String json) {
        var jsonMessage = readResourceAsString(json)
            .replace(NHS_NUMBER_PLACEHOLDER, getPatientNhsNumber())
            .replace(CONVERSATION_ID_PLACEHOLDER, getConversationId());
        getMhsJmsTemplate().send(session -> session.createTextMessage(jsonMessage));
    }
    private boolean hasContinueMessageBeenReceived() {
        var migrationStatusLog = getMigrationStatusLogService().getLatestMigrationStatusLog(getConversationId());
        return CONTINUE_REQUEST_ACCEPTED.equals(migrationStatusLog.getMigrationStatus());
    }
}