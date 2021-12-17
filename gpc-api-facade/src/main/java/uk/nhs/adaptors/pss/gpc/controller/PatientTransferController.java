package uk.nhs.adaptors.pss.gpc.controller;

import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.pss.gpc.model.TransferStatus;
import uk.nhs.adaptors.pss.gpc.service.FhirParser;
import uk.nhs.adaptors.pss.gpc.service.PatientTransferService;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class PatientTransferController {
    private static final String APPLICATION_FHIR_JSON_VALUE = "application/fhir+json";

    private final PatientTransferService patientTransferService;
    private final FhirParser fhirParser;

    @PostMapping(
        path = "/Patient/$gpc.migratestructuredrecord",
        consumes = {APPLICATION_FHIR_JSON_VALUE},
        produces = {APPLICATION_FHIR_JSON_VALUE})
    public ResponseEntity<String> migratePatientStructuredRecord(@RequestBody String body) {
        LOGGER.info("Received patient transfer request");

        Parameters parameters = fhirParser.parseResource(body, Parameters.class);
        TransferStatus transferStatus = patientTransferService.handlePatientMigrationRequest(parameters);
        if (transferStatus == TransferStatus.NEW) {
            String responseBody = patientTransferService.getEmptyBundle();
            return new ResponseEntity<>(responseBody, HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
}
