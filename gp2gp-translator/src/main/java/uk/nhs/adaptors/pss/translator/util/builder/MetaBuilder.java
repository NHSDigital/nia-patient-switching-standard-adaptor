package uk.nhs.adaptors.pss.translator.util.builder;

import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.v3.CV;
import uk.nhs.adaptors.pss.translator.util.ConfidentialityUtil;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public final class MetaBuilder {
    private Meta meta;

    public MetaBuilder withInitialMeta(final Supplier<Meta> metaSupplier) {
        this.meta = Objects.requireNonNull(metaSupplier.get());
        return this;
    }

    @SafeVarargs
    public final MetaBuilder withSecurityIfConfidentialityCodesPresent(Optional<CV>... confidentialityCodes) {
        this.meta = ConfidentialityUtil.addSecurityToMetaIfConfidentialityCodesPresent(
            Arrays.asList(confidentialityCodes),
            this.meta
        );

        return this;
    }

    public Meta build() {
        return this.meta.copy();
    }
}