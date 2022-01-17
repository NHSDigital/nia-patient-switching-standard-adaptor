package uk.nhs.adaptors.pss.gpc.exception;

public class FhirValidationException extends RuntimeException {
    public FhirValidationException(String message) {
        super(message);
    }
}
