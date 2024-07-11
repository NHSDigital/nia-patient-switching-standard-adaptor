package uk.nhs.adaptors.pss.translator.service;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.v3.CV;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

@Service
public class ConfidentialityService {
    private static final Coding CONFIDENTIALITY_CODING = new Coding()
        .setSystem("http://hl7.org/fhir/v3/ActCode")
        .setCode("NOPAT")
        .setDisplay("no disclosure to patient, family or caregivers without attending provider's authorization");

    public Meta createMetaAndAddSecurityIfConfidentialityCodesPresent(Collection<Optional<CV>> confidentialityCodes, String metaProfile) {
        final Meta meta = generateMeta(metaProfile);
        final boolean isCodePresent = confidentialityCodes.stream()
            .filter(Optional::isPresent)
            .map(Optional::get)
            .anyMatch(this::isNopat);

        if (isCodePresent) {
            return addConfidentialityToMeta(meta);
        }

        return meta;
    }

    private Meta addConfidentialityToMeta(final Meta meta) {
        return meta.setSecurity(
            Collections.singletonList(
                CONFIDENTIALITY_CODING
            )
        );
    }

    private boolean isNopat(CV coding) {
        return coding.getCode().equals("NOPAT");
    }
}