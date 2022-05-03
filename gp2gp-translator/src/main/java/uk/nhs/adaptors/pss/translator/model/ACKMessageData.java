package uk.nhs.adaptors.pss.translator.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
@EqualsAndHashCode
public class ACKMessageData {
    @NonNull
    private String conversationId;

    @NonNull
    private String toOdsCode;

    @NonNull
    private String messageRef;

    @NonNull
    private String toAsid;

    @NonNull
    private String fromAsid;
}