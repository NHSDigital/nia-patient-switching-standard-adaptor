package uk.nhs.adaptors.pss.translator.task.scheduled;

import static uk.nhs.adaptors.connector.model.MigrationStatus.CONTINUE_REQUEST_ACCEPTED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_RECEIVED;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.connector.model.MessagePersistDuration;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MessagePersistDurationService;
import uk.nhs.adaptors.connector.service.PatientMigrationRequestService;
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
    private final PatientMigrationRequestService migrationRequestService;

    @Scheduled(cron = CRON_TIME)
    public void checkForTimeouts() {
        LOGGER.info("running scheduled task");

        // get migrations with status EHR_EXTRACT_RECEIVED or CONTINUE_REQUEST_ACCEPTED

        List<PatientMigrationRequest> extractReceivedRequests =
            migrationRequestService.getMigrationRequestByCurrentMigrationStatus(EHR_EXTRACT_RECEIVED);
        List<PatientMigrationRequest> largeMessageRequests =
            migrationRequestService.getMigrationRequestByCurrentMigrationStatus(CONTINUE_REQUEST_ACCEPTED);

        // TODO: iterate through the migration requests:
        //  - update the persist durations and get the number of COPC messages from the DB (if the migration contains large messages)
        //  - do the timeout calculation
        //  - send the NACK message if the migration has timed out
        //  - potentially clear unnecessary data from the db after a timeout?

    }

    private MessagePersistDuration getPersistDurationFor(PatientMigrationRequest migrationRequest, String messageType) {

        Optional<MessagePersistDuration> messageDurationOptional =
            messagePersistDurationService.getMessagePersistDuration(migrationRequest.getId(), messageType);

        if (messageDurationOptional.isEmpty()
            || messageDurationOptional.get().getCallsSinceUpdate() >= FREQUENCY_OF_PERSIST_DURATION_UPDATE) {

            return messagePersistDurationService.addMessagePersistDuration(
                messageType,
                sdsService.getPersistDurationFor(messageType, migrationRequest.getLosingPracticeOdsCode()),
                1,
                migrationRequest.getId()
            );
        }

        return messageDurationOptional.map(mpd ->
            messagePersistDurationService.addMessagePersistDuration(
                mpd.getMessageType(),
                mpd.getPersistDuration(),
                mpd.getCallsSinceUpdate() + 1,
                migrationRequest.getId()
            )
        ).orElseThrow();
    }
}
