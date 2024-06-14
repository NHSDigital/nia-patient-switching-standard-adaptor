package uk.nhs.adaptors.pss.translator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableScheduling;

import uk.nhs.adaptors.common.config.CommonConfiguration;
import uk.nhs.adaptors.connector.config.DbConnectorConfiguration;

@Configuration
@EnableJms
@EnableScheduling
@Import({DbConnectorConfiguration.class, CommonConfiguration.class})
public class ApplicationConfiguration {
}
