package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Builder
@Entity
@Table(name = "patient_migration_request")
public class PatientMigrationRequest {
    private int id;
    private String patientNhsNumber;
    private String bundleResource;
    private String inboundMessage;
    private String conversationId;
    private String losingPracticeOdsCode;
    private String winningPracticeOdsCode;
}
