package uk.nhs.adaptors.pss.gpc.controller.validation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = PatientTransferRequestValidator.class)
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface PatientTransferRequest {
    String message() default "Invalid Patient Transfer request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
