package uk.nhs.adaptors.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hl7.fhir.dstu3.model.CodeableConcept;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
