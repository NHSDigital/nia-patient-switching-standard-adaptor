package uk.nhs.adaptors.connector.model;

import java.time.OffsetDateTime;

import org.jdbi.v3.core.enums.EnumByName;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.nhs.adaptors.common.enums.MigrationStatus;

@Getter
@Setter
@Builder
public class MigrationStatusLog {

    private int id;
    @EnumByName
    private MigrationStatus migrationStatus;
    private OffsetDateTime date;
    private int migrationRequestId;
    private String messageId;
    private String gp2gpErrorCode;
}
