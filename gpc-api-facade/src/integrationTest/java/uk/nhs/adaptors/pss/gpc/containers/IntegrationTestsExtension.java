package uk.nhs.adaptors.pss.gpc.containers;

import static org.springframework.jms.support.destination.JmsDestinationAccessor.RECEIVE_TIMEOUT_NO_WAIT;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IntegrationTestsExtension implements BeforeAllCallback, BeforeEachCallback {

    private static final String DLQ_PREFIX = "DLQ.";

    @Override
    public void beforeAll(ExtensionContext context) {
        ActiveMqContainer.getInstance().start();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        var applicationContext = SpringExtension.getApplicationContext(context);

        var jmsTemplate = applicationContext.getBean(JmsTemplate.class);

        var queueName = Objects.requireNonNull(
            applicationContext.getEnvironment().getProperty("amqp.pss.queueName"));

        var receiveTimeout = jmsTemplate.getReceiveTimeout();
        jmsTemplate.setReceiveTimeout(RECEIVE_TIMEOUT_NO_WAIT);
        List.of(queueName, DLQ_PREFIX + queueName)
            .forEach(name -> {
                while (jmsTemplate.receive(name) != null) {
                    LOGGER.info("Purged '" + name + "' message");
                }
            });
        jmsTemplate.setReceiveTimeout(receiveTimeout);
    }
}
