package uk.nhs.adaptors.pss.translator.util;

import static org.assertj.core.api.Assertions.assertThat;

import static uk.nhs.adaptors.pss.translator.util.TextUtil.addLine;
import static uk.nhs.adaptors.pss.translator.util.TextUtil.getLastLine;

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TextUtilTest {

    private static final String THREE_LINER = """
        HELLO
        PS
        ADAPTER""";

    private static final String TWO_LINER = """
        HELLO
        WORLD""";

    private static final String ONE_LINER = "HELLO";

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
    public void testAddingLine() {
        String shouldBeFourLiner = addLine(THREE_LINER, "FOURTH_LINE");
        assertThat(shouldBeFourLiner.split(StringUtils.LF).length).isEqualTo(4);
    }

    private static Stream<Arguments> testInputs() {
        return Stream.of(
            Arguments.of(ONE_LINER, ONE_LINER),
            Arguments.of(TWO_LINER, "WORLD"),
            Arguments.of(THREE_LINER, "ADAPTER"),
            Arguments.of(SPATIAL_THREE_LINER, "ADAPTER"),
            Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY)
        );
    }

}
