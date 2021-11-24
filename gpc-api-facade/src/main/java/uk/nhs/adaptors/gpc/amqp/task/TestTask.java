package uk.nhs.adaptors.gpc.amqp.task;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@SuperBuilder
@Getter
@EqualsAndHashCode(callSuper = true)
public class TestTask extends TaskDefinition {
}
