package uk.nhs.adaptors.pss.gpc.controller;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import static uk.nhs.adaptors.connector.model.RequestStatus.COMPLETED;
import static uk.nhs.adaptors.connector.model.RequestStatus.IN_PROGRESS;
import static uk.nhs.adaptors.connector.model.RequestStatus.RECEIVED;
import static uk.nhs.adaptors.pss.gpc.controller.handler.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

import java.util.List;

import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.model.RequestStatus;
import uk.nhs.adaptors.pss.gpc.controller.validation.PatientTransferRequest;
import uk.nhs.adaptors.pss.gpc.service.FhirParser;
import uk.nhs.adaptors.pss.gpc.service.PatientTransferService;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class PatientTransferController {
    private static final List<RequestStatus> IN_PROGRESS_STATUSES = List.of(RECEIVED, IN_PROGRESS);

    private final PatientTransferService patientTransferService;
    private final FhirParser fhirParser;

    @PostMapping(
        path = "/Patient/$gpc.migratestructuredrecord",
        consumes = {APPLICATION_FHIR_JSON_VALUE},
        produces = {APPLICATION_FHIR_JSON_VALUE}
    )
    public ResponseEntity<String> migratePatientStructuredRecord(@RequestBody @PatientTransferRequest String body) {
        LOGGER.info("Received patient transfer request");

        Parameters parameters = fhirParser.parseResource(body, Parameters.class);
        PatientMigrationRequest request = patientTransferService.handlePatientMigrationRequest(parameters);
        if (request == null) {
            return new ResponseEntity<>(ACCEPTED);
        } else if (IN_PROGRESS_STATUSES.contains(request.getRequestStatus())) {
            return new ResponseEntity<>(NO_CONTENT);
        } else if (COMPLETED == request.getRequestStatus()) {
            return new ResponseEntity<>(patientTransferService.getEmptyBundle(), OK);
        } else {
            throw new IllegalStateException("Unsupported transfer status: " + request.getRequestStatus());
        }
    }
}
