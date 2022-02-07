package uk.nhs.adaptors.pss.translator.mapper;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.v3.CD;
import org.springframework.stereotype.Service;

@Service
public class CodeableConceptMapper {
    private static final String SNOMED_SYSTEM_CODE = "2.16.840.1.113883.2.1.3.2.4.15";
    private static final String SNOMED_SYSTEM = "http://snomed.info/sct";

    public CodeableConcept mapToCodeableConcept(CD codedData) {
        if (hasSnomedCodeInMainCode(codedData)) {
            return createCodeableConcept(codedData.getCode(), SNOMED_SYSTEM, codedData.getDisplayName(), codedData.getOriginalText());
        } else if (hasSnomedCodeInTranslationElement(codedData)) {
            var translation = getSnomedTranslationElement(codedData);
            var display = getDisplayForSnomedTranslationElement(codedData, translation);
            return createCodeableConcept(translation.getCode(), SNOMED_SYSTEM, display, codedData.getOriginalText());
        }

        var text = getText(codedData);
        return new CodeableConcept().setText(text);
    }

    private boolean hasSnomedCodeInMainCode(CD codedData) {
        return codedData.getCodeSystem().equals(SNOMED_SYSTEM_CODE);
    }

    private boolean hasSnomedCodeInTranslationElement(CD codedData) {
        return codedData.getTranslation()
            .stream()
            .anyMatch(translation -> translation.getCodeSystem().equals(SNOMED_SYSTEM_CODE));
    }

    private CD getSnomedTranslationElement(CD codedData) {
        return codedData.getTranslation()
            .stream()
            .filter(translation -> translation.getCodeSystem().equals(SNOMED_SYSTEM_CODE))
            .findFirst()
            .get();
    }

    private String getText(CD codedData) {
        return codedData.getOriginalText() != null ? codedData.getOriginalText() : codedData.getDisplayName();
    }

    private String getDisplayForSnomedTranslationElement(CD codedData, CD translation) {
        return translation.getDisplayName() != null ? translation.getDisplayName() : codedData.getDisplayName();
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
