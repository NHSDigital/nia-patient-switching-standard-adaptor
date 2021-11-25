package uk.nhs.adaptors.pss.translator.config;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;

import io.micrometer.core.instrument.util.StringUtils;

@Configuration
public class AmqpConfiguration {
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new MappingJackson2MessageConverter();
    }

    @Bean("pssQueueConnectionFactory")
    public JmsConnectionFactory jmsConnectionFactoryPssQueue(PssQueueProperties properties) {
        JmsConnectionFactory factory = new JmsConnectionFactory();

        factory.setRemoteURI(properties.getBroker());

        if (StringUtils.isEmpty(properties.getUsername())) {
            factory.setUsername(properties.getUsername());
        }

        if (StringUtils.isEmpty(properties.getPassword())) {
            factory.setPassword(properties.getPassword());
        }

        return factory;
    }

    @Bean("mhsQueueConnectionFactory")
    public JmsConnectionFactory jmsConnectionFactoryMhsInboundQueue(MhsQueueProperties properties) {
        JmsConnectionFactory factory = new JmsConnectionFactory();

        factory.setRemoteURI(properties.getBroker());

        if (StringUtils.isEmpty(properties.getUsername())) {
            factory.setUsername(properties.getUsername());
        }

        if (StringUtils.isEmpty(properties.getPassword())) {
            factory.setPassword(properties.getPassword());
        }

        return factory;
    }

    @Bean("pssQueueJmsListenerFactory")
    public JmsListenerContainerFactory<?> jmsListenerContainerFactoryPssQueue(
        @Qualifier("pssQueueConnectionFactory") JmsConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory =
            new DefaultJmsListenerContainerFactory();
        factory
            .setConnectionFactory(connectionFactory);

        return factory;
    }

    @Bean("mhsQueueJmsListenerFactory")
    public JmsListenerContainerFactory<?> jmsListenerContainerFactoryMhsQueue(
        @Qualifier("mhsQueueConnectionFactory") JmsConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory =
            new DefaultJmsListenerContainerFactory();
        factory
            .setConnectionFactory(connectionFactory);

        return factory;
    }
}
