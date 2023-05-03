package uk.nhs.adaptors.pss.translator.exception;

public class InlineAttachmentProcessingException extends Exception {
    public InlineAttachmentProcessingException(String message) {
        super(message);
    }

    public InlineAttachmentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
