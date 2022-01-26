package uk.nhs.adaptors.pss.translator.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

public class FileUtils {

    public static String readFile(String path) throws IOException {
        try (InputStream is = FileUtils.class.getResourceAsStream(path)) {
            if (is == null) { throw new FileNotFoundException(path); }
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }

}
