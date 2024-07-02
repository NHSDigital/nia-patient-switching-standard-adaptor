package uk.nhs.adaptors.pss.translator.util;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.v3.CV;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public final class ConfidentialityUtil {
    private ConfidentialityUtil() { }
    private static final Coding CONFIDENTIALITY_CODING = new Coding()
        .setSystem("http://hl7.org/fhir/v3/ActCode")
        .setCode("NOPAT")
        .setDisplay("no disclosure to patient, family or caregivers without attending provider's authorization");

    public static Meta addSecurityToMetaIfConfidentialityCodesPresent(Collection<Optional<CV>> confidentialityCodes, Meta meta) {
        final Collection<Boolean> values = confidentialityCodes.stream()
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(ConfidentialityUtil::isNopat)
            .toList();

        if(!values.isEmpty()) {
            return ConfidentialityUtil.addConfidentialityToMeta(meta);
        }

        return meta;
    }

    private static Meta addConfidentialityToMeta(final Meta meta) {
        return meta.setSecurity(
            Collections.singletonList(
                CONFIDENTIALITY_CODING
            )
        );
    }

    private static boolean isNopat(CV coding) {
        return coding.getCode().equals("NOPAT");
    }
}