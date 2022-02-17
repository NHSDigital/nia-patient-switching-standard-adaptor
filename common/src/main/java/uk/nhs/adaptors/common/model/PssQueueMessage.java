package uk.nhs.adaptors.common.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PssQueueMessage {
    private String patientNhsNumber;
    private String toAsid;
    private String fromAsid;
    private String toOds;
    private String fromOds;
}
