package uk.nhs.adaptors.pss.gpc.controller.validation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;

@Constraint(validatedBy = PatientTransferRequestValidator.class)
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface PatientTransferRequest {
}
