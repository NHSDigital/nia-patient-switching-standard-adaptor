package uk.nhs.adaptors.connector.model;

import lombok.Getter;

@Getter
public enum MigrationStatus {
    REQUEST_RECEIVED,
    EHR_EXTRACT_REQUEST_ACCEPTED,
    EHR_EXTRACT_REQUEST_ERROR,
    EHR_EXTRACT_RECEIVED,
    EHR_EXTRACT_TRANSLATED,
    MIGRATION_COMPLETED
}
