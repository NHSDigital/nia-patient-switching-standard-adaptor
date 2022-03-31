package uk.nhs.adaptors.pss.translator.storage;

public interface StorageService {
    void uploadFile(String filename, byte[] fileAsString) throws StorageException;
    byte[] downloadFile(String filename) throws StorageException;
    void deleteFile(String filename) throws StorageException;
}
