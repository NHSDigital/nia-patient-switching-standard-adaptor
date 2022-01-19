package uk.nhs.adaptors.connector.model;

import lombok.Getter;

@Getter
public enum RequestStatus {
    RECEIVED,
    IN_PROGRESS,
    MHS_ACCEPTED,
    MHS_BAD_REQUEST,
    EXTRACT_RECEIVED,
    COMPLETED,
    ERROR;
}
