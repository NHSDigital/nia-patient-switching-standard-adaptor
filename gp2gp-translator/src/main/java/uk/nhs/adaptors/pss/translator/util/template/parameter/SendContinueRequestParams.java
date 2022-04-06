package uk.nhs.adaptors.pss.translator.util.template.parameter;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SendContinueRequestParams {
    private String messageId;
    private String timestamp;
    private String conversationId;
    private String nhsNumber;
    private String toAsid;
    private String fromAsid;
    private String toOdsCode;
    private String fromOdsCode;
    private String mcciIN010000UK13creationTime;
}
