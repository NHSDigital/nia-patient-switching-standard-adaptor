package uk.nhs.adaptors.pss.translator.mhs.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@NoArgsConstructor
public class InboundMessage {
    private String ebXML;
    private String payload;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Attachment> attachments;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("external_attachments")
    private List<ExternalAttachment> externalAttachments;

    @Data
    @Jacksonized
    @AllArgsConstructor
    @Builder
    public static class Attachment {
        @JsonProperty("content_type")
        private String contentType;
        @JsonProperty("is_base64")
        private String isBase64;
        private String description;
        private String payload;
    }

    @Data
    @Jacksonized
    @AllArgsConstructor
    @Builder
    public static class ExternalAttachment {
        @JsonProperty("document_id")
        private String documentId;
        @JsonProperty("message_id")
        private String messageId;
        private String title;
        private String description;
    }
}
