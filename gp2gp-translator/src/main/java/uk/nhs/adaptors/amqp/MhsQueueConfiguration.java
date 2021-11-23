package uk.nhs.adaptors.amqp;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;

@Configuration
@EnableJms
public class MhsQueueConfiguration extends AmqpConfiguration{
    @Bean
    public JmsConnectionFactory jmsConnectionFactory(MhsQueueProperties properties) {
        return super.jmsConnectionFactory(properties);
    }
}
