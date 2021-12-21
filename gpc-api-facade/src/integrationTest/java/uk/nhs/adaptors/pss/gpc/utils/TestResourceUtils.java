package uk.nhs.adaptors.pss.gpc.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.gpc.controller.PatientTransferControllerIT;

public class TestResourceUtils {
    @SneakyThrows
    public static String readResourceAsString(String path) {
        URL resource = PatientTransferControllerIT.class.getResource(path);
        return Files.readString(Paths.get(resource.getPath()), UTF_8);
    }
}
