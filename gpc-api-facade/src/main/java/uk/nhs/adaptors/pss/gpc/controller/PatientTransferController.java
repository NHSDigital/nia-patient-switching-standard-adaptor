package uk.nhs.adaptors.pss.gpc.controller;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import static uk.nhs.adaptors.connector.model.MigrationStatus.*;
import static uk.nhs.adaptors.pss.gpc.controller.handler.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.FROM_ASID;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.FROM_ODS;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.TO_ASID;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.TO_ODS;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.model.MigrationStatusLog;
import uk.nhs.adaptors.pss.gpc.controller.validation.PatientTransferRequest;
import uk.nhs.adaptors.pss.gpc.service.PatientTransferService;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Validated
public class PatientTransferController {
    private static final List<MigrationStatus> IN_PROGRESS_STATUSES = List.of(
        REQUEST_RECEIVED,
        EHR_EXTRACT_REQUEST_ACCEPTED,
        EHR_EXTRACT_RECEIVED,
        EHR_EXTRACT_REQUEST_ACKNOWLEDGED,
        EHR_EXTRACT_TRANSLATED,
        CONTINUE_REQUEST_ACCEPTED
    );

    private static final List<MigrationStatus> LRG_MESSAGE_ERRORS = List.of(
        ERROR_LRG_MSG_REASSEMBLY_FAILURE,
        ERROR_LRG_MSG_ATTACHMENTS_NOT_RECEIVED,
        ERROR_LRG_MSG_GENERAL_FAILURE,
        ERROR_LRG_MSG_TIMEOUT
    );

    private final PatientTransferService patientTransferService;

    @PostMapping(
        path = "/Patient/$gpc.migratestructuredrecord",
        consumes = {APPLICATION_FHIR_JSON_VALUE},
        produces = {APPLICATION_FHIR_JSON_VALUE}
    )
    public ResponseEntity<String> migratePatientStructuredRecord(
        @RequestBody @PatientTransferRequest Parameters body,
        @RequestHeader(TO_ASID) @NotNull String toAsid,
        @RequestHeader(FROM_ASID) @NotNull String fromAsid,
        @RequestHeader(TO_ODS) @NotNull String toOds,
        @RequestHeader(FROM_ODS) @NotNull String fromOds) {
        LOGGER.info("Received patient transfer request");
        Map<String, String> headers = Map.of(
            TO_ASID, toAsid,
            FROM_ASID, fromAsid,
            TO_ODS, toOds,
            FROM_ODS, fromOds
        );

        MigrationStatusLog request = patientTransferService.handlePatientMigrationRequest(body, headers);
        if (request == null) {
            return new ResponseEntity<>(ACCEPTED);
        } else if (IN_PROGRESS_STATUSES.contains(request.getMigrationStatus())) {
            return new ResponseEntity<>(NO_CONTENT);
        } else if (MIGRATION_COMPLETED == request.getMigrationStatus()) {
            return new ResponseEntity<>(patientTransferService.getEmptyBundle(), OK);
        } else if (LRG_MESSAGE_ERRORS.contains(request.getMigrationStatus())) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            throw new IllegalStateException("Unsupported transfer status: " + request.getMigrationStatus());
        }
    }
}
