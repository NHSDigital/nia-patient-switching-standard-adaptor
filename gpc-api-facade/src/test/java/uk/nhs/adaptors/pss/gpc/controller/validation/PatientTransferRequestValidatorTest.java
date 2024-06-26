package uk.nhs.adaptors.pss.gpc.controller.validation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Parameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PatientTransferRequestValidatorTest {
    private static final String NHS_NUMBER = "123456789";

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;

    @InjectMocks
    private PatientTransferRequestValidator validator;

    @Test
    public void isValidShouldReturnTrueForValidParameters() {
        var validParameters = createValidParametersResource(NHS_NUMBER);

        assertThat(validator.isValid(validParameters, constraintValidatorContext)).isTrue();
    }

    @Test
    public void isValidShouldReturnFalseWhenNhsNumberComponentIsMissing() {
        var invalidParameters = createParametersResourceWithoutNhsNumberComponent();
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(constraintViolationBuilder);

        assertThat(validator.isValid(invalidParameters, constraintValidatorContext)).isFalse();
    }

    @Test
    public void isValidShouldReturnFalseWhenValueIdentifierIsMissing() {
        var invalidParameters = createParametersResourceWithoutNhsNumberComponent();
        Parameters.ParametersParameterComponent nhsNumberComponent = new Parameters.ParametersParameterComponent();
        nhsNumberComponent.setName("patientNHSNumber");
        invalidParameters.addParameter(nhsNumberComponent);
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(constraintViolationBuilder);

        assertThat(validator.isValid(invalidParameters, constraintValidatorContext)).isFalse();
    }

    @Test
    public void isValidShouldReturnFalseWhenNhsNumberComponentIsAddedTwice() {
        var invalidParameters = createValidParametersResource(NHS_NUMBER);
        invalidParameters.addParameter(createNhsNumberComponent("987654321"));
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(constraintViolationBuilder);

        assertThat(validator.isValid(invalidParameters, constraintValidatorContext)).isFalse();
    }

    @Test
    public void isValidShouldReturnFalseWhenValueIdentifierValueIsNull() {
        var invalidParameters = createValidParametersResource(null);
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(constraintViolationBuilder);

        assertThat(validator.isValid(invalidParameters, constraintValidatorContext)).isFalse();
    }

    @Test
    public void isValidShouldReturnFalseWhenValueIdentifierValueIsEmpty() {
        var invalidParameters = createValidParametersResource("");
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(constraintViolationBuilder);

        assertThat(validator.isValid(invalidParameters, constraintValidatorContext)).isFalse();
    }

    private Parameters createValidParametersResource(String nhsNumberValue) {
        Parameters parameters = new Parameters();

        parameters
            .addParameter(createNhsNumberComponent(nhsNumberValue))
            .addParameter(createFullRecordComponent());

        return parameters;
    }

    private Parameters createParametersResourceWithoutNhsNumberComponent() {
        Parameters parameters = new Parameters();
        parameters.addParameter(createFullRecordComponent());

        return parameters;
    }

    private Parameters.ParametersParameterComponent createNhsNumberComponent(String nhsNumberValue) {
        Parameters.ParametersParameterComponent nhsNumberComponent = new Parameters.ParametersParameterComponent();
        nhsNumberComponent.setName("patientNHSNumber");
        Identifier identifier = new Identifier();
        identifier
            .setSystem("https://fhir.nhs.uk/Id/nhs-number")
            .setValue(nhsNumberValue);
        nhsNumberComponent.setValue(identifier);

        return nhsNumberComponent;
    }

    private Parameters.ParametersParameterComponent createFullRecordComponent() {
        BooleanType booleanType = new BooleanType();
        booleanType.setValue(true);
        Parameters.ParametersParameterComponent sensitiveInformationPart = new Parameters.ParametersParameterComponent();
        sensitiveInformationPart
            .setName("includeSensitiveInformation")
            .setValue(booleanType);
        Parameters.ParametersParameterComponent fullRecordComponent = new Parameters.ParametersParameterComponent();
        fullRecordComponent
            .setName("includeFullRecord")
            .addPart(sensitiveInformationPart);

        return fullRecordComponent;
    }
}
