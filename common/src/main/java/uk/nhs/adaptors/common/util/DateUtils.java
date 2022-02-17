package uk.nhs.adaptors.common.util;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;

@Service
public class DateUtils {
    public OffsetDateTime getCurrentOffsetDateTime() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
    public Instant getCurrentInstant() {
        return getCurrentOffsetDateTime().toInstant();
    }
}
