package uk.nhs.adaptors.connector.util;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;

// TODO this class will be moved to common module in NIA-1881
@Service
public class DateUtils {
    public OffsetDateTime getCurrentOffsetDateTime() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
