package uk.nhs.adaptors.pss.translator;

import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.adaptors.pss.util.BaseEhrHandler;

import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;
import static uk.nhs.adaptors.connector.model.MigrationStatus.CONTINUE_REQUEST_ACCEPTED;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith({SpringExtension.class})
@DirtiesContext
@AutoConfigureMockMvc
public class COPCHandlingIT extends BaseEhrHandler {

    @Test
    public void handleCOPCAndSaveMessageIdInDatabase() throws JSONException {
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_3/uk06.json");

        await().until(this::hasContinueMessageBeenReceived);

        sendInboundMessageToQueue("/json/LargeMessage/Scenario_3/copc.json");

        await().until(this::isEhrMigrationCompleted);

        var migrationStatusLog = getMigrationStatusLogService().getLatestMigrationStatusLog(getConversationId());

        Assertions.assertNotNull(migrationStatusLog.getMessageId());
        Assertions.assertNotEquals(migrationStatusLog.getMessageId(), "");

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