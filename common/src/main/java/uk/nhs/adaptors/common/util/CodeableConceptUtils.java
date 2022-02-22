package uk.nhs.adaptors.common.util;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;

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

    public static CodeableConcept getCodeableConceptWithCoding(String system, String code, String display) {
        Coding coding = new Coding(system, code, display);
        return new CodeableConcept(coding);
    }
}
