package uk.nhs.adaptors.amqp.utils.task;

import lombok.Getter;

public enum TaskType {
    TEST_TASK(TestTask.class);

    @Getter
    private final Class<? extends TaskDefinition> classOfTaskDefinition;
    @Getter
    private final String taskName;

    TaskType(Class<? extends TaskDefinition> classOfTaskDefinition) {
        this.classOfTaskDefinition = classOfTaskDefinition;
        this.taskName = classOfTaskDefinition.getName();
    }
}
