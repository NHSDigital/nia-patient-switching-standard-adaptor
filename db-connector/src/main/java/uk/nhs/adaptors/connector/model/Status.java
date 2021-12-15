package uk.nhs.adaptors.connector.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Status {
    RECEIVED("RECEIVED"),
    IN_PROGRESS("IN_PROGRESS"),
    EXTRACT_RECEIVED("EXTRACT_RECEIVED"),
    COMPLETED("COMPLETED"),
    ERROR("ERROR");

    private final String value;
}
