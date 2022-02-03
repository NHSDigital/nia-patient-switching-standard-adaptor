package uk.nhs.adaptors.pss.translator.mhs.exception;

public class InvalidInboundMessageException extends RuntimeException {
    public InvalidInboundMessageException(String message, Throwable cause) {
        super(message, cause);
    }
    public InvalidInboundMessageException(String message) {
        super(message);
    }
}
