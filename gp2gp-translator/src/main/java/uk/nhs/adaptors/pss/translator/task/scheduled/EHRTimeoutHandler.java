package uk.nhs.adaptors.pss.translator.task.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.connector.model.MessagePersistDuration;
import uk.nhs.adaptors.connector.service.MessagePersistDurationService;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class EHRTimeoutHandler {

    private static final String CRON_TIME = "*/10 * * * * SUN-SAT";

    private final MessagePersistDurationService messagePersistDurationService;

    @SuppressWarnings("checkstyle:MagicNumber")
    @Scheduled(cron = CRON_TIME)
    public void checkForTimeouts() {
        LOGGER.info("running scheduled task");

        // save or update
        messagePersistDurationService.addMessagePersistDuration("TestMessage", 2000, 0);

        // get from database
        MessagePersistDuration returnedMPD = messagePersistDurationService.getMessagePersistDuration("TestMessage");

        // print to log
        LOGGER.info("persist duration of [{}] stored for message [{}]", returnedMPD.getPersistDuration(), returnedMPD.getMessageType());
    }
}
