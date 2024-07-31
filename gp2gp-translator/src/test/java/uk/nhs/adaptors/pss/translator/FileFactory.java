package uk.nhs.adaptors.pss.translator;

import java.io.File;

public final class FileFactory {
    private static final String BASE_FILE_PATH = "src/test/resources/xml/%s/%s";
    private FileFactory() { }

    public static File getXmlFileFor(String folderName, String fileName) {
        return new File(BASE_FILE_PATH.formatted(folderName, fileName));
    }
}