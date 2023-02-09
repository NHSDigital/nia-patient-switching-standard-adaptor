package uk.nhs.adaptors.pss.translator.util;

import static org.assertj.core.api.Assertions.assertThat;

import static uk.nhs.adaptors.pss.translator.util.TextUtil.addLine;
import static uk.nhs.adaptors.pss.translator.util.TextUtil.extractPimpComment;
import static uk.nhs.adaptors.pss.translator.util.TextUtil.getLastLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import lombok.SneakyThrows;

public class TextUtilTest {

    private static final String PIMP_COMMENTS_DIR = "pimp-comments/";

    private static final String THREE_LINER = """
        HELLO
        PS
        ADAPTER""";

    private static final String TWO_LINER = """
        HELLO
        WORLD""";

    private static final String ONE_LINER = "HELLO";

    @SuppressWarnings("RegexpSingleline")
    private static final String SPATIAL_THREE_LINER = """
        HELLO           
            PS            
                 ADAPTER       """;

    @ParameterizedTest
    @MethodSource("testInputs")
    public void testGettingOneLiner(String input, String expected) {
        String line = getLastLine(input);
        assertThat(line).isEqualTo(expected);
    }

    @Test
    @SuppressWarnings("checkstyle:magicnumber")
    public void testAddingLine() {
        String shouldBeFourLiner = addLine(THREE_LINER, "FOURTH_LINE");
        assertThat(shouldBeFourLiner.split(StringUtils.LF).length).isEqualTo(4);
    }

    @ParameterizedTest
    @MethodSource("pimpComments")
    @SneakyThrows
    public void When_ExtractPimpComment_WithValidComment_Expect_CommentExtracted(String inputFile, String outputFile) {
        var input = readFileAsString(PIMP_COMMENTS_DIR + inputFile);
        var expectedOutput = readFileAsString(PIMP_COMMENTS_DIR + outputFile);

        var output = extractPimpComment(input);

        assertThat(output).isEqualTo(expectedOutput);
    }

    @ParameterizedTest
    @MethodSource("invalidPimpComments")
    @SneakyThrows
    public void When_ExtractPimpComment_WithInvalidComment_Expect_InputReturned(String inputFile) {
        var input = readFileAsString(PIMP_COMMENTS_DIR + inputFile);

        var output = extractPimpComment(input);

        assertThat(output).isEqualTo(input);
    }

    private static Stream<Arguments> pimpComments() {
        return Stream.of(
            Arguments.of("aggregate-comment-1.txt", "aggregate-comment-1-output.txt"),
            Arguments.of("aggregate-comment-2.txt", "aggregate-comment-2-output.txt"),
            Arguments.of("lab-specimen-comment-1.txt", "lab-specimen-comment-1-output.txt"),
            Arguments.of("laboratory-result-comment-1.txt", "laboratory-result-comment-1-output.txt"),
            Arguments.of("laboratory-result-comment-2.txt", "laboratory-result-comment-2-output.txt"),
            Arguments.of("user-comment-1.txt", "user-comment-1-output.txt")
            );
    }

    private static Stream<Arguments> invalidPimpComments() {
        return Stream.of(
            Arguments.of("invalid-pimp-comment-1.txt"));
    }

    private static Stream<Arguments> testInputs() {
        return Stream.of(
            Arguments.of(ONE_LINER, ONE_LINER),
            Arguments.of(TWO_LINER, "WORLD"),
            Arguments.of(THREE_LINER, "ADAPTER"),
            Arguments.of(SPATIAL_THREE_LINER, "ADAPTER"),
            Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY),
            Arguments.of(null, StringUtils.EMPTY)
        );
    }

    private String readFileAsString(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return Files.readString(Paths.get(resource.getURI()));
    }
}
