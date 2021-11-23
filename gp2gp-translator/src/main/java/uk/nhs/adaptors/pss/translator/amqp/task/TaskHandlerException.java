package uk.nhs.adaptors.pss.translator.amqp.task;

public class TaskHandlerException extends RuntimeException {
    public TaskHandlerException(String message) {
        super(message);
    }

    public TaskHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
