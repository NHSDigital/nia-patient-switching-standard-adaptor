package uk.nhs.adaptors.common.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;

@Configuration
@EntityScan(basePackages = {"uk.nhs.adaptors.common"})
@ComponentScan(basePackages = {"uk.nhs.adaptors.common"})
public class CommonConfiguration {

    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forDstu3();
    }
}
