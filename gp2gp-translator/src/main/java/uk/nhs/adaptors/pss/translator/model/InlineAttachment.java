package uk.nhs.adaptors.pss.translator.model;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;

@Getter
public class InlineAttachment {
    private final String originalFilename;
    private final String contentType;
    private final boolean isCompressed;
    private final boolean isBase64;
    private final String description;
    private final String payload;

    public InlineAttachment(InboundMessage.Attachment attachment) throws ParseException {
        this.originalFilename = parseFilename(attachment.getDescription());
        this.contentType = attachment.getContentType();
        this.isCompressed = parseCompressed(attachment.getDescription());
        this.isBase64 = Boolean.parseBoolean(attachment.getIsBase64());
        this.description = attachment.getDescription();
        this.payload = attachment.getPayload();
    }

    private String parseFilename(String description) throws ParseException {
        Pattern pattern = Pattern.compile("Filename=\"([A-Za-z\\d\\-_. ]*)\"");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new ParseException("Unable to parse originalFilename", 0);
    }

    private boolean parseCompressed(String description) throws ParseException {
        Pattern pattern = Pattern.compile("Compressed=(Yes|No)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1).equals("Yes");
        }

        throw new ParseException("Unable to parse isCompressed", 0);
    }
}
