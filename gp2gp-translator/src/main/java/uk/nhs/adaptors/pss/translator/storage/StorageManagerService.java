package uk.nhs.adaptors.pss.translator.storage;

import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class StorageManagerService {

    // Configuration singleton parameters
    private final StorageService storageService;

    private final StorageServiceConfiguration configuration;


    public StorageManagerService(StorageService storageService, StorageServiceConfiguration configuration) {
        this.storageService = storageService;
        this.configuration = configuration;
    }

    public void uploadFile(String filename, StorageDataWrapper dataWrapper) throws StorageException {

        Integer retryAttempts = 0;
        Integer retryLimit = configuration.getRetryLimit();

        Boolean successful = Boolean.FALSE;
        while (retryAttempts < retryLimit) {
            try {
                storageService.uploadFile(filename, dataWrapper.getData());
                successful = validateUploadedFile(filename, dataWrapper.getData());
                if (successful) {
                    retryAttempts = retryLimit;
                } else {
                    deleteFile(filename);
                    retryAttempts++;
                    if (retryAttempts == retryLimit) {
                        throw new Exception();
                    }
                }
            } catch (Exception e) {
                throw new StorageException("Error occurred uploading to Storage", e);
            }
        }
    }

    public byte[] downloadFile(String filename) throws StorageException {
        try {
            byte[] byteResponse = storageService.downloadFile(filename);
            return byteResponse;
        } catch (Exception e) {
            throw new StorageException("Error occurred downloading from Storage", e);
        }
    }

    public void deleteFile(String filename) {
        try {
            storageService.deleteFile(filename);
        } catch (Exception e) {
            throw new StorageException("Error occurred deleting from Storage", e);
        }
    }

    private Boolean validateUploadedFile(String filename, byte[] fileAsString) throws StorageException {
        byte[] downloadedFile = storageService.downloadFile(filename);
        return Arrays.equals(fileAsString, downloadedFile);
    }

}
