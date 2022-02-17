package uk.nhs.adaptors.pss.translator.generator;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.UriType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.service.IdGeneratorService;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BundleGenerator {

    private final IdGeneratorService idGeneratorService;

    public Bundle generateBundle() {
        Bundle bundle = new Bundle();
        Meta meta = new Meta();
        meta.setProfile(List.of(new UriType("https://fhir.nhs.uk/STU3/StructureDefinition/GPConnect-StructuredRecord-Bundle-1")));

        bundle.setId(idGeneratorService.generateUuid());
        bundle.setMeta(meta);
        bundle.setType(Bundle.BundleType.COLLECTION);

        return bundle;
    }
}
