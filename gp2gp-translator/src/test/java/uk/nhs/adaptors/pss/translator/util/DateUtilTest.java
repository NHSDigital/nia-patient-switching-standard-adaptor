package uk.nhs.adaptors.pss.translator.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.stream.Stream;

import org.hl7.v3.IVLTS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.utils.DateFormatUtil;


public class DateUtilTest {
    private static final String XML_RESOURCES_BASE = "xml/DateFormat/";

    @ParameterizedTest(name = "parseDate")
    @MethodSource("dates")
    public void shouldParseForCorrectDateFormat(String inputDate, String expectedDate) {
        assertThat(DateFormatUtil.parse(inputDate).asStringValue()).isEqualTo(expectedDate);
    }

    @ParameterizedTest(name = "parseDateToInstantType")
    @MethodSource("datesToInstant")
    public void shouldParseToInstantTypeForCorrectDateFormat(String inputString, String expectedDate) {
        assertThat(DateFormatUtil.parseToInstantType(inputString).asStringValue()).isEqualTo(expectedDate);
    }

    @Test
    public void shouldThrowExceptionForEmptyString() {
        String dateAsString = "";
        assertThrows(IllegalStateException.class, () -> DateFormatUtil.parse(dateAsString));
    }

    @Test
    public void shouldThrowExceptionForIncorrectDateFormat() {
        String dateAsString = "202019891898.00";
        assertThrows(IllegalStateException.class, () -> DateFormatUtil.parse(dateAsString));
    }

    private static Stream<Arguments> dates() {
        return Stream.of(
            Arguments.of(
                unmarshallDateElement("Year.xml"),
                "2011"
            ),
            Arguments.of(
                unmarshallDateElement("YearMonth.xml"),
                "2015-02"
            ),
            Arguments.of(
                unmarshallDateElement("YearMonthDay.xml"),
                "2017-03-22"
            ),
            Arguments.of(
                unmarshallDateElement("Hour.xml"),
                "2018-07-25T17:00:00+00:00"
            ),
            Arguments.of(
                unmarshallDateElement("HourMinute.xml"),
                "2018-12-25T18:20:00+00:00"
            ),
            Arguments.of(
                unmarshallDateElement("DateTimeWithPositiveTimezone.xml"),
                "2018-06-25T17:20:21+00:00"
            ),
            Arguments.of(
                unmarshallDateElement("DateTimeWithNegativeTimezone.xml"),
                "2020-07-25T22:20:21+00:00"
            ),
            Arguments.of(
                unmarshallDateElement("DateTimeWithDot.xml"),
                "2004-02-25T12:05:30.055+00:00"
            ),
            Arguments.of(
                unmarshallDateElement("DateTimeWithDotPositiveTimezone.xml"),
                "2003-06-25T09:05:30.055+00:00"
            )
        );
    }

    private static Stream<Arguments> datesToInstant() {
        return Stream.of(
            Arguments.of(
                unmarshallDateElement("Year.xml"),
                "2011-01-01T00:00:00.000+00:00"
            ),
            Arguments.of(
                unmarshallDateElement("YearMonth.xml"),
                "2015-02-01T00:00:00.000+00:00"
            ),
            Arguments.of(
                unmarshallDateElement("YearMonthDay.xml"),
                "2017-03-22T00:00:00.000+00:00"
            ),
            Arguments.of(
                unmarshallDateElement("Hour.xml"),
                "2018-07-25T17:00:00.000+00:00"
            ),
            Arguments.of(
                unmarshallDateElement("HourMinute.xml"),
                "2018-12-25T18:20:00.000+00:00"
            ),
            Arguments.of(
                unmarshallDateElement("HourMinuteSecond.xml"),
                "2018-07-25T17:20:21.000+00:00"
            ),
            Arguments.of(
                unmarshallDateElement("DateTimeWithPositiveTimezone.xml"),
                "2018-06-25T17:20:21.000+00:00"
            ),
            Arguments.of(
                unmarshallDateElement("DateTimeWithNegativeTimezone.xml"),
                "2020-07-25T22:20:21.000+00:00"
            ),
            Arguments.of(
                unmarshallDateElement("DateTimeWithDot.xml"),
                "2004-02-25T12:05:30.055+00:00"
            ),
            Arguments.of(
                unmarshallDateElement("DateTimeWithDotPositiveTimezone.xml"),
                "2003-06-25T09:05:30.055+00:00"
            )
        );
    }

    @SneakyThrows
    private static String unmarshallDateElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), IVLTS.class).getCenter().getValue();
    }
}
