package uk.nhs.adaptors.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Extension;

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

    public static CodeableConcept createCodeableConceptWithCoding(String system, String code, String display) {
        var codeableConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setSystem(system);
        coding.setCode(code);
        coding.setDisplay(display);

        codeableConcept.addCoding(coding);
        return codeableConcept;
    }


    public static CodeableConcept createCodeableConceptWithExtension(String code, String system, String display,
                                                                     String text, Extension extension) {
        var codeableConcept = new CodeableConcept();
        codeableConcept
            .setText(text)
            .getCodingFirstRep()
            .setCode(code)
            .setSystem(system)
            .setDisplay(display)
            .addExtension(extension);

        return codeableConcept;
    }

}
