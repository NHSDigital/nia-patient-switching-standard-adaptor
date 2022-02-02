package uk.nhs.adaptors.common.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EntityScan(basePackages = {"uk.nhs.adaptors.common"})
@ComponentScan(basePackages = {"uk.nhs.adaptors.common"})
public class CommonConfiguration {
}
