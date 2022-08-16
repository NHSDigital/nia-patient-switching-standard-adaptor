package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@Entity
@Table(name="migration_status_log")
public class MigrationStatusLog {
    private int id;

    @Enumerated(EnumType.STRING)
    private MigrationStatus migrationStatus;   //
    private OffsetDateTime date;
    private int migrationRequestId;
    private String messageId;
}
