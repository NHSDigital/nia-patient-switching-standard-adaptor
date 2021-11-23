package uk.nhs.adaptors.amqp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "amqp.mhsQueue")
@Getter
@Setter
public class MhsQueueProperties extends AmqpProperties{
}
