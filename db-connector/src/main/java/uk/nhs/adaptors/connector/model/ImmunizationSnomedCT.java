package uk.nhs.adaptors.connector.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ImmunizationSnomedCT {
    private String id;
    private String conceptid;
    private String term;
}