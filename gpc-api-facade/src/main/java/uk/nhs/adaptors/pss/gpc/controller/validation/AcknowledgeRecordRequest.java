package uk.nhs.adaptors.pss.gpc.controller.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = AcknowledgeRecordRequestValidator.class)
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface AcknowledgeRecordRequest {
    String message() default "Invalid Acknowledge Record request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}