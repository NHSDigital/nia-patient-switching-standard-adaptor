package uk.nhs.adaptors.pss.translator.util;

import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;

public class ResourceUtil {

    public static Reference buildResourceReference(Resource resource) {
        IdType idType = new IdType(resource.getResourceType().name(), resource.getId());
        return new Reference(idType);
    }

    public static Extension buildReferenceExtension(String url, Reference reference) {
        Extension extension = new Extension();
        extension.setUrl(url);
        extension.setValue(reference);
        return extension;
    }

}
