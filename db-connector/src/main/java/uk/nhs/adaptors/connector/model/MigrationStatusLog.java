package uk.nhs.adaptors.connector.model;

import java.time.OffsetDateTime;

import org.jdbi.v3.core.enums.EnumByName;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MigrationStatusLog {
    private int id;
    @EnumByName
    private RequestStatus requestStatus;
    private OffsetDateTime date;
    private int migrationRequestId;
}
