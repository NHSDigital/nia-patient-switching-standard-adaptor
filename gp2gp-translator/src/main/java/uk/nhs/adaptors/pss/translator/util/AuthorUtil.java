package uk.nhs.adaptors.pss.translator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.RCMRMT030101UKAuthor;
import org.hl7.v3.RCMRMT030101UKEhrComposition;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthorUtil {

    private static final String PRACTITIONER_REFERENCE_PREFIX = "Practitioner/%s";

    public static Optional<Reference> getAuthorReference(RCMRMT030101UKEhrComposition ehrComposition) {

        Optional<RCMRMT030101UKAuthor> author = Optional.ofNullable(ehrComposition.getAuthor());
        return author.map(a -> new Reference(PRACTITIONER_REFERENCE_PREFIX.formatted(a.getAgentRef().getId().getRoot())));
    }

}
