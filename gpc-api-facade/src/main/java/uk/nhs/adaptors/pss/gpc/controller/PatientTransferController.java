package uk.nhs.adaptors.pss.gpc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.pss.gpc.service.MigratePatientStructuredRecordService;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class PatientTransferController {
    public static final String APPLICATION_FHIR_JSON_VALUE = "application/fhir+json";

    private final MigratePatientStructuredRecordService migratePatientStructuredRecordService;

    @PostMapping(
        path = "/Patient/$gpc.migratestructuredrecord",
        consumes = {APPLICATION_FHIR_JSON_VALUE},
        produces = {APPLICATION_FHIR_JSON_VALUE})
    public ResponseEntity<String> migratePatientStructuredRecord(@RequestBody String body) {
        LOGGER.info("Received patient transfer request");
        return migratePatientStructuredRecordService.handlePatientMigrationRequest(body);
    }
}
