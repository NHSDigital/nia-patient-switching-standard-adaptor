package uk.nhs.adaptors.pss.translator.storage;

public interface StorageService {
    void UploadFile(String filename, byte[] fileAsString) throws StorageException;
    byte[] DownloadFile(String filename) throws StorageException;
    void DeleteFile(String filename) throws StorageException;
}
