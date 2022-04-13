package uk.nhs.adaptors.connector.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.connector.dao.MessagePersistDurationDao;
import uk.nhs.adaptors.connector.model.MessagePersistDuration;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class MessagePersistDurationService {
    private final MessagePersistDurationDao messagePersistDurationDao;

    public void addMessagePersistDuration(String messageType, int persistDuration, int callsSinceUpdate) {
        messagePersistDurationDao.saveMessagePersistDuration(messageType, persistDuration, callsSinceUpdate);
    }

    public MessagePersistDuration getMessagePersistDuration(String messageType) {
        int messageId = messagePersistDurationDao.getMessagePersistDurationId(messageType);
        System.out.println(messageId);
        return messagePersistDurationDao.getMessagePersistDuration(messageId);
    }
}
