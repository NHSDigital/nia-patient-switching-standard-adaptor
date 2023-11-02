package uk.nhs.adaptors.pss.translator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CodeSystemsUtil {

    private static final Map<String, String> SYSTEM_CODES = Map.of(
            "2.16.840.1.113883.2.1.3.2.4.15", "http://snomed.info/sct",
            "2.16.840.1.113883.2.1.6.3", "https://fhir.hl7.org.uk/Id/egton-codes",
            "2.16.840.1.113883.2.1.6.2", "http://read.info/readv2",
            "2.16.840.1.113883.2.1.3.2.4.14", "http://read.info/ctv3"
    );

    public static String getFhirCodeSystem(String hl7code) {
        return SYSTEM_CODES.getOrDefault(hl7code, hl7code);
    }
}
