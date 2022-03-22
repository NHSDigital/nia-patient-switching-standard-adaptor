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

    private boolean isMedicationResource = false;

    public CodeableConcept mapToCodeableConcept(CD codedData) {
        if (hasSnomedCodeInMainCode(codedData)) {
            return generateSnomedCodeableConcept(codedData, null);
        } else if (hasSnomedCodeInTranslationElement(codedData)) {
            var translation = getSnomedTranslationElement(codedData);
            return generateSnomedCodeableConcept(translation, codedData);
        }

        return new CodeableConcept().setText(generateText(codedData.getOriginalText(), codedData.getDisplayName()));
    }

    public CodeableConcept mapToCodeableConceptForMedication(CD codedData) {
        this.isMedicationResource = true;
        return mapToCodeableConcept(codedData);
    }

    private CodeableConcept generateSnomedCodeableConcept(CD codedData, CD mainCodeFromTranslation) {
        if (isCodeConceptId(getPartitionIdentifier(codedData.getCode()))) {
            return generateCodeableConceptUsingConceptId(codedData, mainCodeFromTranslation);
        } else if (isCodeDescriptionId(getPartitionIdentifier(codedData.getCode()))) {
            return generateCodeableConceptUsingDescriptionId(codedData, mainCodeFromTranslation);
        }

        return new CodeableConcept().setText(generateText(codedData.getOriginalText(), codedData.getDisplayName()));
    }

    private CodeableConcept generateCodeableConceptUsingConceptId(CD codedData, CD mainCodeFromTranslation) {
        var conceptId = codedData.getCode();
        var displayName = codedData.getDisplayName();
        var originalText = Objects.nonNull(mainCodeFromTranslation) ? mainCodeFromTranslation.getOriginalText()
            : codedData.getOriginalText();

        SnomedCTDescription description;
        SnomedCTDescription preferredTerm = snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(conceptId);

        if (StringUtils.isNotEmpty(displayName)) {
            description = snomedCTDao.getSnomedDescriptionUsingConceptIdAndDisplayName(conceptId, displayName);

            if (Objects.isNull(description) && Objects.nonNull(preferredTerm)) {
                return createCodeableConcept(conceptId, preferredTerm.getTerm(), generateText(originalText, displayName),
                    createExtension(preferredTerm.getId(), null));
            }

            if (Objects.nonNull(description) && Objects.nonNull(preferredTerm)) {
                if (isDescriptionPreferredTerm(description, preferredTerm)) {
                    return createCodeableConcept(conceptId, displayName, codedData.getOriginalText(),
                        createExtension(description.getId(), null));
                } else {
                    return createCodeableConcept(preferredTerm.getId(), preferredTerm.getTerm(),
                        codedData.getOriginalText(), createExtension(description.getId(), description.getTerm()));
                }
            }
        } else {
            description = snomedCTDao.getSnomedDescriptionUsingConceptId(conceptId);

            if (Objects.nonNull(description) && Objects.nonNull(preferredTerm)) {

                var text = originalText;

                if (Objects.nonNull(mainCodeFromTranslation)
                    && StringUtils.isNotEmpty(mainCodeFromTranslation.getDisplayName())) {
                    text = !mainCodeFromTranslation.getDisplayName().equals(preferredTerm.getTerm())
                        ? mainCodeFromTranslation.getDisplayName()
                        : preferredTerm.getTerm();
                }

                return createCodeableConcept(conceptId, preferredTerm.getTerm(), generateText(originalText, text),
                    createExtension(description.getId(), null));
            }
        }

        // No matching description for concept id
        if (Objects.isNull(description) && Objects.isNull(preferredTerm)) {
            var display = StringUtils.isNotEmpty(displayName) ? displayName
                : Objects.nonNull(mainCodeFromTranslation) && StringUtils.isNotEmpty(mainCodeFromTranslation.getDisplayName())
                ? mainCodeFromTranslation.getDisplayName() : null;

            return createCodeableConcept(conceptId, display, originalText, null);
        }

        return null;
    }

    private CodeableConcept generateCodeableConceptUsingDescriptionId(CD codedData, CD mainCodeFromTranslation) {
        var descriptionId = codedData.getCode();
        var displayName = Objects.nonNull(mainCodeFromTranslation)
            ? mainCodeFromTranslation.getDisplayName() : codedData.getDisplayName();
        var originalText = Objects.nonNull(mainCodeFromTranslation)
            ? mainCodeFromTranslation.getOriginalText() : codedData.getOriginalText();

        SnomedCTDescription description = snomedCTDao.getSnomedDescriptionUsingDescriptionId(descriptionId);
        String text = null;

        // No matching description for description id
        if (Objects.isNull(description)) {
            if (Objects.nonNull(mainCodeFromTranslation)) {
                displayName = getDisplayForSnomedTranslationElement(codedData, mainCodeFromTranslation);
            }

            return new CodeableConcept().setText(generateText(originalText, displayName));
        }

        SnomedCTDescription preferredTerm = snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(description.getConceptid());

        if (Objects.nonNull(preferredTerm)) {
            if (isDescriptionPreferredTerm(description, preferredTerm)) {
                if (!displayName.equals(description.getTerm())) {
                    text = displayName;
                }
                return createCodeableConcept(description.getConceptid(), description.getTerm(), generateText(originalText,
                    text), createExtension(description.getId(), null));
            } else {
                if (!displayName.equals(preferredTerm.getTerm())) {
                    text = displayName;
                }
                return createCodeableConcept(preferredTerm.getConceptid(), preferredTerm.getTerm(),
                    generateText(originalText, text), createExtension(description.getId(), description.getTerm()));
            }
        }

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

    private String generateText(String text, String newText) {
        return StringUtils.isNotEmpty(text) ? text : newText;
    }

    private String getDisplayForSnomedTranslationElement(CD codedData, CD translation) {
        return translation.getDisplayName() != null ? translation.getDisplayName() : codedData.getDisplayName();
    }

    private boolean isCodeConceptId(String partitionIdentifier) {
        return CONCEPT_PARTITION_SHORT.equals(partitionIdentifier) || CONCEPT_PARTITION_LONG.equals(partitionIdentifier);
    }

    private boolean isCodeDescriptionId(String partitionIdentifier) {
        return DESCRIPTION_PARTITION_SHORT.equals(partitionIdentifier) || DESCRIPTION_PARTITION_LONG.equals(partitionIdentifier);
    }

    private String getPartitionIdentifier(String code) {
        var codeLength = code.length();
        return code.substring(codeLength - MAX_CHARACTERS_FROM_RIGHT, codeLength - MIN_CHARACTERS_FROM_RIGHT);
    }

    private CodeableConcept createCodeableConcept(String code, String display, String text, Extension extension) {
        var codeableConcept = new CodeableConcept();
        codeableConcept
            .setText(text)
            .getCodingFirstRep()
            .setCode(code)
            .setSystem(CodeableConceptMapper.SNOMED_SYSTEM)
            .setDisplay(display)
            .addExtension(extension);
        return codeableConcept;
    }

    private boolean isDescriptionPreferredTerm(SnomedCTDescription description, SnomedCTDescription preferredTerm) {
        return description.getId().equals(preferredTerm.getId())
            && description.getTerm().equals(preferredTerm.getTerm());
    }

    private Extension createExtension(String descriptionId, String descriptionDisplay) {
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
}
