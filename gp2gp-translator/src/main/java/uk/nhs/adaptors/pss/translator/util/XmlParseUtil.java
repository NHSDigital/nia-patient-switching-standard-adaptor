package uk.nhs.adaptors.pss.translator.util;

import java.text.ParseException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hl7.v3.RCMRIN030000UK06Message;

public class XmlParseUtil {

    public static String parseFragmentFilename(String description) {

        return Arrays.asList(description.split(" ")).stream()
            .filter(desc -> desc.contains("Filename"))
            .map(desc -> desc.replace("Filename=", ""))
            .toList().get(0);
    }

    public static boolean parseBase64(String description) throws ParseException {
        Pattern pattern = Pattern.compile("OriginalBase64=(Yes|No)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1).equals("Yes");
        }

        throw new ParseException("Unable to parse isBase64", 0);
    }

    public static boolean parseLargeAttachment(String description) throws ParseException {
        Pattern pattern = Pattern.compile("LargeAttachment=(Yes|No)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1).equals("Yes");
        }

        throw new ParseException("Unable to parse isLargeAttachment", 0);
    }

    public static boolean parseCompressed(String description) throws ParseException {
        Pattern pattern = Pattern.compile("Compressed=(Yes|No)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1).equals("Yes");
        }

        throw new ParseException("Unable to parse isCompressed", 0);
    }

    public static String parseContentType(String description) throws ParseException {
        Pattern pattern = Pattern.compile("ContentType=([A-Za-z\\d\\-/]*)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new ParseException("Unable to parse ContentType", 0);
    }

    public static String parseNhsNumber(RCMRIN030000UK06Message payload) {
        return payload
            .getControlActEvent()
            .getSubject()
            .getEhrExtract()
            .getRecordTarget()
            .getPatient()
            .getId()
            .getExtension();
    }

    public static String parseFilename(String description) throws ParseException {
        Pattern pattern = Pattern.compile("Filename=\"([A-Za-z\\d\\-_. ]*)\"");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new ParseException("Unable to parse originalFilename", 0);
    }

    public static int parseFileLength(String description) {
        Pattern pattern = Pattern.compile("Length=([\\d]*)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public static boolean parseIsSkeleton(String description) {
        final String EB_SKELETON_PROP = "X-GP2GP-Skeleton:Yes".toLowerCase();
        return description.replaceAll("\\s+", "").toLowerCase().contains(EB_SKELETON_PROP);
    }
}
