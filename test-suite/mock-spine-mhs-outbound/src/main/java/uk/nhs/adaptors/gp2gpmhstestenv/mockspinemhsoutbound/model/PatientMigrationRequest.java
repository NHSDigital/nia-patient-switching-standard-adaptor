package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "patient_migration_request")
public class PatientMigrationRequest {
    @Id
    private int id;
    private String patientNhsNumber;
    private String bundleResource;
    private String inboundMessage;
    private String conversationId;
    private String losingPracticeOdsCode;
    private String winningPracticeOdsCode;
    @OneToMany(mappedBy = "patientMigrationRequest", cascade = CascadeType.ALL)
    private List<MigrationStatusLog> migrationStatusLog;


    @OneToMany(mappedBy = "patientMigrationRequest", cascade = CascadeType.ALL)
    private List<MessagePersistDuration> messagePersistDurationList;
    @OneToMany(mappedBy = "patientMigrationRequest", cascade = CascadeType.ALL)
    private List<PatientAttachmentLog> patientAttachmentLog;

}
