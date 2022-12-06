package uk.nhs.adaptors.connector.model;

import lombok.Getter;

@Getter
public enum MigrationStatus {
    REQUEST_RECEIVED,
    EHR_EXTRACT_REQUEST_ACCEPTED,
    EHR_EXTRACT_REQUEST_ERROR,
    EHR_EXTRACT_REQUEST_ACKNOWLEDGED,
    EHR_EXTRACT_REQUEST_NEGATIVE_ACK,
    EHR_EXTRACT_RECEIVED,
    EHR_EXTRACT_PROCESSING,
    EHR_EXTRACT_TRANSLATED,
    CONTINUE_REQUEST_ACCEPTED,
    CONTINUE_REQUEST_ERROR,
    MIGRATION_COMPLETED,
    ERROR_LRG_MSG_REASSEMBLY_FAILURE,
    ERROR_LRG_MSG_ATTACHMENTS_NOT_RECEIVED,
    ERROR_LRG_MSG_GENERAL_FAILURE,
    ERROR_LRG_MSG_TIMEOUT,
    EHR_GENERAL_PROCESSING_ERROR,
    COPC_MESSAGE_RECEIVED,
    COPC_MESSAGE_PROCESSING,
    COPC_ACKNOWLEDGED,
    COPC_FAILED,
    FINAL_ACK_SENT
}
