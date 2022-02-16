package uk.nhs.adaptors.common.testutil;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;

import org.springframework.util.ResourceUtils;

import lombok.SneakyThrows;

public class FileUtils {
    @SneakyThrows
    public static String readResourceAsString(String path) {
        return Files.readString(ResourceUtils.getFile("classpath:" + path).toPath(), UTF_8);
    }
}
