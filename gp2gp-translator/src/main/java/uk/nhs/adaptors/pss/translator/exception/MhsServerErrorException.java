package uk.nhs.adaptors.pss.translator.exception;

public class MhsServerErrorException extends RuntimeException {
    public MhsServerErrorException(String message) {
        super(message);
    }
}
