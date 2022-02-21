package uk.nhs.adaptors.pss.translator.util.template.parameter;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SendEhrExtractRequestParams {
    private String messageId;
    private String timestamp;
    private String toAsid;
    private String fromAsid;
    private String nhsNumber;
    private String ehrRequestId;
    private String fromOds;
    private String toOds;
}
