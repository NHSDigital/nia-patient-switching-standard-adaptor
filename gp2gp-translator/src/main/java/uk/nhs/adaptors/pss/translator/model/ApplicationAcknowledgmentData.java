package uk.nhs.adaptors.pss.translator.model;

import java.util.Optional;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
@EqualsAndHashCode
public class ApplicationAcknowledgmentData {
    @NonNull
    private String conversationId;

    private String nackCode;

    @NonNull
    private String toOdsCode;

    @NonNull
    private String messageRef;

    @NonNull
    private String toAsid;

    @NonNull
    private String fromAsid;

    public Optional<String> getNackCode() {
        return nackCode == null ? Optional.empty() : Optional.of(nackCode);
    }
}
