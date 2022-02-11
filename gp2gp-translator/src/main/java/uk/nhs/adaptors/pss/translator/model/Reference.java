package uk.nhs.adaptors.pss.translator.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Reference {
    private String id;
    private String href;
    private String description;
}
