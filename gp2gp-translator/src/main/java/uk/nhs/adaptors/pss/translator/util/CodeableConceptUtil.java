package uk.nhs.adaptors.pss.translator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hl7.v3.CD;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CodeableConceptUtil {

    public static Boolean compareCodeableConcepts(CD c1, CD c2) {

        if (c1 == null && c2 == null) {
            return Boolean.TRUE;
        }

        if (c1 == null || c2 == null) {
            return Boolean.FALSE;
        }

        var c1Qualifier = c1.getQualifier();
        var c2Qualifier = c2.getQualifier();
        var c1Translation = c1.getTranslation();
        var c2Translation = c2.getTranslation();

        return  Objects.equals(c1.getCode(), c2.getCode())
                 && Objects.equals(c1.getCodeSystem(), c2.getCodeSystem())
                 && Objects.equals(c1.getDisplayName(), c2.getDisplayName())
                 && Objects.equals(c1.getOriginalText(), c2.getOriginalText())
                 && (c1Qualifier == null && c2Qualifier == null || compareCodeSubElements(c1Qualifier, c2Qualifier))
                 && (c1Translation == null && c2Translation == null || compareCodeSubElements(c1Translation, c2Translation));
    }

    private static Boolean compareCodeSubElements(List<? extends CD> c1, List<? extends CD> c2) {

        boolean itemsMatch = Boolean.FALSE;

        if ((c1 == null && c2 == null) || (c1.isEmpty() && c2.isEmpty())) {
            return Boolean.TRUE;
        }

        if (c1 != null && c2 != null && c1.size() == c2.size()) {

            for (CD c1Element : c1) {
                for (CD c2Element : c2) {
                    if (compareCodeableConcepts(c1Element, c2Element)) {
                        itemsMatch = true;
                        break;
                    } else {
                        itemsMatch = false;
                    }
                }

                if (!itemsMatch) {
                    return itemsMatch;
                }
            }
        }

        return itemsMatch;
    }

}
