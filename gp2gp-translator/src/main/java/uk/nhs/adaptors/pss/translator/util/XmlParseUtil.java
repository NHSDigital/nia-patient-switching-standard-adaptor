package uk.nhs.adaptors.pss.translator.util;

import java.text.ParseException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hl7.v3.COPCIN000001UK01Message;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XmlParseUtil {

    public static String parseFragmentFilename(String description) {
        try {
            return Arrays.stream(description.split(" "))
                .filter(desc -> desc.contains("Filename"))
                .map(desc -> desc.replace("Filename=", "").replace("\"", ""))
                .toList().get(0);
        } catch(IndexOutOfBoundsException e) {
            return "";
        }
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


    ////////////////////////COPCIN000001UK01

    public static String parseFromAsid(COPCIN000001UK01Message payload) {
        return payload.getCommunicationFunctionRcv()
                .get(0)
                .getDevice()
                .getId()
                .get(0)
                .getExtension();
    }

    public static String parseToAsid(COPCIN000001UK01Message payload) {
        return payload.getCommunicationFunctionSnd()
                .getDevice()
                .getId()
                .get(0)
                .getExtension();
    }

    public static String parseMessageRef(COPCIN000001UK01Message payload) {
        return payload.getId().getRoot();
    }


    public static String parseToOdsCode(COPCIN000001UK01Message payload) {

        Element gp2gpElement = payload.getControlActEvent()
                .getSubject()
                .getPayloadInformation()
                .getValue()
                .getAny()
                .get(0);

        return getFromPractiseValue(gp2gpElement);
    }


    public static String getFromPractiseValue(Element gp2gpElement) {
        for (int i = 0; i < gp2gpElement.getChildNodes().getLength(); i++) {
            Node currNode = gp2gpElement.getChildNodes().item(i);
            if (currNode.getLocalName().equals("From")) {
                return currNode.getFirstChild().getNodeValue();
            }
        }
        return null;
    }

    //////////////RCMRIN030000UK06

    public static String parseFromAsid(RCMRIN030000UK06Message payload) {
        return payload.getCommunicationFunctionRcv()
                .get(0)
                .getDevice()
                .getId()
                .get(0)
                .getExtension();
    }

    public static String parseToAsid(RCMRIN030000UK06Message payload) {
        return payload.getCommunicationFunctionSnd()
                .getDevice()
                .getId()
                .get(0)
                .getExtension();
    }

    public static String parseToOdsCode(RCMRIN030000UK06Message payload) {
        return payload.getControlActEvent()
                .getSubject()
                .getEhrExtract()
                .getAuthor()
                .getAgentOrgSDS()
                .getAgentOrganizationSDS()
                .getId()
                .getExtension();
    }

    public static String parseMessageRef(RCMRIN030000UK06Message payload) {
        return payload.getId().getRoot();
    }



}
