package uk.nhs.adaptors.amqp;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;

@Configuration
@EnableJms
public class PssQueueConfiguration extends AmqpConfiguration{
    @Bean
    public JmsConnectionFactory jmsConnectionFactory(PssQueueProperties properties) {
        return super.jmsConnectionFactory(properties);
    }
}
