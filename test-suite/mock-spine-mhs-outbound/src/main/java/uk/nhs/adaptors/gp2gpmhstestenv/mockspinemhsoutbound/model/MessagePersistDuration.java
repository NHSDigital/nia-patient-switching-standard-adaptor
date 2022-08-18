package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model;

import lombok.*;

import javax.persistence.*;
import java.time.Duration;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="message_persist_duration")
public class MessagePersistDuration {
    @Id
    private int id;
    private String messageType;
    private Duration persistDuration;
    private int callsSinceUpdate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "migration_request_id")
    private PatientMigrationRequest patientMigrationRequest;
}
