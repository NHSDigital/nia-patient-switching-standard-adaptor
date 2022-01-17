package uk.nhs.adaptors.pss.translator.mapper;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.v3.CD;
import org.hl7.v3.CV;

public class CodeableConceptMapper {
    private static final String SNOMED_SYSTEM_CODE = "2.16.840.1.113883.2.1.3.2.4.15";
    private static final String SNOMED_SYSTEM = "http://snomed.info/sct";

    public CodeableConcept mapToCodeableConcept(CV codedData) {
        if (hasSnomedCodeInMainCode(codedData)) {
            var text = getText(codedData);
            return createCodeableConcept(codedData.getCode(), SNOMED_SYSTEM, codedData.getDisplayName(), text);
        } else if (hasSnomedCodeInTranslationElement(codedData)) {
            var translation = getSnomedTranslationElement(codedData);
            var text = getTextForSnomedTranslationElement(codedData, translation)
            return createCodeableConcept(translation.getCode(), SNOMED_SYSTEM, translation.getDisplayName(), text);
        } else {
            // todo co z tym originaltextem
            var text = getText(codedData);
            return createCodeableConcept(codedData.getCode(), codedData.getCodeSystem(), codedData.getDisplayName(), text);
        }
    }

    private boolean hasSnomedCodeInMainCode(CV codedData) {
        return codedData.getCodeSystem().equals(SNOMED_SYSTEM_CODE);
    }

    private boolean hasSnomedCodeInTranslationElement(CV codedData) {
        return codedData.getTranslation()
            .stream()
            .anyMatch(t -> t.getCodeSystem().equals(SNOMED_SYSTEM_CODE));
    }

    private CD getSnomedTranslationElement(CV codedData) {
        return codedData.getTranslation()
            .stream()
            .filter(t -> t.getCodeSystem().equals(SNOMED_SYSTEM_CODE))
            .findFirst()
            .get();
    }

    private String getText(CV codedData) {
        return codedData.getOriginalText() != null ? codedData.getOriginalText().toString() : codedData.getDisplayName();
    }

    private String getTextForSnomedTranslationElement(CV codedData, CD translation) {
        // todo
    }

    private CodeableConcept createCodeableConcept(String code, String system, String display, String text) {
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
