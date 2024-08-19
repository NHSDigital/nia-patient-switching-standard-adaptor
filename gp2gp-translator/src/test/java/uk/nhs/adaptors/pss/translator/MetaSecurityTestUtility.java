package uk.nhs.adaptors.pss.translator;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class MetaSecurityTestUtility {

    public static void assertMetaSecurityIsPresent(Meta initialMetaWithSecurityAdded, final Meta meta) {
        final List<Coding> metaSecurity = meta.getSecurity();
        final int metaSecuritySize = metaSecurity.size();

        assertAll(
            () -> assertThat(metaSecuritySize).isEqualTo(1),
            () -> assertThat(meta).usingRecursiveComparison().isEqualTo(initialMetaWithSecurityAdded)
        );
    }

    public static void assertMetaSecurityNotPresent(final ProcedureRequest request, String metaProfile) {
        final Meta meta = request.getMeta();

        assertMetaSecurityIsNotPresent(meta, metaProfile);
    }

    public static void assertMetaSecurityIsNotPresent(final Meta meta, String metaProfile) {
        assertAll(
            () -> assertThat(meta.getSecurity()).isEmpty(),
            () -> assertThat(meta.getProfile().getFirst().getValue()).isEqualTo(metaProfile)
        );
    }

}
