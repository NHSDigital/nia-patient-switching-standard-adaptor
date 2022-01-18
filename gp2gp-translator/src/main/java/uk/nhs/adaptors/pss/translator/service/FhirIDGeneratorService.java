package uk.nhs.adaptors.pss.translator.service;

import java.util.UUID;

public class FhirIDGeneratorService {
    private static final String FHIR_ID = UUID.randomUUID().toString().toUpperCase();

    public String getFHIR_ID() {
        return FHIR_ID;
    }

}
