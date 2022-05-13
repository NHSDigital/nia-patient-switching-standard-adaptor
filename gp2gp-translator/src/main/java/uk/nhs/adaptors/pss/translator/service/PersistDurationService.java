package uk.nhs.adaptors.pss.translator.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.nhs.adaptors.connector.model.MessagePersistDuration;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MessagePersistDurationService;
import uk.nhs.adaptors.pss.translator.exception.SdsRetrievalException;

import java.time.Duration;
import java.util.Optional;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class PersistDurationService {

    private static final int FREQUENCY_OF_PERSIST_DURATION_UPDATE = 3;

    private MessagePersistDurationService messagePersistDurationService;
    private SDSService sdsService;

    public Duration getPersistDurationFor(PatientMigrationRequest migrationRequest, String messageType) throws SdsRetrievalException {

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
