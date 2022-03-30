package uk.nhs.adaptors.pss.translator.storage;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class StorageManagerService {

    // Configuration singleton parameters
    private final StorageService _storageService;

    public StorageManagerService(StorageService storageService){
        _storageService = storageService;
    }

    public void UploadFile(String filename, StorageDataWrapper dataWrapper) throws StorageException {

        Integer retryAttempts = 0;
        Boolean successful = Boolean.FALSE;
        while (retryAttempts < 3) {
            try {
                _storageService.UploadFile(filename, dataWrapper.getData());
                successful = ValidateUploadedFile(filename, dataWrapper.getData());
                if (successful) {
                    retryAttempts = 3;
                } else {
                    DeleteFile(filename);
                    retryAttempts++;
                    if (retryAttempts == 3) {
                        throw new Exception();
                    }
                }
            }
            catch (Exception e) {
                throw new StorageException("Error occurred uploading to Storage", e);
            }
        }
    }

    public byte[] DownloadFile(String filename) throws StorageException {
        try {
            byte[] byteResponse = _storageService.DownloadFile(filename);
            return byteResponse;
        }
        catch (Exception e) {
            throw new StorageException("Error occurred downloading from Storage", e);
        }
    }

    public void DeleteFile(String filename){
        try {
            _storageService.DeleteFile(filename);
        }
        catch (Exception e) {
            throw new StorageException("Error occurred deleting from Storage", e);
        }
    }

    private Boolean ValidateUploadedFile(String filename, byte[] fileAsString) throws StorageException {
        byte [] downloadedFile = _storageService.DownloadFile(filename);
        return Arrays.equals(fileAsString, downloadedFile);
    }

}
