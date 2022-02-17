package uk.nhs.adaptors.common.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import lombok.SneakyThrows;

public class FileUtil {
    @SneakyThrows
    public static String readResourceAsString(String path) {
        try (InputStream is = FileUtil.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new FileNotFoundException(path);
            }
            return IOUtils.toString(is, UTF_8);
        }
    }
}
