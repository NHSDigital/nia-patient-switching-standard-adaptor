package uk.nhs.adaptors.pss.translator.util;

import org.hl7.v3.RCMRMT030101UK04NarrativeStatement;

public class ResourceFilterUtil {

    public static boolean hasReferredToExternalDocument(RCMRMT030101UK04NarrativeStatement narrativeStatement) {
        return narrativeStatement.getReference()
            .stream()
            .anyMatch(reference -> reference.getReferredToExternalDocument() != null);
    }
}
