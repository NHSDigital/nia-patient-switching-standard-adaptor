package uk.nhs.adaptors.pss.translator.storage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageServiceFactoryConfig {

    @Bean(name = "storage-service")
    public StorageServiceFactory storageServiceFactory() {
        return new StorageServiceFactory();
    }

    @Bean
    public StorageService storageService() {
        return storageServiceFactory().getObject();
    }
}