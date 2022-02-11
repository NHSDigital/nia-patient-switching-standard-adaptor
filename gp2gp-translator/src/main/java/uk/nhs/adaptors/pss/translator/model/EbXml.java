package uk.nhs.adaptors.pss.translator.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EbXml {
    private String conversationId;
    private List<Reference> references;
}
