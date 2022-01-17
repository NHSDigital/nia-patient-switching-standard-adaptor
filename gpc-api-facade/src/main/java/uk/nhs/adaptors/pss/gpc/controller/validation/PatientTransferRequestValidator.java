package uk.nhs.adaptors.pss.gpc.controller.validation;

import static java.util.Objects.nonNull;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.pss.gpc.exception.FhirValidationException;

@Component
@Slf4j
public class PatientTransferRequestValidator implements ConstraintValidator<PatientTransferRequest, Parameters> {
    private static final String NHS_NUMBER_PART_NAME = "patientNHSNumber";

    @Override
    public void initialize(PatientTransferRequest constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Parameters value, ConstraintValidatorContext context) {
        try {
            checkNhsNumber(value);
        } catch (FhirValidationException exception) {
            setErrorMessage(context, exception.getMessage());
            return false;
        }

        return true;
    }

    private static void checkNhsNumber(Parameters parameters) {
        parameters.getParameter()
            .stream()
            .filter(p -> NHS_NUMBER_PART_NAME.equals(p.getName()))
            .filter(p -> nonNull(p.getValue()))
            .reduce((x, y) -> {
                throw new FhirValidationException(String.format("Exactly 1 Parameter named '%s' with not empty value expected",
                    NHS_NUMBER_PART_NAME));
            })
            .map(p -> (Identifier) p.getValue())
            .filter(i -> StringUtils.isNotBlank(i.getValue()))
            .orElseThrow(() -> new FhirValidationException(String.format(
                "Missing value for Parameter '%s'", NHS_NUMBER_PART_NAME)));

    }

    private void setErrorMessage(ConstraintValidatorContext cxt, String message) {
        cxt.disableDefaultConstraintViolation();
        cxt.buildConstraintViolationWithTemplate(message)
            .addConstraintViolation();
    }
}
