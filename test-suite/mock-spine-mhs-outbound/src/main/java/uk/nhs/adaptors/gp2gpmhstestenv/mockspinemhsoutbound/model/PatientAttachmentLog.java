package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "patient_attachment_log")
public class PatientAttachmentLog {

    @Id
    private String id;
    private String mid;
    private String parentMid;
    private String filename;
    private String contentType;
    private Boolean compressed;
    private Boolean largeAttachment;
    private Boolean base64;
    private Boolean skeleton;
    private Boolean uploaded;
    private Integer lengthNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "migration_request_id")
    private PatientMigrationRequest patientMigrationRequest;
    private Integer orderNum;
    private Boolean deleted;
}
