package uk.nhs.adaptors.pss.translator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class EhrExtractRequest {
    private String patientNHSNumber;
    private Boolean includeSensitiveInformation;
}
