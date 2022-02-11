package uk.nhs.adaptors.pss.translator.mhs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@AllArgsConstructor
@Data
public class OutboundMessage {
    private String payload;
}
