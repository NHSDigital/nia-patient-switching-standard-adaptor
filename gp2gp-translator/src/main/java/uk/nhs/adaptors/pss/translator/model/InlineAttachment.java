package uk.nhs.adaptors.pss.translator.model;

import java.text.ParseException;

import lombok.Builder;
import lombok.Getter;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.util.XmlParseUtilService;

@Getter
@Builder
public class InlineAttachment {
    private final String originalFilename;
    private final String contentType;
    private final boolean isCompressed;
    private final boolean isBase64;
    private final String description;
    private final String payload;
    private final Integer length;

    public static InlineAttachment fromInboundMessageAttachment(InboundMessage.Attachment attachment) throws ParseException {
        if (attachment.getDescription() == null) {
            throw new ParseException("Unable to parse NULL description", 0);
        }

        return InlineAttachment.builder()
                .originalFilename(parseFilename(attachment.getDescription()))
                .contentType(attachment.getContentType())
                .isCompressed(parseCompressed(attachment.getDescription()))
                .isBase64(Boolean.parseBoolean(attachment.getIsBase64()))
                .description(attachment.getDescription())
                .payload(attachment.getPayload())
                .length(XmlParseUtilService.parseFileLength(attachment.getDescription()))
                .build();
    }

    private static String parseFilename(String description) throws ParseException {
        if (XmlParseUtilService.isDescriptionEmisStyle(description)) {
            return description;
        }

        return XmlParseUtilService.parseFilename(description);
    }

    private static boolean parseCompressed(String description) throws ParseException {
        if (XmlParseUtilService.isDescriptionEmisStyle(description)) {
            return false;
        }

        return XmlParseUtilService.parseCompressed(description);
    }
}
