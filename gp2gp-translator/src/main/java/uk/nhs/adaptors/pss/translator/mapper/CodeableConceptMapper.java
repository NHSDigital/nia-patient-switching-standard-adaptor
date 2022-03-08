package uk.nhs.adaptors.pss.translator.mapper;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.v3.CD;
import org.springframework.stereotype.Service;

@Service
public class CodeableConceptMapper {
    private static final String SNOMED_SYSTEM_CODE = "2.16.840.1.113883.2.1.3.2.4.15";
    private static final String SNOMED_SYSTEM = "http://snomed.info/sct";
    private static final String CONCEPT_PARTITION_SHORT = "00";
    private static final String CONCEPT_PARTITION_LONG = "10";
    private static final String DESCRIPTION_PARTITION_SHORT = "01";
    private static final String DESCRIPTION_PARTITION_LONG = "11";

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

    private CodeableConcept generateSnomedCodeableConcept(String code, String displayName) {
        var partitionIdentifier = getPartitionIdentifier(code);

        if (hasConceptId(partitionIdentifier)) {
            if (StringUtils.isNotEmpty(displayName)) {
                // obtain description id from db
                //// if description is preferred term then there is no synonym 
                ////// we output concept id as .code and displayName as .display and description id as description id in the extension
                
                //// if description is NOT preferred term then there IS synonym 
                ////// we output description id of desc matching displayName and the dislayName as the .descriptionId and .descriptionId in the extension. Term text of preferred term as .code and .display
            
                //// if no matching description get displayName use preferred term (see spread sheet)            
            }
        } else if (hasDescriptionId(partitionIdentifier)) {
                // obtain descriptionId from using concept id and seect the preferred term
                //// description id is used in the extension (https://fhir.nhs.uk/STU3/StructureDefinition/Extension-coding-sctdescid)
                //// term text as .display
                //// the code used for concept is used as .code
            
                //// if snomed coding is obtained from translation and displayName is absent, obtain display name from main code and compare to preferred text (see spreadsheet) 
        }

        // if description id
        //// obtain description from db
        ////// if the preferreD term
        //////// output the description id in the extension
        //////// output term text from descrpiotn as .display and concept id from descrpion as .code in CC
        
        return null;
    }

    private boolean hasSnomedCodeInMainCode(CD codedData) {
        return codedData.hasCodeSystem() && SNOMED_SYSTEM_CODE.equals(codedData.getCodeSystem());
    }

    private boolean hasSnomedCodeInTranslationElement(CD codedData) {
        return codedData.getTranslation()
            .stream()
            .anyMatch(translation -> SNOMED_SYSTEM_CODE.equals(translation.getCodeSystem()));
    }

    private CD getSnomedTranslationElement(CD codedData) {
        return codedData.getTranslation()
            .stream()
            .filter(translation -> SNOMED_SYSTEM_CODE.equals(translation.getCodeSystem()))
            .findFirst()
            .get();
    }

    private String getText(CD codedData) {
        return codedData.getOriginalText() != null ? codedData.getOriginalText() : codedData.getDisplayName();
    }

    private String getDisplayForSnomedTranslationElement(CD codedData, CD translation) {
        return translation.getDisplayName() != null ? translation.getDisplayName() : codedData.getDisplayName();
    }

    private boolean hasConceptId(String partitionIdentifier) {
        return CONCEPT_PARTITION_SHORT.equals(partitionIdentifier) || CONCEPT_PARTITION_LONG.equals(partitionIdentifier);
    }

    private boolean hasDescriptionId(String partitionIdentifier) {
        return DESCRIPTION_PARTITION_SHORT.equals(partitionIdentifier) || DESCRIPTION_PARTITION_LONG.equals(partitionIdentifier);
    }

    private String getPartitionIdentifier(String code) {
        var codeLength = code.length();

        return code.substring(code.length() - 3, code.length() - 1);
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
