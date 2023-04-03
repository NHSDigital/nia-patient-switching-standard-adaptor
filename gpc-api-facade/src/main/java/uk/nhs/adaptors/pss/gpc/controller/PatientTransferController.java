package uk.nhs.adaptors.pss.gpc.controller;

import static org.hl7.fhir.dstu3.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.dstu3.model.OperationOutcome.IssueType.EXCEPTION;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import static uk.nhs.adaptors.connector.model.MigrationStatus.CONTINUE_REQUEST_ACCEPTED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.COPC_ACKNOWLEDGED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.COPC_FAILED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.COPC_MESSAGE_PROCESSING;
import static uk.nhs.adaptors.connector.model.MigrationStatus.COPC_MESSAGE_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_PROCESSING;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_ACCEPTED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_ACKNOWLEDGED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_EHR_GENERATION_ERROR;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_MISFORMED_REQUEST;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_MULTI_OR_NO_RESPONSES;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_NOT_PRIMARY_HEALTHCARE_PROVIDER;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_PATIENT_NOT_REGISTERED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_SENDER_NOT_CONFIGURED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK_UNKNOWN;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_GENERAL_PROCESSING_ERROR;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_LRG_MSG_ATTACHMENTS_NOT_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_LRG_MSG_GENERAL_FAILURE;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_LRG_MSG_REASSEMBLY_FAILURE;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_LRG_MSG_TIMEOUT;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_REQUEST_TIMEOUT;
import static uk.nhs.adaptors.connector.model.MigrationStatus.FINAL_ACK_SENT;
import static uk.nhs.adaptors.connector.model.MigrationStatus.MIGRATION_COMPLETED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.REQUEST_RECEIVED;
import static uk.nhs.adaptors.pss.gpc.controller.handler.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.FROM_ASID;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.FROM_ODS;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.TO_ASID;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.TO_ODS;
import static uk.nhs.adaptors.pss.gpc.util.fhir.OperationOutcomeUtils.createOperationOutcome;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.OperationOutcome;
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
import uk.nhs.adaptors.common.util.CodeableConceptUtils;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.model.MigrationStatusLog;
import uk.nhs.adaptors.pss.gpc.controller.validation.PatientTransferRequest;
import uk.nhs.adaptors.pss.gpc.service.PatientTransferService;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Validated
public class PatientTransferController {

    private static final String ISSUE_SYSTEM = "https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1";

    private static final List<MigrationStatus> IN_PROGRESS_STATUSES = List.of(
        REQUEST_RECEIVED,
        EHR_EXTRACT_REQUEST_ACCEPTED,
        EHR_EXTRACT_RECEIVED,
        EHR_EXTRACT_PROCESSING,
        EHR_EXTRACT_REQUEST_ACKNOWLEDGED,
        EHR_EXTRACT_TRANSLATED,
        CONTINUE_REQUEST_ACCEPTED,
        COPC_MESSAGE_RECEIVED,
        COPC_MESSAGE_PROCESSING,
        COPC_ACKNOWLEDGED,
        COPC_FAILED
    );

    private static final List<MigrationStatus> GPG2PG_NACK_400_ERROR_STATUSES = List.of(
        EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_MISFORMED_REQUEST
    );

    private static final List<MigrationStatus> GPG2PG_NACK_404_ERROR_STATUSES = List.of(
        EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_PATIENT_NOT_REGISTERED,
        EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_NOT_PRIMARY_HEALTHCARE_PROVIDER
    );

    private static final List<MigrationStatus> GPG2PG_NACK_500_ERROR_STATUSES = List.of(
        EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_EHR_GENERATION_ERROR,
        EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_MULTI_OR_NO_RESPONSES,
        EHR_EXTRACT_REQUEST_NEGATIVE_ACK_UNKNOWN
    );

    private static final List<MigrationStatus> GPG2PG_NACK_501_ERROR_STATUSES = List.of(
        EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_SENDER_NOT_CONFIGURED
    );

    private static final List<MigrationStatus> LRG_MESSAGE_ERRORS = List.of(
        ERROR_LRG_MSG_REASSEMBLY_FAILURE,
        ERROR_LRG_MSG_ATTACHMENTS_NOT_RECEIVED,
        ERROR_LRG_MSG_GENERAL_FAILURE,
        ERROR_LRG_MSG_TIMEOUT
    );

    private final PatientTransferService patientTransferService;
    private final FhirParser fhirParser;

