package uk.nhs.adaptors.pss.translator;

import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;
import static uk.nhs.adaptors.connector.model.MigrationStatus.CONTINUE_REQUEST_ACCEPTED;

import java.util.List;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.nhs.adaptors.pss.util.BaseEhrHandler;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith({SpringExtension.class})
@DirtiesContext
@AutoConfigureMockMvc
public class LargeMessagingIT extends BaseEhrHandler {

    private LargeMessagingIT (){
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
            .replace(NHS_NUMBER_PLACEHOLDER, getPatientNhsNumber())
            .replace(CONVERSATION_ID_PLACEHOLDER, getConversationId());
        getMhsJmsTemplate().send(session -> session.createTextMessage(jsonMessage));
    }
    private boolean hasContinueMessageBeenReceived() {
        var migrationStatusLog = getMigrationStatusLogService().getLatestMigrationStatusLog(getConversationId());
        return CONTINUE_REQUEST_ACCEPTED.equals(migrationStatusLog.getMigrationStatus());
    }
}