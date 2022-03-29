package uk.nhs.adaptors.pss.translator.storage;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class StorageManagerService {

    // Configuration singleton parameters
    private final StorageService _storageService;

    public StorageManagerService(StorageService storageService){
        _storageService = storageService;
    }

    public void UploadFile(String filename, byte[] fileAsString) throws StorageException {

        Integer retryAttempts = 0;
        Boolean successful = Boolean.FALSE;
        while (retryAttempts < 3) {
            try {
                _storageService.UploadFile(filename, fileAsString);
                successful = ValidateUploadedFile(filename, fileAsString);
                if (successful) {
                    retryAttempts = 3;
                } else {
                    DeleteFile(filename);
                    retryAttempts++;
                }
            }
            catch (Exception e) {
                throw new StorageException("Error occurred uploading to Local Storage", e);
            }
        }
    }

    public byte[] DownloadFile(String filename) throws StorageException {
        try {
            byte[] byteResponse = _storageService.DownloadFile(filename);
            return byteResponse;
        }
        catch (Exception e) {
            throw new StorageException("Error occurred downloading from Local Storage", e);
        }
    }

    public void DeleteFile(String filename){
        try {
            _storageService.DeleteFile(filename);
        }
        catch (Exception e) {
            throw new StorageException("Error occurred deleting from Local Storage", e);
        }
    }

    private Boolean ValidateUploadedFile(String filename, byte[] fileAsString) throws StorageException {
        return fileAsString.equals(_storageService.DownloadFile(filename));
    }

}
