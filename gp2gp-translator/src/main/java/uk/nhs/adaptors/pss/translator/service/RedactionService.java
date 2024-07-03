package uk.nhs.adaptors.pss.translator.service;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.v3.CV;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class RedactionService {

    private static final Coding CONFIDENTIALITY_CODING = new Coding()
        .setSystem("http://hl7.org/fhir/v3/ActCode")
        .setCode("NOPAT")
        .setDisplay("no disclosure to patient, family or caregivers without attending provider's authorization");

    @SafeVarargs
    public final Meta processRedactions(Supplier<Meta> metaSupplier, Optional<CV>... confidentialityCodes) {
        final Collection<Optional<CV>> codes = Arrays.asList(confidentialityCodes);

        if(isNopatConfidentialityCodePresent(codes)) {
            return setMetaSecurity(metaSupplier.get());
        }

        return metaSupplier.get();
    }

    private Meta setMetaSecurity(Meta initialMeta) {
        return initialMeta.setSecurity(
            Collections.singletonList(
                CONFIDENTIALITY_CODING
            )
        );
    }

    private boolean isNopatConfidentialityCodePresent(final Collection<Optional<CV>> codes) {
        return codes.stream()
            .filter(Optional::isPresent)
            .map(Optional::get)
            .anyMatch(this::isNopat);
    }

    private boolean isNopat(CV code) {
        return code.getCode().equals("NOPAT");
    }
}