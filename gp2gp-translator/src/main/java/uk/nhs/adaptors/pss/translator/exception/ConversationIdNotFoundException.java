package uk.nhs.adaptors.pss.translator.exception;

import lombok.Getter;

@Getter
public class ConversationIdNotFoundException extends RuntimeException {

    private final String conversationId;
    public ConversationIdNotFoundException(String message, String conversationId) {
        super(message);
        this.conversationId = conversationId;
    }
}
