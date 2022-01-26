package uk.nhs.adaptors.connector.model;

import lombok.Getter;

@Getter
public enum RequestStatus {
    RECEIVED,
    IN_PROGRESS,
    EHR_EXTRACT_REQUEST_ACCEPTED,
    EHR_EXTRACT_REQUEST_ERROR,
    EXTRACT_RECEIVED,
    COMPLETED,
    ERROR;
}
