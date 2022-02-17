package uk.nhs.adaptors.common.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;

import org.springframework.util.ResourceUtils;

import lombok.SneakyThrows;

public class FileUtil {
    @SneakyThrows
    public static String readResourceAsString(String path) {
        return readString(ResourceUtils.getFile("classpath:" + path).toPath(), UTF_8);
    }
}
