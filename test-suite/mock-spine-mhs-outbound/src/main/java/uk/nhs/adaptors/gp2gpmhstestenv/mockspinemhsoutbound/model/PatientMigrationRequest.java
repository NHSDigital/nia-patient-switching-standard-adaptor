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
@Table(name = "patient_migration_request", schema = "public")
public class PatientMigrationRequest {
    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "patient_nhs_number")
    private String patientNhsNumber;
    @Column(name = "bundle_resource")
    private String bundleResource;
    @Column(name = "inbound_message")
    private String inboundMessage;
    @Column(name = "conversation_id")
    private String conversationId;
    @Column(name = "losing_practice_ods_code")
    private String losingPracticeOdsCode;
    @Column(name = "winning_practice_ods_code")
    private String winningPracticeOdsCode;
    @OneToMany(mappedBy = "patientMigrationRequest", cascade = CascadeType.ALL)
    private List<MigrationStatusLog> migrationStatusLog;

    @OneToMany(mappedBy = "patientMigrationRequest", cascade = CascadeType.ALL)
    private List<MessagePersistDuration> messagePersistDurationList;
    @OneToMany(mappedBy = "patientMigrationRequest", cascade = CascadeType.ALL)
    private List<PatientAttachmentLog> patientAttachmentLog;

}
