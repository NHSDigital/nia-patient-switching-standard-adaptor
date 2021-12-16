package uk.nhs.adaptors.connector.model;

import java.time.OffsetDateTime;

import org.jdbi.v3.core.enums.EnumByName;

import lombok.Data;

@Data
public class PatientMigrationRequest {
    private String id;
    private String patientNhsNumber;
    @EnumByName
    private RequestStatus requestStatus;
    private OffsetDateTime date;
}
