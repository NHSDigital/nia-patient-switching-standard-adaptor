package uk.nhs.adaptors.pss.translator.model;

import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_NEGATIVE_ACK_ABA_INCORRECT_PATIENT;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_NEGATIVE_ACK_FAILED_TO_INTEGRATE;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_NEGATIVE_ACK_NON_ABA_INCORRECT_PATIENT;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_NEGATIVE_ACK_SUPPRESSED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_PATIENT_NOT_REGISTERED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_GENERAL_PROCESSING_ERROR;
import static uk.nhs.adaptors.common.enums.MigrationStatus.ERROR_EXTRACT_CANNOT_BE_PROCESSED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.ERROR_LRG_MSG_ATTACHMENTS_NOT_RECEIVED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.ERROR_LRG_MSG_GENERAL_FAILURE;
import static uk.nhs.adaptors.common.enums.MigrationStatus.ERROR_LRG_MSG_REASSEMBLY_FAILURE;
import static uk.nhs.adaptors.common.enums.MigrationStatus.ERROR_LRG_MSG_TIMEOUT;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.common.enums.MigrationStatus;

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
    PATIENT_NOT_AT_SURGERY("06"),
    NON_ABA_EHR_EXTRACT_REJECTED_WRONG_PATIENT("28");

    @Getter
    private final String code;

    public MigrationStatus getMigrationStatus() {
        return switch (this) {
            case LARGE_MESSAGE_ATTACHMENTS_NOT_RECEIVED -> ERROR_LRG_MSG_ATTACHMENTS_NOT_RECEIVED;
            case LARGE_MESSAGE_GENERAL_FAILURE -> ERROR_LRG_MSG_GENERAL_FAILURE;
            case LARGE_MESSAGE_REASSEMBLY_FAILURE -> ERROR_LRG_MSG_REASSEMBLY_FAILURE;
            case LARGE_MESSAGE_TIMEOUT -> ERROR_LRG_MSG_TIMEOUT;
            case UNEXPECTED_CONDITION -> EHR_GENERAL_PROCESSING_ERROR;
            case EHR_EXTRACT_CANNOT_BE_PROCESSED -> ERROR_EXTRACT_CANNOT_BE_PROCESSED;
            case CLINICAL_SYSTEM_INTEGRATION_FAILURE -> EHR_EXTRACT_NEGATIVE_ACK_FAILED_TO_INTEGRATE;
            case ABA_EHR_EXTRACT_SUPPRESSED  ->  EHR_EXTRACT_NEGATIVE_ACK_SUPPRESSED;
            case ABA_EHR_EXTRACT_REJECTED_WRONG_PATIENT -> EHR_EXTRACT_NEGATIVE_ACK_ABA_INCORRECT_PATIENT;
            case NON_ABA_EHR_EXTRACT_REJECTED_WRONG_PATIENT -> EHR_EXTRACT_NEGATIVE_ACK_NON_ABA_INCORRECT_PATIENT;
            case PATIENT_NOT_AT_SURGERY -> EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_PATIENT_NOT_REGISTERED;
        };
    }
}

