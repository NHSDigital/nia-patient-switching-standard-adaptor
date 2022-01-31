package uk.nhs.adaptors.pss.translator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import uk.nhs.adaptors.connector.configuration.DbConnectorConfiguration;

@Configuration
@Import(DbConnectorConfiguration.class)
public class ApplicationConfiguration {
}
