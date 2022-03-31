package uk.nhs.adaptors.pss.translator.storage;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

@Setter
public class StorageServiceFactory {

    private StorageService storageService;

    @Autowired
    private StorageServiceConfiguration configuration;

    //    @Override
    public StorageService getObject() {
        if (storageService == null) {
            switch (StorageServiceOptionsEnum.enumOf(configuration.getType())) {
                case S3:
                    storageService = new AWSStorageService(configuration);
                    break;
                case AZURE:
                    storageService = new AzureStorageService(configuration);
                    break;
                default:
                    storageService = new LocalStorageService();
            }
        }
        return storageService;
    }

    //    @Override
    public Class<?> getObjectType() {
        return StorageService.class;
    }
}