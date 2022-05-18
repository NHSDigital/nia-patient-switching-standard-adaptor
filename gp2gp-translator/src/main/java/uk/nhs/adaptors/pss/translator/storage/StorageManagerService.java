package uk.nhs.adaptors.pss.translator.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StorageManagerService {

    private final StorageService storageService;
    private final StorageServiceConfiguration configuration;

    public void uploadFile(String filename, StorageDataUploadWrapper dataWrapper, String conversationId) throws StorageException {

        Integer retryAttempts = 0;
        Integer retryLimit = configuration.getRetryLimit();

        Boolean successful = Boolean.FALSE;

        var clashPreventionFilename = createFilename(filename, conversationId);

        while (retryAttempts < retryLimit) {
            try {
                storageService.uploadFile(clashPreventionFilename, dataWrapper.getData());
                successful = validateUploadedFile(clashPreventionFilename, dataWrapper.getData());
                if (successful) {
                    retryAttempts = retryLimit;
                } else {
                    deleteFile(filename, conversationId);
                    retryAttempts++;
                    if (retryAttempts.equals(retryLimit)) {
                        throw new Exception();
                    }
                }
            } catch (Exception e) {
                throw new StorageException("Error occurred uploading to Storage", e);
            }
        }
    }

    public byte[] downloadFile(String filename, String conversationId) throws StorageException {
        try {
            var clashPreventionFilename = createFilename(filename, conversationId);
            byte[] byteResponse = storageService.downloadFile(clashPreventionFilename);
            return byteResponse;
        } catch (Exception e) {
            throw new StorageException("Error occurred downloading from Storage", e);
        }
    }

    public void deleteFile(String filename, String conversationId) {
        try {
            var clashPreventionFilename = createFilename(filename, conversationId);
            storageService.deleteFile(clashPreventionFilename);
        } catch (Exception e) {
            throw new StorageException("Error occurred deleting from Storage", e);
        }
    }

    public String getFileLocation(String filename, String conversationId) {
        try {
            var clashPreventionFilename = createFilename(filename, conversationId);
            return storageService.getFileLocation(clashPreventionFilename);
        } catch (Exception e) {
            throw new StorageException("Error occurred getting file location from Storage", e);
        }
    }

    private Boolean validateUploadedFile(String filename, byte[] fileAsString) throws StorageException {
        byte[] downloadedFile = storageService.downloadFile(filename);
        return Arrays.equals(fileAsString, downloadedFile);
    }

    private String createFilename(String filename, String conversationId) {
        return conversationId + "_" + filename;
    }

}
