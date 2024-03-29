package uk.nhs.adaptors.pss.translator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "amqp.gp2gp")
@Getter
@Setter
public class Gp2GpAdaptorQueueProperties {
    private String queueName;
    private String broker;
    private String username;
    private String password;
}
