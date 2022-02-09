package uk.nhs.adaptors.pss.gpc.util.fhir;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Parameters;

public class ParametersUtils {

    private static final String PATIENT_NHS_NUMBER_PARAM_NAME = "patientNHSNumber";

    public static Optional<Identifier> getNhsNumberFromParameters(Parameters parameters) {
        return parameters.getParameter()
            .stream()
            .filter(it -> PATIENT_NHS_NUMBER_PARAM_NAME.equals(it.getName()))
            .map(Parameters.ParametersParameterComponent::getValue)
            .map(Identifier.class::cast)
            .findFirst();
    }
}
