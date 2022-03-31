package uk.nhs.adaptors.pss.translator.util.template.parameter;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SendContinueRequestParams {
    private String messageId;
    private String timestamp;
    private String toAsid;
    private String fromAsid;
}
