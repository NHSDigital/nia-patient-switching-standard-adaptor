package uk.nhs.adaptors.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Extension;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CodeableConceptUtils {

    /**
     *
     * @param system holds the SNOMED CT system identifier (http://snomed.info/sct)
     * @param code holds the SNOMED CT concept identifier
     * @param display holds the SNOMED CT concept display
     * @param  text represents the text that was originally displayed to the user when the code was recorded
     *
     * @return CodeableConcept object with set code, system, display and text properties
     */
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

    public static CodeableConcept createCodeableConceptWithDoubleCoding(String code, String system, String display,
                                                                        String text, String gp2gpSpecificCode) {
        var codeableConcept = new CodeableConcept().setText(text);
        var coding = new Coding(system, code, display);
        var codingWithGP2GPSpecificCode = new Coding("urn:oid:2.16.840.1.113883.2.1.3.2.4.17.101", gp2gpSpecificCode, display);
        codeableConcept.setCoding(List.of(coding, codingWithGP2GPSpecificCode));

        return codeableConcept;
    }

    /**
     *
     * @param system holds the SNOMED CT system identifier (http://snomed.info/sct)
     * @param code holds the SNOMED CT concept identifier
     * @param display holds the SNOMED CT concept display
     *
     * @return CodeableConcept object with set code, system and display properties
     */
    public static CodeableConcept createCodeableConcept(String system, String code, String display) {
        var codeableConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setSystem(system);
        coding.setCode(code);
        coding.setDisplay(display);

        codeableConcept.addCoding(coding);
        return codeableConcept;
    }

    /**
     *
     * @param code holds the SNOMED CT concept identifier
     * @param system holds the SNOMED CT system identifier (http://snomed.info/sct)
     * @param display holds the SNOMED CT concept display
     * @param  text represents the text that was originally displayed to the user when the code was recorded
     * @param extension is he wrapper element for the SNOMED description extension
     *
     * @return CodeableConcept object with set code, system and display, text and extension properties
     */
    public static CodeableConcept createCodeableConcept(String code, String system, String display,
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
