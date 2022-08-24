package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "migration_request_id")
    private PatientMigrationRequest patientMigrationRequest;
}
