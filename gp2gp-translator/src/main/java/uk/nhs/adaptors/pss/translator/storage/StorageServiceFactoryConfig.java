package uk.nhs.adaptors.pss.translator.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageServiceFactoryConfig {

    @Autowired
    @Bean(name = "storage-service")
    public StorageServiceFactory storageServiceFactory() {
        StorageServiceFactory factory = new StorageServiceFactory();
        return factory;
    }

    @Bean
    public StorageService storageService() {
        return storageServiceFactory().getObject();
    }
}