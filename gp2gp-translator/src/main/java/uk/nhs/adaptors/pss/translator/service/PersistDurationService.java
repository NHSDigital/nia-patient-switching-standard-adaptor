package uk.nhs.adaptors.pss.translator.service;

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.connector.model.MessagePersistDuration;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MessagePersistDurationService;
import uk.nhs.adaptors.pss.translator.config.TimeoutProperties;
import uk.nhs.adaptors.pss.translator.exception.SdsRetrievalException;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class PersistDurationService {

    private TimeoutProperties timeoutProperties;
    private MessagePersistDurationService messagePersistDurationService;
    private SDSService sdsService;

    public Duration getPersistDurationFor(PatientMigrationRequest migrationRequest, String messageType) throws SdsRetrievalException {

        Optional<MessagePersistDuration> messageDurationOptional =
            messagePersistDurationService.getMessagePersistDuration(migrationRequest.getId(), messageType);

        if (messageDurationOptional.isEmpty()
            || messageDurationOptional.get().getCallsSinceUpdate() >= timeoutProperties.getSdsPollFrequency()) {

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
