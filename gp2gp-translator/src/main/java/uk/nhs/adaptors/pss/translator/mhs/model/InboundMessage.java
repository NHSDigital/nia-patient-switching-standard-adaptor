package uk.nhs.adaptors.pss.translator.mhs.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InboundMessage {
    private String ebXML;
    private String payload;
}
