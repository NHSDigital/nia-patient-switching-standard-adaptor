package uk.nhs.adaptors.pss.translator.utils;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import lombok.Getter;

@Getter
public class DateFormat {
    private final String dateFormat;
    private final TemporalPrecisionEnum precision;

    public DateFormat(String dateFormat, TemporalPrecisionEnum precision) {
        this.dateFormat = dateFormat;
        this.precision = precision;
    }
}
