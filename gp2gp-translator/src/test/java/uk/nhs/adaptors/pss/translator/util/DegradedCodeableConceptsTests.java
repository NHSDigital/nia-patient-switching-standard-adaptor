package uk.nhs.adaptors.pss.translator.util;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DegradedCodeableConceptsTests {

    private static final String SNOMED_SYSTEM = "http://snomed.info/sct";

    @Test
    public void When_CodingContainsSnomedSystem_Expect_DegradedCodingNotAdded() {
        var snomedCoding = new Coding().setSystem(SNOMED_SYSTEM);
        var codingArray = new ArrayList<>(List.of(snomedCoding));
        var codeableConcept = new CodeableConcept().setCoding(codingArray);

        DegradedCodeableConcepts.addDegradedEntryIfRequired(codeableConcept, DegradedCodeableConcepts.DEGRADED_PLAN);

        assertThat(codeableConcept.getCoding()).hasSize(1);
        assertThat(codeableConcept.getCodingFirstRep()).isEqualTo(snomedCoding);
    }

    @Test
    public void When_CodingNotPresent_Expect_DegradedCodingAdded() {
        var codeableConcept = new CodeableConcept();

        DegradedCodeableConcepts.addDegradedEntryIfRequired(codeableConcept, DegradedCodeableConcepts.DEGRADED_PLAN);

        assertThat(codeableConcept.getCoding()).hasSize(1);
        assertThat(codeableConcept.getCodingFirstRep()).isEqualTo(DegradedCodeableConcepts.DEGRADED_PLAN);
    }

    @Test
    public void When_CodingContainsNonSnomedItem_Expect_DegradedCodingAddedAsCodingFirstRep() {
        var nonSnomedCoding = new Coding().setDisplay("test-display");
        var codingArray = new ArrayList<>(List.of(nonSnomedCoding));
        var codeableConcept = new CodeableConcept().setCoding(codingArray);

        DegradedCodeableConcepts.addDegradedEntryIfRequired(codeableConcept, DegradedCodeableConcepts.DEGRADED_PLAN);

        assertThat(codeableConcept.getCoding()).hasSize(2);
        assertThat(codeableConcept.getCodingFirstRep()).isEqualTo(DegradedCodeableConcepts.DEGRADED_PLAN);
        assertThat(codeableConcept.getCoding().get(1)).isEqualTo(nonSnomedCoding);
    }
}
