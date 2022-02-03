package uk.nhs.adaptors.pss.translator.mapper;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.v3.RCMRMT030101UK04Patient;

public class PatientMapper {

    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String PATIENT_NHS_NUMBER_ID = "2.16.840.1.113883.2.1.4.1";

    private static final String NHS_NUMBER_SYSTEM_URL = "https://fhir.nhs.uk/Id/nhs-number";


    public Patient mapToPatient(RCMRMT030101UK04Patient patient) {
        var identifier = getIdentifier(patient.getId().getRoot());
        var extension = patient.getId().getExtension();
        return createPatient(identifier, extension);
    }


    private Identifier getIdentifier(String id) {
        return new Identifier()
            .setSystem(IDENTIFIER_SYSTEM)
            .setValue(id);
    }


    private Patient createPatient(Identifier i, String extension) {
        return new Patient()
            .addIdentifier(i);

    }

}
