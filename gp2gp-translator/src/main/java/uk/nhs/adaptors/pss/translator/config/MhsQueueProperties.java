package uk.nhs.adaptors.pss.translator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "amqp.mhs")
@Getter
@Setter
public class MhsQueueProperties implements QueueProperties {

    private String queueName;
    private String broker;
    private String username;
    private String password;
    private int maxRedeliveries;
    private String dlqPrefix;
    private int closeTimeout;

    public String getDLQName() {
        return getDlqPrefix() + getQueueName();
    }
}
