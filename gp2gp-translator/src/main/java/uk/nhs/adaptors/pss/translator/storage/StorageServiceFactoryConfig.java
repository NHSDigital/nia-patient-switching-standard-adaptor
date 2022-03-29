package uk.nhs.adaptors.pss.translator.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageServiceFactoryConfig {
    @Autowired
//    private StorageServiceConfiguration configuration;

    @Bean(name = "storage-connector")
    public StorageServiceFactory storageServiceFactory() {
        StorageServiceFactory factory = new StorageServiceFactory();
//        factory.setConfiguration(configuration);
        return factory;
    }

    @Bean
    public StorageService storageService() {
        return storageServiceFactory().getObject();
    }
}
