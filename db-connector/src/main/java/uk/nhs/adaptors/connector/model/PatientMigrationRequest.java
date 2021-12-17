package uk.nhs.adaptors.connector.model;

import java.time.OffsetDateTime;

import org.jdbi.v3.core.enums.EnumByName;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PatientMigrationRequest {
    private int id;
    private String patientNhsNumber;
    @EnumByName
    private RequestStatus requestStatus;
    private OffsetDateTime date;
}
