package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuccessTemplateParams {
    private String conversationId;
    private String interactionId;
    private String messageId;
    private String refToMessageId;
    private String timestamp;
}
