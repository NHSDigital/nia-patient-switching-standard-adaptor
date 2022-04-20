package uk.nhs.adaptors.pss.translator.util.template.parameter;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplicationAcknowledgmentParams {
    private String messageId;
    private String creationTime;
    private String nackCode;
    private String messageRef;
    private String toAsid;
    private String fromAsid;
}
