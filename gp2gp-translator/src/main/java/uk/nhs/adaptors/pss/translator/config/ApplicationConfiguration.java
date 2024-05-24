package uk.nhs.adaptors.pss.translator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

import uk.nhs.adaptors.common.config.CommonConfiguration;
import uk.nhs.adaptors.connector.config.DbConnectorConfiguration;

@Configuration
@EnableRetry
@EnableScheduling
@Import({DbConnectorConfiguration.class, CommonConfiguration.class})
public class ApplicationConfiguration {
}
