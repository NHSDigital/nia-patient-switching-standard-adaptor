package uk.nhs.adaptors.pss.translator.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.connector.model.MigrationStatus;

import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_GENERAL_PROCESSING_ERROR;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_LRG_MSG_ATTACHMENTS_NOT_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_LRG_MSG_GENERAL_FAILURE;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_LRG_MSG_REASSEMBLY_FAILURE;
import static uk.nhs.adaptors.connector.model.MigrationStatus.ERROR_LRG_MSG_TIMEOUT;

@RequiredArgsConstructor
public enum NACKReason {
    LARGE_MESSAGE_REASSEMBLY_FAILURE("29"),
    LARGE_MESSAGE_ATTACHMENTS_NOT_RECEIVED("31"),
    LARGE_MESSAGE_GENERAL_FAILURE("30"),
    LARGE_MESSAGE_TIMEOUT("25"),
    CLINICAL_SYSTEM_INTEGRATION_FAILURE("11"),
    EHR_EXTRACT_CANNOT_BE_PROCESSED("21"),
    UNEXPECTED_CONDITION("99"),
    ABA_EHR_EXTRACT_REJECTED_WRONG_PATIENT("17"),
    ABA_EHR_EXTRACT_SUPPRESSED("15"),
    NON_ABA_EHR_EXTRACT_REJECTED_WRONG_PATIENT("28");



    @Getter
    private final String code;

    public MigrationStatus getMigrationStatus() {
        return switch (this) {
            case LARGE_MESSAGE_ATTACHMENTS_NOT_RECEIVED -> ERROR_LRG_MSG_ATTACHMENTS_NOT_RECEIVED;
            case LARGE_MESSAGE_GENERAL_FAILURE -> ERROR_LRG_MSG_GENERAL_FAILURE;
            case LARGE_MESSAGE_REASSEMBLY_FAILURE -> ERROR_LRG_MSG_REASSEMBLY_FAILURE;
            case LARGE_MESSAGE_TIMEOUT -> ERROR_LRG_MSG_TIMEOUT;
            case UNEXPECTED_CONDITION, EHR_EXTRACT_CANNOT_BE_PROCESSED -> EHR_GENERAL_PROCESSING_ERROR;
            case CLINICAL_SYSTEM_INTEGRATION_FAILURE, ABA_EHR_EXTRACT_SUPPRESSED  -> EHR_EXTRACT_REQUEST_NEGATIVE_ACK;
            case ABA_EHR_EXTRACT_REJECTED_WRONG_PATIENT -> EHR_EXTRACT_REQUEST_NEGATIVE_ACK;
            case NON_ABA_EHR_EXTRACT_REJECTED_WRONG_PATIENT -> EHR_EXTRACT_REQUEST_NEGATIVE_ACK;
        };
    }
}

