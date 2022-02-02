package uk.nhs.adaptors.pss.translator.mapper;

import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.InstantType;

import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

public class DateTimeMapper {

    public DateTimeType mapDateTime(String dateToMap) {

        return DateFormatUtil.parse(dateToMap);
    }

    public InstantType mapDateTimeInstant(String dateToMap) {

        return DateFormatUtil.parseToInstantType(dateToMap);
    }

}
