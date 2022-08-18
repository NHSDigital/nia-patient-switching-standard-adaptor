package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class RoutingResponse {
    @NonNull
    private String nhsMHSEndPoint;
    @NonNull
    private String nhsMHSPartyKey;
    @NonNull
    private String nhsMhsCPAId;
    @NonNull
    private String uniqueIdentifier;
}
