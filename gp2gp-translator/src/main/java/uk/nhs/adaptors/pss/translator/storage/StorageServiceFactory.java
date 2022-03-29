package uk.nhs.adaptors.pss.translator.storage;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

@Setter
public class StorageServiceFactory {

    private StorageService _storageService;

    @Autowired
    private StorageServiceConfiguration _configuration;

    //    @Override
    public StorageService getObject() {
        if (_storageService == null) {
            switch (StorageServiceOptionsEnum.enumOf(_configuration.getType())) {
                case S3:
                    _storageService = new AWSStorageService(_configuration);
                    break;
                case AZURE:
                    _storageService = new AzureStorageService(_configuration);
                    break;
                default:
                    _storageService = new LocalStorageService();
            }
        }
        return _storageService;
    }

    //    @Override
    public Class<?> getObjectType() {
        return StorageService.class;
    }
}