package uk.nhs.adaptors.pss.translator.config;

import javax.jms.ConnectionFactory;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.JmsDestination;
import org.apache.qpid.jms.message.JmsMessageSupport;
import org.apache.qpid.jms.policy.JmsRedeliveryPolicy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;

import io.micrometer.core.instrument.util.StringUtils;

@Configuration
@ConditionalOnMissingBean(ConnectionFactory.class)
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

        configureRedeliveryPolicy(properties, factory);

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
        factory.setConnectionFactory(connectionFactory);

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
