package uk.nhs.adaptors.pss.translator.util;

import static java.time.ZoneOffset.UTC;
import static java.util.TimeZone.getTimeZone;

import static ca.uhn.fhir.model.api.TemporalPrecisionEnum.MILLI;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.InstantType;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import lombok.experimental.UtilityClass;
import uk.nhs.adaptors.pss.translator.model.DateFormat;

@UtilityClass
public class DateFormatUtil {
    private static final ZoneId UK_ZONE_ID = ZoneId.of("Europe/London");
    private static final int YEAR_PRECISION = 4;
    private static final int MONTH_PRECISION = 6;
    private static final int DAY_PRECISION = 8;
    private static final int HOUR_PRECISION = 10;
    private static final int MINUTE_PRECISION = 12;
    private static final int MILLISECOND_PRECISION = 18;
    private static final String DATETIME_TIMEZONE_MILLISECONDS_FORMAT = "yyyyMMddHHmmss.SSSX";
    private static final String DATETIME_TIMEZONE_SECONDS_FORMAT = "yyyyMMddHHmmssX";
    private static final String DATETIME_TIMEZONE_MINUTES_FORMAT = "yyyyMMddHHmmX";
    private static final String DATETIME_TIMEZONE_HOURS_FORMAT = "yyyyMMddHHX";
    private static final String DATETIME_TIMEZONE_FULL_MILLISECONDS_FORMAT = "yyyyMMddHHmmss.SSSZ";
    private static final String DATETIME_TIMEZONE_FULL_FORMAT = "yyyyMMddHHmmssZ";
    private static final String DATETIME_TIMEZONE_FULL_MINUTES_FORMAT = "yyyyMMddHHmmZ";
    private static final String DATETIME_TIMEZONE_FULL_HOURS_FORMAT = "yyyyMMddHHZ";
    private static final String DATETIME_MILLISECONDS_FORMAT = "yyyyMMddHHmmss.SSS";
    private static final String DATETIME_SECONDS_FORMAT = "yyyyMMddHHmmss";
    private static final String DATETIME_MINUTES_FORMAT = "yyyyMMddHHmm";
    private static final String DATETIME_HOURS_FORMAT = "yyyyMMddHH";
    private static final String DATE_FORMAT = "yyyyMMdd";
    private static final String DATE_MONTH_FORMAT = "yyyyMM";
    private static final String DATE_YEAR_FORMAT = "yyyy";
    private static final String ERROR_MESSAGE = "Unable to parse date %s to Fhir date format";
    private static final String HL7_DATETIME_FORMAT = "yyyyMMddHHmmss";
    private static final DateTimeFormatter HL7_SECONDS_COMPUTER_READABLE = DateTimeFormatter.ofPattern(HL7_DATETIME_FORMAT);

    public static DateTimeType parseToDateTimeType(String dateToParse) {
        DateFormat format = getFormat(dateToParse);
        SimpleDateFormat formatter = getFormatter(format);

        try {
            Date date = formatter.parse(dateToParse);
            return new DateTimeType(date, format.getPrecision(), getTimeZone(UTC));
        } catch (ParseException e) {
            throw new IllegalStateException(String.format(ERROR_MESSAGE, dateToParse), e);
        }
    }

    public static InstantType parseToInstantType(String dateToParse) {
        DateFormat format = getFormat(dateToParse);
        SimpleDateFormat formatter = getFormatter(format);

        try {
            Date date = formatter.parse(dateToParse);
            return new InstantType(date, MILLI, getTimeZone(UTC));
        } catch (ParseException e) {
            throw new IllegalStateException(String.format(ERROR_MESSAGE, dateToParse), e);
        }
    }

    public static Date parsePathwaysDate(String dateStr) {
        try {
            return Date.from(Instant.parse(dateStr));
        } catch (DateTimeParseException exc) {
            LocalDateTime parse = LocalDateTime.parse(dateStr);
            return Date.from(Instant.from(parse.atZone(UK_ZONE_ID)));
        }
    }

    public static String toHl7Format(Instant instant) {
        return instant.atZone(UK_ZONE_ID).format(HL7_SECONDS_COMPUTER_READABLE);
    }

    private DateFormat getFormat(String date) {
        if (containsOffset(date)) {
            return getFormatWithTimezone(date);
        }
        return getFormatWithoutTimezone(date);
    }

    private boolean containsOffset(String date) {
        return date.contains("+") || date.contains("-");
    }

    private String[] splitDateFromOffset(String date) {
        return date.contains("+") ? date.split("\\+") : date.split("-");
    }

    private DateFormat getFormatWithTimezone(String date) {
        String[] dateAndOffset = splitDateFromOffset(date);
        String datePart = dateAndOffset[0];
        String offsetPart = dateAndOffset[1];

        if (offsetPart.length() == 2) {
            return switch (datePart.length()) {
                case HOUR_PRECISION -> new DateFormat(DATETIME_TIMEZONE_HOURS_FORMAT, TemporalPrecisionEnum.SECOND);
                case MINUTE_PRECISION -> new DateFormat(DATETIME_TIMEZONE_MINUTES_FORMAT, TemporalPrecisionEnum.SECOND);
                case MILLISECOND_PRECISION -> new DateFormat(DATETIME_TIMEZONE_MILLISECONDS_FORMAT, MILLI);
                default -> new DateFormat(DATETIME_TIMEZONE_SECONDS_FORMAT, TemporalPrecisionEnum.SECOND);
            };
        }
        return switch (datePart.length()) {
            case HOUR_PRECISION -> new DateFormat(DATETIME_TIMEZONE_FULL_HOURS_FORMAT, TemporalPrecisionEnum.SECOND);
            case MINUTE_PRECISION -> new DateFormat(DATETIME_TIMEZONE_FULL_MINUTES_FORMAT, TemporalPrecisionEnum.SECOND);
            case MILLISECOND_PRECISION -> new DateFormat(DATETIME_TIMEZONE_FULL_MILLISECONDS_FORMAT, MILLI);
            default -> new DateFormat(DATETIME_TIMEZONE_FULL_FORMAT, TemporalPrecisionEnum.SECOND);
        };
    }

    private DateFormat getFormatWithoutTimezone(String date) {
        return switch (date.length()) {
            case YEAR_PRECISION -> new DateFormat(DATE_YEAR_FORMAT, TemporalPrecisionEnum.YEAR);
            case MONTH_PRECISION -> new DateFormat(DATE_MONTH_FORMAT, TemporalPrecisionEnum.MONTH);
            case DAY_PRECISION -> new DateFormat(DATE_FORMAT, TemporalPrecisionEnum.DAY);
            case HOUR_PRECISION -> new DateFormat(DATETIME_HOURS_FORMAT, TemporalPrecisionEnum.SECOND);
            case MINUTE_PRECISION -> new DateFormat(DATETIME_MINUTES_FORMAT, TemporalPrecisionEnum.SECOND);
            case MILLISECOND_PRECISION -> new DateFormat(DATETIME_MILLISECONDS_FORMAT, MILLI);
            default -> new DateFormat(DATETIME_SECONDS_FORMAT, TemporalPrecisionEnum.SECOND);
        };
    }

    private SimpleDateFormat getFormatter(DateFormat format) {
        String dateFormat = format.getDateFormat();
        if (dateFormat.equals(DATE_FORMAT) || dateFormat.equals(DATE_MONTH_FORMAT)) {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
            formatter.setTimeZone(getTimeZone(UTC));

            return formatter;
        }

        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        formatter.setTimeZone(getTimeZone(UK_ZONE_ID));

        return formatter;
    }
}
