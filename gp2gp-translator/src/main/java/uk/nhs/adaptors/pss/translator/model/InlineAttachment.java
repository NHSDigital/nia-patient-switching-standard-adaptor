package uk.nhs.adaptors.pss.translator.model;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.util.XmlParseUtilService;

@Getter
public class InlineAttachment {
    private final String originalFilename;
    private final String contentType;
    private final boolean isCompressed;
    private final boolean isBase64;
    private final String description;
    private final String payload;
    private final Integer length;

    public InlineAttachment(InboundMessage.Attachment attachment) throws ParseException {
        if (attachment.getDescription() == null) {
            throw new ParseException("Unable to parse NULL description", 0);
        }

        this.originalFilename = parseFilename(attachment.getDescription());
        this.contentType = attachment.getContentType();
        this.isCompressed = parseCompressed(attachment.getDescription());
        this.isBase64 = Boolean.parseBoolean(attachment.getIsBase64());
        this.description = attachment.getDescription();
        this.payload = attachment.getPayload();
        this.length = XmlParseUtilService.parseFileLength(attachment.getDescription());
    }

    private String parseFilename(String description) {
        Pattern pattern = Pattern.compile("Filename=\"([A-Za-z\\d\\-_. ]*)\"");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return description;
    }

    private boolean parseCompressed(String description) {
        Pattern pattern = Pattern.compile("Compressed=(Yes|No|true|false)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return (matcher.group(1).equals("Yes") || matcher.group(1).equals("true"));
        }

        return false;
    }
}
