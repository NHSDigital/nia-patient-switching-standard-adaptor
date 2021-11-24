package uk.nhs.adaptors.gpc.config;

import static org.springframework.util.StringUtils.isEmpty;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.JmsDestination;
import org.apache.qpid.jms.message.JmsMessageSupport;
import org.apache.qpid.jms.policy.JmsRedeliveryPolicy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;

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

        if (!isEmpty(properties.getUsername())) {
            factory.setUsername(properties.getUsername());
        }

        if (!isEmpty(properties.getPassword())) {
            factory.setPassword(properties.getPassword());
        }

        configureRedeliveryPolicy(properties, factory);

        return factory;
    }

    @Bean("pssQueueJmsListenerFactory")
    public JmsListenerContainerFactory<?> jmsListenerContainerFactoryPssQueue(@Qualifier("pssQueueConnectionFactory") JmsConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory =
            new DefaultJmsListenerContainerFactory();
        factory
            .setConnectionFactory(connectionFactory);

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

    private void configureRedeliveryPolicy(PssQueueProperties properties, JmsConnectionFactory factory) {
        factory.setRedeliveryPolicy(new CustomRedeliveryPolicy(
            properties.getMaxRedeliveries(), JmsMessageSupport.MODIFIED_FAILED_UNDELIVERABLE));
    }

    static final class CustomRedeliveryPolicy implements JmsRedeliveryPolicy {
        private final int maxRedeliveries;
        private final int outcome;

        private CustomRedeliveryPolicy(int maxRedeliveries, int outcome) {
            this.maxRedeliveries = maxRedeliveries;
            this.outcome = outcome;
        }

        @Override
        public JmsRedeliveryPolicy copy() {
            return new CustomRedeliveryPolicy(this.maxRedeliveries, this.outcome);
        }

        @Override
        public int getMaxRedeliveries(JmsDestination destination) {
            return this.maxRedeliveries;
        }

        @Override
        public int getOutcome(JmsDestination destination) {
            return this.outcome;
        }
    }
}
