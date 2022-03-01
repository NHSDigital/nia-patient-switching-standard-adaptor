package uk.nhs.adaptors.pss.translator.util;

import java.util.List;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.UriType;

public class ResourceUtil {

    private static final String META_PROFILE_TEMPLATE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-%s";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/{practiseCode}";
    private static final String PRACTISE_CODE = "{practiseCode}";

    public static Meta generateMeta(String urlProfile) {
        Meta meta = new Meta();
        UriType profile = new UriType(String.format(META_PROFILE_TEMPLATE, urlProfile));
        meta.setProfile(List.of(profile));
        return meta;
    }

    public static Identifier buildIdentifier(String rootId, String practiseCode) {
        Identifier identifier = new Identifier();
        identifier.setSystem(IDENTIFIER_SYSTEM.replace(PRACTISE_CODE, practiseCode));
        identifier.setValue(rootId);

        return identifier;
    }

}
