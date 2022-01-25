package uk.nhs.adaptors.pss.translator.utils;

import static java.time.ZoneOffset.UTC;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class IsoDateTimeFormatter extends SimpleDateFormat {

    private static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    private IsoDateTimeFormatter() {
        super(ISO_DATE_TIME_FORMAT);
        setTimeZone(TimeZone.getTimeZone(UTC));
    }

    public static String toIsoDateTimeString(Date date) {
        return new IsoDateTimeFormatter().format(date);
    }
}