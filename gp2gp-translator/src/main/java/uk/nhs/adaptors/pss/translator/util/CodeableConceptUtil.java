package uk.nhs.adaptors.pss.translator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hl7.v3.CD;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CodeableConceptUtil {

    public static Boolean compareCodeableConcepts(CD c1, CD c2) {

        if (c1 == null || c2 == null) {
            return c1 == c2;
        }

        return Objects.equals(c1.getCode(), c2.getCode())
                && Objects.equals(c1.getCodeSystem(), c2.getCodeSystem())
                && Objects.equals(c1.getDisplayName(), c2.getDisplayName())
                && Objects.equals(c1.getOriginalText(), c2.getOriginalText())
                && compareCodeSubElements(c1.getQualifier(), c2.getQualifier())
                && compareCodeSubElements(c1.getTranslation(), c2.getTranslation());
    }

    private static Boolean compareCodeSubElements(List<? extends CD> c1, List<? extends CD> c2) {
        if (c1 == null || c2 == null) {
            return c1 == c2;
        }

        if (c1.isEmpty() && c2.isEmpty()) {
            return true;
        }

        if (c1.size() != c2.size()) {
            return false;
        }

        for (CD c1Element : c1) {
            boolean matchFound = c2
                    .stream()
                    .anyMatch(c2Element -> compareCodeableConcepts(c1Element, c2Element));

            if (!matchFound) {
                return false;
            }
        }

        return true;
    }

}
