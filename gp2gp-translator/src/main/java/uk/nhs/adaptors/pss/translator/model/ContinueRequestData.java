package uk.nhs.adaptors.pss.translator.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
// TODO: This class is related to the large messaging epic and can be used during implementation of NIAD-2045
public class ContinueRequestData {
    private String nhsNumber;
    private String fromAsid;
    private String toAsid;
    private String toOdsCode;
    private String conversationId;
}
