package uk.nhs.adaptors.pss.gpc.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class PatientTransferController {
    public static final String APPLICATION_FHIR_JSON_VALUE = "application/fhir+json";

    @PostMapping(
        path = "/Patient/$gpc.migratestructuredrecord",
        consumes = {APPLICATION_FHIR_JSON_VALUE},
        produces = {APPLICATION_FHIR_JSON_VALUE})
    public String migratePatientStructuredRecord(@RequestBody String body) {

    }
}
