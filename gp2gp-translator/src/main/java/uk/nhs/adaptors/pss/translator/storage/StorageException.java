package uk.nhs.adaptors.pss.translator.storage;

public class StorageException extends RuntimeException {
    public StorageException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}