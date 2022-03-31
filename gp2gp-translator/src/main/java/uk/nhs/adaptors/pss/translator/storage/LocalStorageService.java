package uk.nhs.adaptors.pss.translator.storage;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class LocalStorageService implements StorageService {

    // Configuration singleton parameters
    private final Map<String, byte[]> storage;

    public LocalStorageService() {
        storage = new HashMap<>();
    }

    public void uploadFile(String filename, byte[] fileAsString) throws StorageException {
        try {
            storage.put(filename, fileAsString);
        } catch (Exception e) {
            throw new StorageException("Error occurred uploading to Local Storage", e);
        }
    }

    public byte[] downloadFile(String filename) throws StorageException {
        try {
            InputStream inputStream = downloadFileToStream(filename);
            return IOUtils.toByteArray(inputStream);
        } catch (Exception e) {
            throw new StorageException("Error occurred downloading from Local Storage", e);
        }
    }

    public void deleteFile(String filename) {
        try {
            storage.remove(filename);
        } catch (Exception e) {
            throw new StorageException("Error occurred deleting from Local Storage", e);
        }
    }

    private InputStream downloadFileToStream(String filename) throws StorageException {
        try {
            byte[] objectBytes = storage.get(filename);
            return new ByteArrayInputStream(objectBytes);
        } catch (Exception exception) {
            throw new StorageException("Error occurred downloading from Local Storage", exception);
        }
    }
}
