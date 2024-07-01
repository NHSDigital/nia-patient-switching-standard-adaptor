package uk.nhs.adaptors.pss.translator.util;

import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.v3.RCMRMT030101UKEhrComposition;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceUtil {

    private static final String META_PROFILE_TEMPLATE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-%s";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/%s";

    public static Meta generateMeta(String urlProfile) {
        Meta meta = new Meta();
        UriType profile = new UriType(String.format(META_PROFILE_TEMPLATE, urlProfile));
        meta.setProfile(List.of(profile));
        return meta;
    }

    public static Meta generateMetaWithSecurity(String urlProfile) {
        return generateMeta(urlProfile)
            .setSecurity(Collections.singletonList(new Coding()
                .setSystem("http://hl7.org/fhir/v3/ActCode")
                .setCode("NOPAT")
                .setDisplay("no disclosure to patient, family or caregivers without attending provider's authorization")));
    }

    public static Identifier buildIdentifier(String rootId, String practiseCode) {
        Identifier identifier = new Identifier();
        identifier.setSystem(IDENTIFIER_SYSTEM.formatted(practiseCode));
        identifier.setValue(rootId);

        return identifier;
    }

    public static Extension buildReferenceExtension(String url, Reference reference) {
        return new Extension(url, reference);
    }

    public static void addContextToObservation(Observation observation, List<Encounter> encounters,
        RCMRMT030101UKEhrComposition ehrComposition) {

        encounters.stream()
            .filter(encounter -> encounter.getId().equals(ehrComposition.getId().getRoot()))
            .findFirst()
            .map(Reference::new)
            .ifPresent(observation::setContext);
    }

}
