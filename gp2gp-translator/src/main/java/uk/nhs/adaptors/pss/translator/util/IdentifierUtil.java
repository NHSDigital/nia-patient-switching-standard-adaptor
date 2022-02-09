package uk.nhs.adaptors.pss.translator.util;

import org.hl7.fhir.dstu3.model.Identifier;


public class IdentifierUtil {
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";

    public static Identifier getIdentifier(String id) {
        Identifier identifier = new Identifier()
            .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL (NIAD-2021)
            .setValue(id);

        return identifier;
    }
}
