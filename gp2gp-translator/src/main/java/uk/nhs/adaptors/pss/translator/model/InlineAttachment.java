package uk.nhs.adaptors.pss.translator.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;

@Getter
public class InlineAttachment {
    private final String originalFilename;
    private final String contentType;
    private final boolean isCompressed;
    private final boolean isLargeAttachment;
    private final boolean isOriginalBase64;
    private final boolean isBase64;
    private final String description;
    private final String payload;

    public InlineAttachment(InboundMessage.Attachment attachment) {
        this.originalFilename = parseFilename(attachment.getDescription());
        this.contentType = attachment.getContentType();
        this.isCompressed = parseCompressed(attachment.getDescription());
        this.isLargeAttachment = parseLargeAttachment(attachment.getDescription());
        this.isOriginalBase64 = parseOriginalBase64(attachment.getDescription());
        this.isBase64 = Boolean.parseBoolean(attachment.getIsBase64());
        this.description = attachment.getDescription();
        this.payload = attachment.getPayload();
    }

    private String parseFilename(String description) {
        Pattern pattern = Pattern.compile("Filename=\"([A-Za-z\\d\\-_. ]*)\"");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }

    private boolean parseCompressed(String description) {
        Pattern pattern = Pattern.compile("Compressed=(Yes|No)");
        Matcher matcher = pattern.matcher(description);

        return matcher.find() && matcher.group(1).equals("Yes");
    }

    private boolean parseLargeAttachment(String description) {
        Pattern pattern = Pattern.compile("LargeAttachment=(Yes|No)");
        Matcher matcher = pattern.matcher(description);

        return matcher.find() && matcher.group(1).equals("Yes");
    }

    private boolean parseOriginalBase64(String description) {
        Pattern pattern = Pattern.compile("OriginalBase64=(Yes|No)");
        Matcher matcher = pattern.matcher(description);

        return matcher.find() && matcher.group(1).equals("Yes");
    }
}
