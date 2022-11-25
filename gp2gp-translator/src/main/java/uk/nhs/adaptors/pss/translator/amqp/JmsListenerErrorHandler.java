package uk.nhs.adaptors.pss.translator.amqp;

import java.util.Map;

import org.jdbi.v3.core.ConnectionException;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JmsListenerErrorHandler implements ErrorHandler {

    private static final Map<Class<? extends RuntimeException>, String> RETRYABLE_EXCEPTION_MESSAGES = Map.of(
        ConnectionException.class, "Unable to connect to database"
    );

    @Override
    public void handleError(Throwable t) {

        Throwable cause = t.getCause();
        Class<? extends Throwable> classOfCause = cause.getClass();

        LOGGER.warn("Caught Error cause of type: [{}], with message: [{}]", classOfCause.toString(), cause.getMessage());

        if (RETRYABLE_EXCEPTION_MESSAGES.containsKey(classOfCause)) {
            throw new RuntimeException(RETRYABLE_EXCEPTION_MESSAGES.get(classOfCause));
        }
    }
}
