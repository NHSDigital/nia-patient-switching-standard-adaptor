package utility;

import java.io.File;

/**
 * The FileFactory aims to reduce the memory footprint when running the tests,
 * instead of statically loading all test data into memory we essentially lazy
 * load it, allowing for garbage collection to kick in after each test.
 */
public final class FileFactory {
    private static final String FILE_PATH = "src/test/resources/xml/%s";
    private FileFactory() { }

    public static File getFileFor(final String fileName) {
        return new File(FILE_PATH.formatted(fileName));
    }
}