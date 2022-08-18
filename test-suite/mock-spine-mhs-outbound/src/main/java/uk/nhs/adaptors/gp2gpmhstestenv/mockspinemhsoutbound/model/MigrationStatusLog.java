package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model;

import lombok.*;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="migration_status_log")
public class MigrationStatusLog {

    @Id
    private int id;
    @Enumerated(EnumType.STRING)
    private MigrationStatus migrationStatus;
    private OffsetDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "migration_request_id")
    private PatientMigrationRequest patientMigrationRequest;
    private String messageId;
}
