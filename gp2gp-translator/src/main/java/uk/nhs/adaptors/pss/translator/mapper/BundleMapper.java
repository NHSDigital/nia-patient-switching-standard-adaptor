package uk.nhs.adaptors.pss.translator.mapper;

import java.util.List;
import java.util.UUID;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.UriType;

public class BundleMapper {

    private static void bundleMapper() {

        Bundle bundle = new Bundle();
        Meta meta = new Meta();
        meta.setProfile(List.of(new UriType("https://fhir.nhs.uk/STU3/StructureDefinition/GPConnect-StructuredRecord-Bundle-1")));

        bundle.setId(UUID.randomUUID().toString());
        bundle.setMeta(meta);
        bundle.setType(Bundle.BundleType.COLLECTION);
        bundle.setEntry(List.of(new Bundle.BundleEntryComponent()));

    }
}


