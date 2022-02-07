package uk.nhs.adaptors.pss.translator.mhs.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InboundMessage {
    private String ebXML;
    private String payload;
    private List<String> attachments;
    @JsonProperty("external_attachments")
    private List<String> externalAttachments;
}
