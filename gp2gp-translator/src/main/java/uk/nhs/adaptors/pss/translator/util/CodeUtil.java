package uk.nhs.adaptors.pss.translator.util;

import java.util.Optional;

import org.hl7.v3.CD;

public class CodeUtil {

    private final static String SNOMED_CODE_SYSTEM = "2.16.840.1.113883.2.1.3.2.4.15";

    public static Optional<String> extractSnomedCode(CD cd) {

        if (cd.hasCode() && cd.hasCodeSystem() && cd.getCodeSystem().equals(SNOMED_CODE_SYSTEM)) {
            return Optional.of(cd.getCode());
        }

        if (!cd.getTranslation().isEmpty()) {
           return cd.getTranslation().stream()
               .filter(translation -> translation.hasCode() && translation.hasCodeSystem())
               .filter(translation -> translation.getCodeSystem().equals(SNOMED_CODE_SYSTEM))
               .map(CD::getCode)
               .findFirst();
        }

        return Optional.empty();
    }
}
