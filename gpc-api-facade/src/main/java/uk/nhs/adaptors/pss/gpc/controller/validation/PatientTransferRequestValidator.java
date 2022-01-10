package uk.nhs.adaptors.pss.gpc.controller.validation;

import static java.util.Objects.nonNull;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.pss.gpc.exception.FhirValidationException;
import uk.nhs.adaptors.pss.gpc.service.FhirParser;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PatientTransferRequestValidator implements ConstraintValidator<PatientTransferRequest, String> {
    private static final String NHS_NUMBER_PART_NAME = "patientNHSNumber";

    private final FhirParser fhirParser;

    @Override
    public void initialize(PatientTransferRequest constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            Parameters parameters = fhirParser.parseResource(value, Parameters.class);
            checkNhsNumber(parameters);
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
