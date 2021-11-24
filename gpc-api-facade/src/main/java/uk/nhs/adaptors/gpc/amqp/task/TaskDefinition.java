package uk.nhs.adaptors.gpc.amqp.task;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@EqualsAndHashCode
public abstract class TaskDefinition {
    private final String taskName;
}
