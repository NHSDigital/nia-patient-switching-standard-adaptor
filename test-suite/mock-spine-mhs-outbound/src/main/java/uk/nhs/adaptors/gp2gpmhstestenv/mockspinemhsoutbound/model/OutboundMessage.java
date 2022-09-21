package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Map;

@Data
@Builder
public class OutboundMessage {
    @NonNull
    private Map<String, String> headers;
    @NonNull
    private String body;
}
