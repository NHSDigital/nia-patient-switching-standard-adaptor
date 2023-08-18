package uk.nhs.adaptors.pss.translator.util;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;

import java.util.List;

public final class DegradedCodeableConcepts {

    private static final String SNOMED_SYSTEM = "http://snomed.info/sct";

    public static final Coding DEGRADED_DRUG_ALLERGY = new Coding()
            .setSystem(SNOMED_SYSTEM)
            .setCode("196461000000101")
            .setDisplay("Transfer-degraded drug allergy");

    public static final Coding DEGRADED_NON_DRUG_ALLERGY = new Coding()
            .setSystem(SNOMED_SYSTEM)
            .setCode("196471000000108")
            .setDisplay("Transfer-degraded non-drug allergy");

    public static final Coding DEGRADED_MEDICATION = new Coding()
            .setSystem(SNOMED_SYSTEM)
            .setCode("196421000000109")
            .setDisplay("Transfer-degraded medication entry");

    public static final Coding DEGRADED_PLAN = new Coding()
            .setSystem(SNOMED_SYSTEM)
            .setCode("196451000000104")
            .setDisplay("Transfer-degraded plan");

    public static final Coding DEGRADED_REFERRAL = new Coding()
            .setSystem(SNOMED_SYSTEM)
            .setCode("196431000000106")
            .setDisplay("Transfer-degraded referral");

    public static final Coding DEGRADED_REQUEST = new Coding()
            .setSystem(SNOMED_SYSTEM)
            .setCode("196441000000102")
            .setDisplay("Transfer-degraded request");

    public static final Coding DEGRADED_OTHER = new Coding()
            .setSystem(SNOMED_SYSTEM)
            .setCode("196411000000103")
            .setDisplay("Transfer-degraded record entry");

    public static void addDegradedEntry(CodeableConcept codeableConcept, Coding degradedCoding) {
        if (codeableConcept.hasCoding()) {
            var coding = codeableConcept.getCoding();
            var hasSnomedCode = coding
                 .stream()
                 .anyMatch(cc -> SNOMED_SYSTEM.equals(cc.getSystem()));

            if (!hasSnomedCode) {
                coding.add(0, degradedCoding);
            }
        } else {
            codeableConcept
                .setCoding(List.of(degradedCoding));
        }
    }
}
