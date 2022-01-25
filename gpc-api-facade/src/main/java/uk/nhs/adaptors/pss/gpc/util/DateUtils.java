package uk.nhs.adaptors.pss.gpc.util;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;

@Service
public class DateUtils {
    public OffsetDateTime getCurrentOffsetDateTime() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
