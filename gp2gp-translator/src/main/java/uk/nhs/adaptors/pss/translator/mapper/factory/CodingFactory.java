package uk.nhs.adaptors.pss.translator.mapper.factory;

import org.hl7.fhir.dstu3.model.Coding;

public final class CodingFactory {
    private CodingFactory() { }

    public static Coding getCodingFor(final CodingType codingType) {
        return switch (codingType) {
            case META_SECURITY -> getMetaSecurityCoding();
        };
    }

    /**
     * Creates the coding for redactions support (confidentialityCode).
     * @return Coding
     */
    private static Coding getMetaSecurityCoding() {
        return new Coding()
            .setSystem("http://hl7.org/fhir/v3/ActCode")
            .setCode("NOPAT")
            .setDisplay("no disclosure to patient, family or caregivers without attending provider's authorization");
    }

    public enum CodingType {
        META_SECURITY
    }
}