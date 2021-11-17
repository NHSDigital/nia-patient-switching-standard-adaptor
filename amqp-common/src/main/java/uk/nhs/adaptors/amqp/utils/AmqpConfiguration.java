package uk.nhs.adaptors.amqp.utils;

import static org.springframework.util.StringUtils.isEmpty;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;

@Configuration
@EnableJms
public class AmqpConfiguration {
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new MappingJackson2MessageConverter();
    }

    @Bean
    public JmsConnectionFactory jmsConnectionFactory(AmqpProperties properties) {
        JmsConnectionFactory factory = new JmsConnectionFactory();

        factory.setRemoteURI(properties.getBroker());

        if (!isEmpty(properties.getUsername())) {
            factory.setUsername(properties.getUsername());
        }

        if (!isEmpty(properties.getPassword())) {
            factory.setPassword(properties.getPassword());
        }

        return factory;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory =
            new DefaultJmsListenerContainerFactory();
        factory
            .setConnectionFactory(jmsConnectionFactory(new AmqpProperties()));

        return factory;
    }

    @Bean
    public PublishQueue publishQueue() {
        return new PublishQueue();
    }

    @Bean
    public SubscribeQueue subscribeQueue() {
        return new SubscribeQueue();
    }
}
