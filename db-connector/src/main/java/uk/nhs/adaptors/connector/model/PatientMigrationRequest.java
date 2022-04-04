package uk.nhs.adaptors.connector.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PatientMigrationRequest {
    private int id;
    private String patientNhsNumber;
    private String bundleResource;
    private String inboundMessage;
    private String conversationId;
    private String loosingPracticeOdsCode;
    private String winningPracticeOdsCode;
}
