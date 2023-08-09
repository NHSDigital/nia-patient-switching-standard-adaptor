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

    private static String originalFilenameRegEx = "Filename=\"([A-Za-z\\d\\-_. ]*)\"";

    private static String isCompressedRegEx = "Compressed=(Yes|No|true|false)";

    private static Pattern filenamePattern;

    private static Pattern compressedPattern;

    private static Matcher filenameMatcher;

    private static Matcher compressedMatcher;

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

    private Boolean isTppAttachment(String description) {
        filenamePattern = Pattern.compile(originalFilenameRegEx);
        compressedPattern = Pattern.compile(isCompressedRegEx);

        filenameMatcher = filenamePattern.matcher(description);
        compressedMatcher = compressedPattern.matcher(description);

        if (filenameMatcher.find() || compressedMatcher.find()) {
            return true;
        }

        return false;
    }

    private String parseFilename(String description) throws ParseException {
        if (!isTppAttachment(description)) {
            return description;
        }

        filenamePattern = Pattern.compile(originalFilenameRegEx);
        filenameMatcher = filenamePattern.matcher(description);

        if (filenameMatcher.find()) {
            return filenameMatcher.group(1);
        }

        throw new ParseException("Unable to parse originalFilename field in description", 0);
    }

    private boolean parseCompressed(String description) throws ParseException {
        if (isTppAttachment(description)) {
            compressedPattern = Pattern.compile(isCompressedRegEx);
            compressedMatcher = compressedPattern.matcher(description);

            if (compressedMatcher.find()) {
                return (compressedMatcher.group(1).equals("Yes") || compressedMatcher.group(1).equals("true"));
            }
            throw new ParseException("Unable to parse isCompressed field in description", 0);
        }

        return false;
    }
}
