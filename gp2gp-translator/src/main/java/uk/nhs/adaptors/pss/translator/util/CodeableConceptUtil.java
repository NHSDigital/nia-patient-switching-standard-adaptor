package uk.nhs.adaptors.pss.translator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hl7.v3.CD;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CodeableConceptUtil {

    public static Boolean compareCodeableConcepts(CD c1, CD c2) {

        var c1Code = c1.getCode();
        var c2Code = c2.getCode();
        var c1CodeSystem = c1.getCodeSystem();
        var c2CodeSystem = c2.getCodeSystem();
        var c1DisplayName = c1.getDisplayName();
        var c2DisplayName = c2.getDisplayName();
        var c1OriginalText = c1.getOriginalText();
        var c2OriginalText = c2.getOriginalText();
        var c1Qualifier = c1.getQualifier();
        var c2Qualifier = c2.getQualifier();
        var c1Translation = c1.getTranslation();
        var c2Translation = c2.getTranslation();



        if (c1 == null && c2 == null) {
            return Boolean.TRUE;
        }

        if ((c1Code == null && c2Code != null) || c1Code != null && c2Code == null) {
            return Boolean.FALSE;
        }

        if ((c1CodeSystem == null && c2CodeSystem != null) || c1CodeSystem != null && c2CodeSystem == null) {
            return Boolean.FALSE;
        }

        if ((c1DisplayName == null && c2DisplayName != null) || c1DisplayName != null && c2DisplayName == null) {
            return Boolean.FALSE;
        }

        if ((c1OriginalText == null && c2OriginalText != null) || c1OriginalText != null && c2OriginalText == null) {
            return Boolean.FALSE;
        }

        if ((c1Qualifier == null && c2Qualifier != null) || c1Qualifier != null && c2Qualifier == null) {
            return Boolean.FALSE;
        }

        if ((c1Translation == null && c2Translation != null) || c1Translation != null && c2Translation == null) {
            return Boolean.FALSE;
        }

        return (c1Code == null && c2Code == null) || c1Code.equals(c2Code)
               && (c1CodeSystem == null && c2CodeSystem == null || c1CodeSystem.equals(c2CodeSystem))
               && (c1DisplayName == null && c2DisplayName == null || c1DisplayName.equals(c2DisplayName))
               && (c1OriginalText == null && c2OriginalText == null || c1OriginalText.equals(c2OriginalText))
               && (c1Qualifier == null && c2Qualifier == null || compareCodeSubelements(c1Qualifier, c2Qualifier))
               && (c1Translation == null && c2Translation == null || compareCodeSubelements(c1Translation, c2Translation));
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
