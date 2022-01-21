package uk.nhs.adaptors.pss.translator.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import io.micrometer.core.instrument.util.IOUtils;

public class ResourceTestFileUtils {
    public static String getFileContent(String filePath) throws IOException {
        try (InputStream is = ResourceTestFileUtils.class.getResourceAsStream(filePath)) {
            if (is == null) {
                throw new FileNotFoundException(filePath);
            }
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }
}