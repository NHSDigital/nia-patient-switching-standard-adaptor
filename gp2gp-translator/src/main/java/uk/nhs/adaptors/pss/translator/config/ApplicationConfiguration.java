package uk.nhs.adaptors.pss.translator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import uk.nhs.adaptors.connector.configuration.DbConnectorConfiguration;

@Configuration
@Getter
@Setter
@Component
@Import(DbConnectorConfiguration.class)
@ConfigurationProperties(prefix = "general")
public class ApplicationConfiguration {

    private String fromOdsCode;

}
