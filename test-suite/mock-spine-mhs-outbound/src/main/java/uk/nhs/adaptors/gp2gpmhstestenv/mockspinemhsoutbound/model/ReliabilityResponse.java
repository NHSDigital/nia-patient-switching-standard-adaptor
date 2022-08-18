package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class ReliabilityResponse {
    @NonNull
    private String nhsMHSSyncReplyMode;
    @NonNull
    private String nhsMHSRetries;
    @NonNull
    private String nhsMHSPersistDuration;
    @NonNull
    private String nhsMHSAckRequested;
    @NonNull
    private String nhsMHSDuplicateElimination;
    @NonNull
    private String nhsMHSRetryInterval;
}
