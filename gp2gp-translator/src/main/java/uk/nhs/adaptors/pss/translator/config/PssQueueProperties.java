package uk.nhs.adaptors.pss.translator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "amqp.pss")
@Getter
@Setter
public class PssQueueProperties implements QueueProperties {

    private String queueName;
    private String broker;
    private String username;
    private String password;
    private int maxRedeliveries;
    private int closeTimeout;
    private int sendTimeout;
}
