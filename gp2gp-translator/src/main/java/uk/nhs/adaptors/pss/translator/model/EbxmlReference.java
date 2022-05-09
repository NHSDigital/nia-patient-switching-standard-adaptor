package uk.nhs.adaptors.pss.translator.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EbxmlReference {
    private String description;
    private String href;
}