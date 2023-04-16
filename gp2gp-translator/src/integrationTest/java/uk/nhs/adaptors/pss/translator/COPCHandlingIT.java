package uk.nhs.adaptors.pss.translator;

import org.apache.commons.lang3.StringUtils;
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
import static uk.nhs.adaptors.common.enums.MigrationStatus.COPC_MESSAGE_PROCESSING;
import static uk.nhs.adaptors.common.enums.MigrationStatus.CONTINUE_REQUEST_ACCEPTED;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith({SpringExtension.class})
@DirtiesContext
@AutoConfigureMockMvc
public class COPCHandlingIT extends BaseEhrHandler {

    @Test
    public void handleCOPCAndSaveMessageIdInDatabase() {
        sendInboundMessageToQueue("/json/LargeMessage/Scenario_3/uk06.json");

        await().until(this::hasContinueMessageBeenReceived);

        sendInboundMessageToQueue("/json/LargeMessage/Scenario_3/copc.json");

        // ideally we should be checking for processing message here, but can fail due to database timing issues.
        await().until(this::isEhrMigrationCompleted);

        var migrationStatusLogs = getMigrationStatusLogService().getMigrationStatusLogs(getConversationId());

        var migrationStatusLog = migrationStatusLogs
                .stream()
                .filter(log -> log.getMigrationStatus() == COPC_MESSAGE_PROCESSING)
                .findFirst();

        Assertions.assertNotNull(migrationStatusLog);
        Assertions.assertNotNull(migrationStatusLog.get().getMessageId());
        Assertions.assertNotEquals(migrationStatusLog.get().getMessageId(), StringUtils.EMPTY);
    }

    @Test
    public void handleCOPCMessageWithMissingAttachment() {
        sendInboundMessageToQueue("/json/LargeMessage/NewError/uk06.json");

        await().until(this::hasContinueMessageBeenReceived);

        sendInboundMessageToQueue("/json/LargeMessage/NewError/copc_error_missing_attachment.json");

        await().until(this::isCopcFailed);

        var migrationStatusLog = getMigrationStatusLogService().getLatestMigrationStatusLog(getConversationId());


        Assertions.assertEquals(migrationStatusLog.getMigrationStatus().toString(), "COPC_FAILED");

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