package uk.nhs.adaptors.pss.translator.util;

import static org.apache.commons.lang3.StringUtils.deleteWhitespace;

import org.apache.commons.lang3.StringUtils;

public class TextUtil {

    public static String getLastLine(String text) {
        return deleteWhitespace(text.substring(text.lastIndexOf(StringUtils.LF)));
    }

}