    @PostMapping(
        path = "/Patient/$gpc.migratestructuredrecord",
        consumes = {APPLICATION_FHIR_JSON_VALUE},
        produces = {APPLICATION_FHIR_JSON_VALUE}
    )
    public ResponseEntity<String> migratePatientStructuredRecord(
        @RequestBody @PatientTransferRequest Parameters body,
        @RequestHeader(TO_ASID) @NotBlank String toAsid,
        @RequestHeader(FROM_ASID) @NotBlank String fromAsid,
        @RequestHeader(TO_ODS) @NotBlank String toOds,
        @RequestHeader(FROM_ODS) @NotBlank String fromOds) throws IOException {
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
        } else if (MIGRATION_COMPLETED == request.getMigrationStatus()
            || FINAL_ACK_SENT == request.getMigrationStatus()) {
            return new ResponseEntity<>(patientTransferService.getBundleResource(), OK);
        } else {

            OperationOutcome operationOutcome = createErrorBodyFromMigrationStatus(request.getMigrationStatus());
            String errorBody = fhirParser.encodeToJson(operationOutcome);
            MigrationStatus currentMigrationStatus = request.getMigrationStatus();

            // This is where we handle errors
            if (GPG2PG_NACK_500_ERROR_STATUSES.contains(currentMigrationStatus)
                || LRG_MESSAGE_ERRORS.contains(currentMigrationStatus)
                || EHR_GENERAL_PROCESSING_ERROR == currentMigrationStatus
                || EHR_EXTRACT_REQUEST_NEGATIVE_ACK == currentMigrationStatus
                || ERROR_REQUEST_TIMEOUT == currentMigrationStatus) {
                return new ResponseEntity<>(errorBody, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (GPG2PG_NACK_400_ERROR_STATUSES.contains(currentMigrationStatus)) {
                return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
            }

            if (GPG2PG_NACK_404_ERROR_STATUSES.contains(currentMigrationStatus)) {
                return new ResponseEntity<>(errorBody, HttpStatus.NOT_FOUND);
            }


            if (GPG2PG_NACK_501_ERROR_STATUSES.contains(currentMigrationStatus)) {
                return new ResponseEntity<>(errorBody, HttpStatus.NOT_IMPLEMENTED);
            }

            throw new IllegalStateException("Unsupported transfer status: " + currentMigrationStatus);
        }
    }

    private OperationOutcome createErrorBodyFromMigrationStatus(MigrationStatus migrationStatus) {

        String operationErrorCode = "";
        String operationErrorMessage = "";

        switch (migrationStatus) {
            case EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_MISFORMED_REQUEST:
                operationErrorCode = "BAD_REQUEST";
                operationErrorMessage = "GP2GP - Request message not well-formed or not able to be processed";
                break;
            case EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_PATIENT_NOT_REGISTERED:
                operationErrorCode = "PATIENT_NOT_FOUND";
                operationErrorMessage = "GP2GP - Patient is not registered at the practice";
                break;
            case EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_NOT_PRIMARY_HEALTHCARE_PROVIDER:
                operationErrorCode = "PATIENT_NOT_FOUND";
                operationErrorMessage = "GP2GP - PDS indicates Requesting practice is "
                    + "not the patient’s current primary healthcare provider";
                break;
            case EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_EHR_GENERATION_ERROR:
                operationErrorCode = "INTERNAL_SERVER_ERROR";
                operationErrorMessage = "GP2GP - Failed to successfully generate the EHR";
                break;
            case EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_MULTI_OR_NO_RESPONSES:
                operationErrorCode = "INTERNAL_SERVER_ERROR";
                operationErrorMessage = "GP2GP - SDS lookup provided zero or more than one result to the query for each interaction.";
                break;
            case EHR_EXTRACT_REQUEST_NEGATIVE_ACK_UNKNOWN:
                operationErrorCode = "INTERNAL_SERVER_ERROR";
                operationErrorMessage = "GP2GP - This is a code that should only be used in circumstances where no other codes can"
                    + " be used to accurately describe the condition.";
                break;
            case EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_SENDER_NOT_CONFIGURED:
                operationErrorCode = "NOT_IMPLEMENTED";
                operationErrorMessage = "GP2GP - End Point setup but GP2GP configuration switched OFF";
                break;
            case ERROR_LRG_MSG_REASSEMBLY_FAILURE:
                operationErrorCode = "INTERNAL_SERVER_ERROR";
                operationErrorMessage = "PS - The Adaptor was unable to recombine the parts of a received attachment";
                break;
            case ERROR_LRG_MSG_ATTACHMENTS_NOT_RECEIVED:
                operationErrorCode = "INTERNAL_SERVER_ERROR";
                operationErrorMessage = "PS - At least one attachment has not be received";
                break;
            case ERROR_LRG_MSG_GENERAL_FAILURE:
                operationErrorCode = "INTERNAL_SERVER_ERROR";
                operationErrorMessage = "PS - A general processing error has occurred";
                break;
            case ERROR_LRG_MSG_TIMEOUT:
                operationErrorCode = "INTERNAL_SERVER_ERROR";
                operationErrorMessage = "PS - An attachment was not received before a timeout condition occurred";
                break;
            case ERROR_REQUEST_TIMEOUT:
                operationErrorCode = "INTERNAL_SERVER_ERROR";
                operationErrorMessage = "PS - The EHR record was not received within the given timeout timeframe";
                break;
            default:
                operationErrorCode = "INTERNAL_SERVER_ERROR";
                operationErrorMessage = "PS - A general error has occurred";
                break;
        }

        CodeableConcept details = CodeableConceptUtils.
                createCodeableConcept(operationErrorCode, ISSUE_SYSTEM, operationErrorMessage, null);
        OperationOutcome operationOutcome = createOperationOutcome(EXCEPTION, ERROR, details, "");
        return operationOutcome;
    }


}
