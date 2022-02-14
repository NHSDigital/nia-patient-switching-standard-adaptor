package uk.nhs.adaptors.pss.translator.mhs.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class OutboundMessage {
    private String payload;
}
