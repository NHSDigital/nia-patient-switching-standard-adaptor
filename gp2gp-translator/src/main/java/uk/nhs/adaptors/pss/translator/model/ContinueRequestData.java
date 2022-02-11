package uk.nhs.adaptors.pss.translator.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ContinueRequestData {
    private String nhsNumber;
    private String fromAsid;
    private String toAsid;
    private String toOdsCode;
    private String conversationId;
}
