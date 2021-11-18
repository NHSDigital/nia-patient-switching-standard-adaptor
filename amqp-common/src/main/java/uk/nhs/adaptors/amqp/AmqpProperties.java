package uk.nhs.adaptors.amqp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "amqp")
@Getter
@Setter
public class AmqpProperties {
    private String broker;
    private String username;
    private String password;
    private String gpcFacadeQueue;
    private String mhsAdaptorQueue;
    private int maxRedeliveries;
}
