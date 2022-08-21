package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model;

import lombok.*;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="migration_status_log", schema = "public")
public class MigrationStatusLog {

    @Id
    @Column(name="id")
    private int id;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private MigrationStatus migrationStatus;
    @Column(name = "date")
    private OffsetDateTime date;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "migration_request_id")
    private PatientMigrationRequest patientMigrationRequest;
}
