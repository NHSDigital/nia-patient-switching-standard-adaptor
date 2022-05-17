package uk.nhs.adaptors.connector.service;

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.connector.dao.MessagePersistDurationDao;
import uk.nhs.adaptors.connector.model.MessagePersistDuration;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class MessagePersistDurationService {
    private final MessagePersistDurationDao messagePersistDurationDao;

    public MessagePersistDuration addMessagePersistDuration(String messageType, Duration persistDuration, int callsSinceUpdate,
        int migrationRequestId) {
        messagePersistDurationDao.saveMessagePersistDuration(messageType, persistDuration.toSeconds(), callsSinceUpdate,
            migrationRequestId);
        return messagePersistDurationDao.getMessagePersistDuration(migrationRequestId, messageType);
    }

    public Optional<MessagePersistDuration> getMessagePersistDuration(int migrationRequestId, String messageType) {
        return messagePersistDurationDao.messageTypeExists(migrationRequestId, messageType)
            ? Optional.of(messagePersistDurationDao.getMessagePersistDuration(migrationRequestId, messageType))
            : Optional.empty();
    }
}
