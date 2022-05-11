package uk.nhs.adaptors.pss.translator.service;

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.connector.model.MessagePersistDuration;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MessagePersistDurationService;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class PersistDurationService {

    private static final int FREQUENCY_OF_PERSIST_DURATION_UPDATE = 3;

    private MessagePersistDurationService messagePersistDurationService;
    private SDSService sdsService;


    //
    public Duration getPersistDurationFor(PatientMigrationRequest migrationRequest, String messageType) {

        Optional<MessagePersistDuration> messageDurationOptional =
            messagePersistDurationService.getMessagePersistDuration(migrationRequest.getId(), messageType);

        if (messageDurationOptional.isEmpty()
            || messageDurationOptional.get().getCallsSinceUpdate() >= FREQUENCY_OF_PERSIST_DURATION_UPDATE) {

            return messagePersistDurationService.addMessagePersistDuration(
                messageType,

                sdsService.getPersistDurationFor(messageType, migrationRequest.getLosingPracticeOdsCode(),
                    migrationRequest.getConversationId()),
                1,
                migrationRequest.getId()
            ).getPersistDuration();
        }

        return messageDurationOptional.map(mpd ->
            messagePersistDurationService.addMessagePersistDuration(
                mpd.getMessageType(),
                mpd.getPersistDuration(),
                mpd.getCallsSinceUpdate() + 1,
                migrationRequest.getId()
            ).getPersistDuration()
        ).orElseThrow();
    }
}
cx
