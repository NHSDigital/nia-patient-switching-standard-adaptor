package uk.nhs.adaptors.pss.translator;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.UriType;

import java.util.Collections;

public final class MetaFactory {
    private MetaFactory() { }
    private static final Coding NOPAT_CODING = new Coding()
        .setSystem("http://hl7.org/fhir/v3/ActCode")
        .setCode("NOPAT")
        .setDisplay("no disclosure to patient, family or caregivers without attending provider's authorization");

    public static Meta getMetaFor(final MetaType metaType, final String profile) {
        return switch (metaType) {
            case META_WITHOUT_SECURITY -> getBaseMeta(profile);
            case META_WITH_SECURITY -> getMetaWithSecurity(profile);
        };
    }

    private static Meta getBaseMeta(final String profile) {
        return new Meta().setProfile(
            Collections.singletonList(
                new UriType(profile)
            )
        );
    }

    private static Meta getMetaWithSecurity(final String profile) {
        return getBaseMeta(profile).addSecurity(NOPAT_CODING);
    }

    public enum MetaType {
        META_WITH_SECURITY,
        META_WITHOUT_SECURITY
    }
}