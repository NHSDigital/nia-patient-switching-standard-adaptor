package uk.nhs.adaptors.pss.gpc.util;

import org.hl7.fhir.dstu3.model.CodeableConcept;

public class CodeableConceptUtils {
    public static CodeableConcept createCodeableConcept(String code, String system, String display, String text) {
        var codeableConcept = new CodeableConcept();
        codeableConcept
            .setText(text)
            .getCodingFirstRep()
            .setCode(code)
            .setSystem(system)
            .setDisplay(display);
        return codeableConcept;
    }
}
