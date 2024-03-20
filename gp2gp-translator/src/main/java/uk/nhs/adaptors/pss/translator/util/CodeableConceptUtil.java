package uk.nhs.adaptors.pss.translator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hl7.v3.CD;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CodeableConceptUtil {

    public static Boolean compareCodeableConcepts(CD c1, CD c2) {
        return c1.getCode().equals(c2.getCode())
               && c1.getCodeSystem().equals(c2.getCodeSystem())
               && c1.getDisplayName().equals(c2.getDisplayName())
               && c1.getOriginalText().equals(c2.getOriginalText())
               && compareCodeSubelements(c1.getQualifier(), c2.getQualifier())
               && compareCodeSubelements(c1.getTranslation(), c2.getTranslation());
    }

    private static Boolean compareCodeSubelements(List<? extends CD> c1, List<? extends CD> c2) {

        boolean itemsMatch = Boolean.FALSE;

        if ((c1 == null && c2 == null) || (c1.isEmpty() && c2.isEmpty())) {
            return Boolean.TRUE;
        }

        if (c1 != null && c2 != null && c1.size() == c2.size()) {

            for (CD c2Element : c2) {
                for (CD c1Element : c1) {
                    if (c1Element.getCode().equals(c2Element.getCode())
                        && c1Element.getCodeSystem().equals(c2Element.getCodeSystem())
                        && c1Element.getDisplayName().equals(c2Element.getDisplayName())) {
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
