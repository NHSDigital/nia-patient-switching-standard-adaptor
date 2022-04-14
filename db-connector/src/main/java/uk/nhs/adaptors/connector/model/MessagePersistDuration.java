package uk.nhs.adaptors.connector.model;

import java.time.Duration;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MessagePersistDuration {
    private int id;
    private String messageType;
    private Duration persistDuration;
    private int callsSinceUpdate;
}
