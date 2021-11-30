package uk.nhs.adaptors.pss.gpc.config;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
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

        if (StringUtils.isNotBlank(properties.getUsername())) {
            factory.setUsername(properties.getUsername());
        }

        if (StringUtils.isNotBlank(properties.getPassword())) {
            factory.setPassword(properties.getPassword());
        }

        return factory;
    }

    @Bean("jmsTemplatePssQueue")
    public JmsTemplate jmsTemplatePssQueue(@Qualifier("pssQueueConnectionFactory") JmsConnectionFactory connectionFactory,
        PssQueueProperties properties) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setDefaultDestinationName(properties.getQueueName());
        return jmsTemplate;
    }
}
