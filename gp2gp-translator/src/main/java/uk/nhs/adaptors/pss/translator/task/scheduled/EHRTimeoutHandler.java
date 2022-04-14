package uk.nhs.adaptors.pss.translator.task.scheduled;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.connector.model.MessagePersistDuration;
import uk.nhs.adaptors.connector.service.MessagePersistDurationService;
import uk.nhs.adaptors.pss.translator.service.SDSService;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class EHRTimeoutHandler {

    private static final String CRON_TIME = "*/10 * * * * SUN-SAT";
    private static final String EHR_EXTRACT_MESSAGE_NAME = "RCMR_IN030000UK06";
    private static final String COPC_MESSAGE_NAME = "COPC_IN000001UK01";
    private static final int FREQUENCY_OF_PERSIST_DURATION_UPDATE = 3;

    private final MessagePersistDurationService messagePersistDurationService;
    private final SDSService sdsService;

    @Scheduled(cron = CRON_TIME)
    public void checkForTimeouts() {
        LOGGER.info("running scheduled task");

        // update persist durations

        MessagePersistDuration ehrPersistDuration = getPersistDurationFor(EHR_EXTRACT_MESSAGE_NAME);
        MessagePersistDuration copcPersistDuration = getPersistDurationFor(COPC_MESSAGE_NAME);

        LOGGER.debug("EHR Extract Persist duration [{}]", ehrPersistDuration.getPersistDuration());
        LOGGER.debug("COPC Persist duration [{}]", copcPersistDuration.getPersistDuration());

        // TODO: get migrations with status EHR_EXTRACT_RECEIVED

    }

    private MessagePersistDuration getPersistDurationFor(String messageType) {

        Optional<MessagePersistDuration> messageDurationOptional = messagePersistDurationService.getMessagePersistDuration(messageType);

        if (messageDurationOptional.isEmpty()
            || messageDurationOptional.get().getCallsSinceUpdate() >= FREQUENCY_OF_PERSIST_DURATION_UPDATE) {
            return messagePersistDurationService.addMessagePersistDuration(
                messageType,
                sdsService.getPersistDurationFor(messageType),
                1
            );
        }

        return messageDurationOptional.map(mpd ->
            messagePersistDurationService.addMessagePersistDuration(
                mpd.getMessageType(),
                mpd.getPersistDuration(),
                mpd.getCallsSinceUpdate() + 1
            )
        ).orElseThrow();
    }
}
