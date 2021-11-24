package uk.nhs.adaptors.pss.translator.amqp.task;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@SuperBuilder
@Getter
public class TaskDefinition {
    private final String taskName;
}
