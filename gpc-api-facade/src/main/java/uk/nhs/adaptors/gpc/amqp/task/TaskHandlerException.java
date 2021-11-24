package uk.nhs.adaptors.gpc.amqp.task;

public class TaskHandlerException extends RuntimeException {
    public TaskHandlerException(String message) {
        super(message);
    }

    public TaskHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
