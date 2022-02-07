package uk.nhs.adaptors.pss.translator.util;

import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Reference;

public class ExtensionUtil {

    public static Extension buildReferenceExtension(String url, Reference reference) {
        Extension extension = new Extension();
        extension.setUrl(url);
        extension.setValue(reference);
        return extension;
    }
}
