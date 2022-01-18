package uk.nhs.adaptors.pss.translator.generator;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.UriType;

import org.springframework.stereotype.Component;

import uk.nhs.adaptors.pss.translator.service.FhirIDGeneratorService;

@Component
public class BundleGenerator {

    private static final FhirIDGeneratorService FHIR_ID = new FhirIDGeneratorService();

    private Bundle bundleGenerator() {

        Bundle bundle = new Bundle();
        Meta meta = new Meta();
        meta.setProfile(List.of(new UriType("https://fhir.nhs.uk/STU3/StructureDefinition/GPConnect-StructuredRecord-Bundle-1")));

        bundle.setId(FHIR_ID.getFHIR_ID());
        bundle.setMeta(meta);
        bundle.setType(Bundle.BundleType.COLLECTION);

        return bundle;

    }
}


