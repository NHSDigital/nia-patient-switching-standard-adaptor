package uk.nhs.adaptors.pss.translator.testutil;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Parameters;

public class CreateParametersUtil {

    public static Parameters createValidParametersResource(String nhsNumberValue) {
        Parameters parameters = new Parameters();

        parameters
            .addParameter(createNhsNumberComponent(nhsNumberValue))
            .addParameter(createFullRecordComponent());

        return parameters;
    }

    private static Parameters.ParametersParameterComponent createFullRecordComponent() {
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

    private static Parameters.ParametersParameterComponent createNhsNumberComponent(String nhsNumberValue) {
        Parameters.ParametersParameterComponent nhsNumberComponent = new Parameters.ParametersParameterComponent();
        nhsNumberComponent.setName("patientNHSNumber");
        Identifier identifier = new Identifier();
        identifier
            .setSystem("https://fhir.nhs.uk/Id/nhs-number")
            .setValue(nhsNumberValue);
        nhsNumberComponent.setValue(identifier);

        return nhsNumberComponent;
    }

}
