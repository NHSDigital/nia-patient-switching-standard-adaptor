package uk.nhs.adaptors.pss.gpc.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.nhs.adaptors.common.enums.MigrationStatus;
import uk.nhs.adaptors.common.util.CodeableConceptUtils;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.model.MigrationStatusLog;
import uk.nhs.adaptors.pss.gpc.controller.validation.PatientTransferRequest;
import uk.nhs.adaptors.pss.gpc.service.PatientTransferService;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.Map;

import static org.hl7.fhir.dstu3.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.dstu3.model.OperationOutcome.IssueType.EXCEPTION;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_GENERAL_PROCESSING_ERROR;
import static uk.nhs.adaptors.common.enums.MigrationStatus.FINAL_ACK_SENT;
import static uk.nhs.adaptors.common.enums.MigrationStatus.MIGRATION_COMPLETED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.ERROR_REQUEST_TIMEOUT;
import static uk.nhs.adaptors.common.model.MigrationStatusGroups.GPG2PG_NACK_400_ERROR_STATUSES;
import static uk.nhs.adaptors.common.model.MigrationStatusGroups.GPG2PG_NACK_404_ERROR_STATUSES;
import static uk.nhs.adaptors.common.model.MigrationStatusGroups.GPG2PG_NACK_500_ERROR_STATUSES;
import static uk.nhs.adaptors.common.model.MigrationStatusGroups.GPG2PG_NACK_501_ERROR_STATUSES;
import static uk.nhs.adaptors.common.model.MigrationStatusGroups.IN_PROGRESS_STATUSES;
import static uk.nhs.adaptors.common.model.MigrationStatusGroups.LRG_MESSAGE_ERRORS;
import static uk.nhs.adaptors.pss.gpc.controller.handler.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.FROM_ASID;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.FROM_ODS;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.TO_ASID;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.TO_ODS;
import static uk.nhs.adaptors.pss.gpc.util.fhir.OperationOutcomeUtils.createOperationOutcome;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Validated
public class PatientTransferController {

    private static final String ISSUE_SYSTEM = "https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1";
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

        var existingConversationId = patientTransferService.checkExistingPatientMigrationRequestInProgress(body);
        if (existingConversationId != null) {
            var operationOutcome = createErrorBodyForInProgressRequest(existingConversationId);
            var errorBody =  fhirParser.encodeToJson(operationOutcome);
            return new ResponseEntity<>(errorBody, HttpStatus.INTERNAL_SERVER_ERROR);
        }

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

    private OperationOutcome createErrorBodyForInProgressRequest(String conversationId) {
        var operationErrorCode = "INTERNAL_SERVER_ERROR";
        var operationErrorMessage = "PS - The Given NHS number is already being processed against Conversation ID: "
            + conversationId + ", you cannot start a new request until the current request has completed or failed.";

        CodeableConcept details = CodeableConceptUtils.
                createCodeableConcept(operationErrorCode, ISSUE_SYSTEM, operationErrorMessage, null);
        return createOperationOutcome(EXCEPTION, ERROR, details, "");
    }
    private OperationOutcome createErrorBodyFromMigrationStatus(MigrationStatus migrationStatus) throws IOException {

        String operationErrorCode;
        String operationErrorMessage;

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
        return createOperationOutcome(EXCEPTION, ERROR, details, "");
    }


}
