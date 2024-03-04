package uk.nhs.adaptors.pss.translator.config;

import static org.springframework.jms.listener.DefaultMessageListenerContainer.CACHE_CONSUMER;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.JmsDestination;
import org.apache.qpid.jms.message.JmsMessageSupport;
import org.apache.qpid.jms.policy.JmsRedeliveryPolicy;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;

import io.micrometer.core.instrument.util.StringUtils;
import uk.nhs.adaptors.pss.translator.amqp.JmsListenerErrorHandler;

@Configuration
public class AmqpConfiguration {

    private static final long TEN_SECONDS = 10000L;

    private final JmsListenerErrorHandler listenerErrorHandler;

    public AmqpConfiguration(JmsListenerErrorHandler listenerErrorHandler) {
        this.listenerErrorHandler = listenerErrorHandler;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new MappingJackson2MessageConverter();
    }

    @Bean("pssQueueConnectionFactory")
    public JmsConnectionFactory jmsConnectionFactoryPssQueue(PssQueueProperties properties) {

        JmsConnectionFactory factory = getJmsConnectionFactory(properties);
        configureRedeliveryPolicy(properties, factory);
        return factory;
    }

    @Bean("mhsQueueConnectionFactory")
    public JmsConnectionFactory jmsConnectionFactoryMhsInboundQueue(MhsQueueProperties properties) {

        JmsConnectionFactory factory = getJmsConnectionFactory(properties);
        configureRedeliveryPolicy(properties, factory);
        return factory;
    }

    @Bean("gp2gpAdaptorQueueConnectionFactory")
    @ConditionalOnProperty(value = "amqp.daisyChaining", havingValue = "true")
    public JmsConnectionFactory jmsConnectionFactoryGp2GpAdaptorInboundQueue(Gp2GpAdaptorQueueProperties properties) {
        return getJmsConnectionFactory(properties);
    }

    @NotNull
    private static JmsConnectionFactory getJmsConnectionFactory(QueueProperties properties) {
        JmsConnectionFactory factory = new JmsConnectionFactory();

        factory.setRemoteURI(properties.getBroker());

        if (StringUtils.isNotBlank(properties.getUsername())) {
            factory.setUsername(properties.getUsername());
        }

        if (StringUtils.isNotBlank(properties.getPassword())) {
            factory.setPassword(properties.getPassword());
        }
        factory.setCloseTimeout(properties.getCloseTimeout());

        return factory;
    }

    @Bean("pssQueueJmsListenerFactory")
    public JmsListenerContainerFactory<?> jmsListenerContainerFactoryPssQueue(
        @Qualifier("pssQueueConnectionFactory") JmsConnectionFactory connectionFactory) {
        return getDefaultJmsListenerContainerFactory(connectionFactory);
    }

    @Bean("mhsQueueJmsListenerFactory")
    public JmsListenerContainerFactory<?> jmsListenerContainerFactoryMhsQueue(
        @Qualifier("mhsQueueConnectionFactory") JmsConnectionFactory connectionFactory) {
        return getDefaultJmsListenerContainerFactory(connectionFactory);
    }

    @NotNull
    private DefaultJmsListenerContainerFactory getDefaultJmsListenerContainerFactory(JmsConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setSessionTransacted(true);
        factory.setCacheLevel(CACHE_CONSUMER);
        factory.setConnectionFactory(connectionFactory);
        factory.setErrorHandler(listenerErrorHandler);
        factory.setRecoveryInterval(TEN_SECONDS);

        return factory;
    }

    @Bean("jmsTemplateMhsQueue")
    public JmsTemplate jmsTemplateMhsQueue(@Qualifier("mhsQueueConnectionFactory") JmsConnectionFactory connectionFactory,
        MhsQueueProperties properties) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setDefaultDestinationName(properties.getQueueName());
        return jmsTemplate;
    }

    @Bean("jmsTemplateMhsDLQ")
    public JmsTemplate jmsTemplateMhsDLQ(@Qualifier("mhsQueueConnectionFactory") JmsConnectionFactory connectionFactory,
        MhsQueueProperties properties) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setDefaultDestinationName(properties.getDLQName());
        return jmsTemplate;
    }

    @Bean("jmsTemplatePssQueue")
    public JmsTemplate jmsTemplateMhsQueue(@Qualifier("pssQueueConnectionFactory") JmsConnectionFactory connectionFactory,
        PssQueueProperties properties) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setDefaultDestinationName(properties.getQueueName());
        return jmsTemplate;
    }

    @Bean("jmsTemplateGp2GpAdaptorQueue")
    @ConditionalOnProperty(value = "amqp.daisyChaining", havingValue = "true")
    public JmsTemplate jmsTemplateGp2GpAdaptorQueue(@Qualifier("gp2gpAdaptorQueueConnectionFactory") JmsConnectionFactory connectionFactory,
        Gp2GpAdaptorQueueProperties properties) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setDefaultDestinationName(properties.getQueueName());
        return jmsTemplate;
    }

    private void configureRedeliveryPolicy(PssQueueProperties properties, JmsConnectionFactory factory) {
        factory.setRedeliveryPolicy(new CustomRedeliveryPolicy(
            properties.getMaxRedeliveries(), JmsMessageSupport.MODIFIED_FAILED_UNDELIVERABLE));
    }

    private void configureRedeliveryPolicy(MhsQueueProperties properties, JmsConnectionFactory factory) {
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
