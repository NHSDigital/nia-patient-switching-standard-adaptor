package uk.nhs.adaptors.pss.translator.util;

import static org.apache.commons.lang3.StringUtils.deleteWhitespace;

import org.apache.commons.lang3.StringUtils;

public class TextUtil {

    public static String getLastLine(String text) {
        if (text == null) {
            return StringUtils.EMPTY;
        }

        if (!text.contains(StringUtils.LF)) {
            return text;
        }

        return deleteWhitespace(text.substring(text.lastIndexOf(StringUtils.LF)));
    }

    public static String addLine(String text, String line) {
        if (text == null) {
            return StringUtils.EMPTY;
        }

        return text.concat(StringUtils.LF).concat(line);
    }

}
