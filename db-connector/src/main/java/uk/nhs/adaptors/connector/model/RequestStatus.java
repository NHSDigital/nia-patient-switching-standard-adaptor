package uk.nhs.adaptors.connector.model;

import lombok.Getter;

@Getter
public enum RequestStatus {
    RECEIVED,
    IN_PROGRESS,
    EXTRACT_RECEIVED,
    COMPLETED,
    ERROR;
}
