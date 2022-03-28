package uk.nhs.adaptors.pss.translator.mapper;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.v3.CD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.connector.dao.SnomedCTDao;
import uk.nhs.adaptors.connector.model.SnomedCTDescription;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CodeableConceptMapper {
    private static final String SNOMED_SYSTEM_CODE = "2.16.840.1.113883.2.1.3.2.4.15";
    private static final String SNOMED_SYSTEM = "http://snomed.info/sct";
    private static final String EXTENSION_URL = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-coding-sctdescid";
    private static final String EXTENSION_DESCRIPTION_ID_URL = "descriptionId";
    private static final String EXTENSION_DESCRIPTION_DISPLAY_URL = "descriptionDisplay";
    private static final String CONCEPT_PARTITION_SHORT = "00";
    private static final String CONCEPT_PARTITION_LONG = "10";
    private static final String DESCRIPTION_PARTITION_SHORT = "01";
    private static final String DESCRIPTION_PARTITION_LONG = "11";
    private static final int MAX_CHARACTERS_FROM_RIGHT = 3;
    private static final int MIN_CHARACTERS_FROM_RIGHT = 1;

    private final SnomedCTDao snomedCTDao;

    public CodeableConcept mapToCodeableConcept(CD codedData) {
        return generateCodeableConcept(codedData, false);
    }

    public CodeableConcept mapToCodeableConceptForMedication(CD codedData) {
        return generateCodeableConcept(codedData, true);
    }

    private CodeableConcept generateCodeableConcept(CD codedData, boolean isMedicationResource) {
        if (hasSnomedCodeInMainCode(codedData)) {
            return generateSnomedCodeableConcept(codedData, null, isMedicationResource);
        } else if (hasSnomedCodeInTranslationElement(codedData)) {
            return generateSnomedCodeableConcept(getSnomedTranslationElement(codedData), codedData, isMedicationResource);
        }

        return new CodeableConcept().setText(determineTextFieldValue(codedData.getOriginalText(), codedData.getDisplayName()));
    }

    private CodeableConcept generateSnomedCodeableConcept(CD codedData, CD mainCodeFromTranslation, boolean isMedicationResource) {
        if (isCodeConceptId(getPartitionIdentifier(codedData.getCode()))) {
            return generateCodeableConceptUsingConceptId(codedData, mainCodeFromTranslation, isMedicationResource);
        } else if (isCodeDescriptionId(getPartitionIdentifier(codedData.getCode()))) {
            return generateCodeableConceptUsingDescriptionId(codedData, mainCodeFromTranslation, isMedicationResource);
        }

        return new CodeableConcept().setText(determineTextFieldValue(codedData.getOriginalText(), codedData.getDisplayName()));
    }

    private CodeableConcept generateCodeableConceptUsingConceptId(CD codedData, CD mainCodeFromTranslation, boolean isMedicationResource) {
        var conceptId = codedData.getCode();
        var displayName = codedData.getDisplayName();

        var originalText = translationMainCodeHasOriginalText(mainCodeFromTranslation)
            ? mainCodeFromTranslation.getOriginalText()
            : codedData.getOriginalText();

        SnomedCTDescription description;
        SnomedCTDescription preferredTerm = snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(conceptId);

        if (StringUtils.isNotEmpty(displayName)) {
            description = snomedCTDao.getSnomedDescriptionUsingConceptIdAndDisplayName(conceptId, displayName);

            if (preferredValuePresentNoDescriptionValue(preferredTerm, description)) {
                return createCodeableConcept(conceptId, SNOMED_SYSTEM, preferredTerm.getTerm(),
                    determineTextFieldValue(originalText, displayName), createExtension(preferredTerm.getId(), null, isMedicationResource));
            }

            if (preferredAndDescriptionValuesPresent(preferredTerm, description)) {
                if (isDescriptionPreferredTerm(description, preferredTerm)) {
                    return createCodeableConcept(conceptId, SNOMED_SYSTEM, displayName, codedData.getOriginalText(),
                        createExtension(description.getId(), null, isMedicationResource));
                } else {
                    return createCodeableConcept(preferredTerm.getId(), SNOMED_SYSTEM, preferredTerm.getTerm(), codedData.getOriginalText(),
                        createExtension(description.getId(), description.getTerm(), isMedicationResource));
                }
            }
        } else {
            description = snomedCTDao.getSnomedDescriptionUsingConceptId(conceptId);

            if (preferredAndDescriptionValuesPresent(preferredTerm, description)) {
                String text = originalText;
                if (StringUtils.isEmpty(originalText) && translationMainCodeHasDisplayName(mainCodeFromTranslation)) {
                    text = !mainCodeFromTranslation.getDisplayName().equals(preferredTerm.getTerm())
                        ? mainCodeFromTranslation.getDisplayName()
                        : preferredTerm.getTerm();
                }

                return createCodeableConcept(conceptId, SNOMED_SYSTEM, preferredTerm.getTerm(), text,
                    createExtension(description.getId(), null, isMedicationResource));
            }
        }

        if (preferredAndDescriptionValuesNotPresent(preferredTerm, description)) {
            var display = StringUtils.isNotEmpty(displayName)
                ? displayName
                : translationMainCodeHasDisplayName(mainCodeFromTranslation) ? mainCodeFromTranslation.getDisplayName() : null;

            return createCodeableConcept(conceptId, SNOMED_SYSTEM, display, originalText, null);
        }

        return null;
    }

    private CodeableConcept generateCodeableConceptUsingDescriptionId(CD codedData, CD mainCodeFromTranslation,
        boolean isMedicationResource) {
        var descriptionId = codedData.getCode();

        var displayName = translationMainCodeHasDisplayName(mainCodeFromTranslation)
            ? mainCodeFromTranslation.getDisplayName()
            : codedData.getDisplayName();

        var originalText = translationMainCodeHasOriginalText(mainCodeFromTranslation)
            ? mainCodeFromTranslation.getOriginalText()
            : codedData.getOriginalText();
        
        if (isMedicationResource) {
            return new CodeableConcept().setText(determineTextFieldValue(originalText, displayName));
        }

        SnomedCTDescription description = snomedCTDao.getSnomedDescriptionUsingDescriptionId(descriptionId);
        String text = null;

        if (Objects.isNull(description)) {
            if (Objects.nonNull(mainCodeFromTranslation)) {
                displayName = getDisplayForSnomedTranslationElement(codedData, mainCodeFromTranslation);
            }

            return new CodeableConcept().setText(determineTextFieldValue(originalText, displayName));
        }

        SnomedCTDescription preferredTerm = snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(description.getConceptid());

        if (Objects.nonNull(preferredTerm)) {
            if (isDescriptionPreferredTerm(description, preferredTerm)) {
                if (!displayName.equals(description.getTerm())) {
                    text = displayName;
                }
                return createCodeableConcept(description.getConceptid(), SNOMED_SYSTEM, description.getTerm(),
                    determineTextFieldValue(originalText, text), createExtension(description.getId(), null, false));
            } else {
                if (!displayName.equals(preferredTerm.getTerm())) {
                    text = displayName;
                }
                return createCodeableConcept(preferredTerm.getConceptid(), SNOMED_SYSTEM, preferredTerm.getTerm(),
                    determineTextFieldValue(originalText, text),
                    createExtension(description.getId(), description.getTerm(), false));
            }
        }

        return null;
    }

    private CodeableConcept createCodeableConcept(String code, String system, String display, String text, Extension extension) {
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

    private Extension createExtension(String descriptionId, String descriptionDisplay, boolean isMedicationResource) {
        if (isMedicationResource) {
            return null;
        }

        Extension extension = new Extension();

        extension.setUrl(EXTENSION_URL);

        if (StringUtils.isNotEmpty(descriptionId)) {
            extension.addExtension(new Extension()
                .setUrl(EXTENSION_DESCRIPTION_ID_URL)
                .setValue(new IdType(descriptionId)));
        }

        if (StringUtils.isNotEmpty(descriptionDisplay)) {
            extension.addExtension(new Extension()
                .setUrl(EXTENSION_DESCRIPTION_DISPLAY_URL)
                .setValue(new StringType(descriptionDisplay)));
        }

        return extension;
    }

    private CD getSnomedTranslationElement(CD codedData) {
        return codedData.getTranslation()
            .stream()
            .filter(translation -> SNOMED_SYSTEM_CODE.equals(translation.getCodeSystem()))
            .findFirst()
            .get();
    }

    private String getDisplayForSnomedTranslationElement(CD codedData, CD translation) {
        return translation.getDisplayName() != null ? translation.getDisplayName() : codedData.getDisplayName();
    }

    private String getPartitionIdentifier(String code) {
        var codeLength = code.length();
        return code.substring(codeLength - MAX_CHARACTERS_FROM_RIGHT, codeLength - MIN_CHARACTERS_FROM_RIGHT);
    }

    private String determineTextFieldValue(String text, String newText) {
        return StringUtils.isNotEmpty(text) ? text : newText;
    }

    private boolean hasSnomedCodeInMainCode(CD codedData) {
        return codedData.hasCodeSystem() && SNOMED_SYSTEM_CODE.equals(codedData.getCodeSystem());
    }

    private boolean hasSnomedCodeInTranslationElement(CD codedData) {
        return codedData.getTranslation()
            .stream()
            .anyMatch(translation -> SNOMED_SYSTEM_CODE.equals(translation.getCodeSystem()));
    }

    private boolean isCodeConceptId(String partitionIdentifier) {
        return CONCEPT_PARTITION_SHORT.equals(partitionIdentifier) || CONCEPT_PARTITION_LONG.equals(partitionIdentifier);
    }

    private boolean isCodeDescriptionId(String partitionIdentifier) {
        return DESCRIPTION_PARTITION_SHORT.equals(partitionIdentifier) || DESCRIPTION_PARTITION_LONG.equals(partitionIdentifier);
    }

    private boolean preferredValuePresentNoDescriptionValue(SnomedCTDescription preferredTerm, SnomedCTDescription description) {
        return Objects.nonNull(preferredTerm) && Objects.isNull(description);
    }

    private boolean preferredAndDescriptionValuesPresent(SnomedCTDescription preferredTerm, SnomedCTDescription description) {
        return Objects.nonNull(preferredTerm) && Objects.nonNull(description);
    }

    private boolean preferredAndDescriptionValuesNotPresent(SnomedCTDescription preferredTerm, SnomedCTDescription description) {
        return Objects.isNull(preferredTerm) && Objects.isNull(description);
    }

    private boolean translationMainCodeHasOriginalText(CD mainCodeFromTranslation) {
        return Objects.nonNull(mainCodeFromTranslation) && StringUtils.isNotEmpty(mainCodeFromTranslation.getOriginalText());
    }

    private boolean translationMainCodeHasDisplayName(CD mainCodeFromTranslation) {
        return Objects.nonNull(mainCodeFromTranslation) && StringUtils.isNotEmpty(mainCodeFromTranslation.getDisplayName());
    }

    private boolean isDescriptionPreferredTerm(SnomedCTDescription description, SnomedCTDescription preferredTerm) {
        return description.getId().equals(preferredTerm.getId())
            && description.getTerm().equals(preferredTerm.getTerm());
    }
}
