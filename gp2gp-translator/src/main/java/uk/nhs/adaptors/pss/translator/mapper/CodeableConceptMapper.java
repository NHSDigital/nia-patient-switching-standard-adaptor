package uk.nhs.adaptors.pss.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.v3.CD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.nhs.adaptors.common.util.OidUtil;
import uk.nhs.adaptors.connector.dao.SnomedCTDao;
import uk.nhs.adaptors.connector.model.SnomedCTDescription;
import uk.nhs.adaptors.pss.translator.util.CodeSystemsUtil;
import static uk.nhs.adaptors.common.util.CodeableConceptUtils.createCodeableConcept;

import java.util.Objects;

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
        var codeableConcept = generateCodeableConcept(codedData, false);
        addNonSnomedCodesToCodeableConcept(codeableConcept, codedData);

        return codeableConcept;
    }

    public CodeableConcept mapToCodeableConceptForMedication(CD codedData) {
        var codeableConcept =  generateCodeableConcept(codedData, true);
        addNonSnomedCodesToCodeableConcept(codeableConcept, codedData);

        return codeableConcept;
    }

    private CodeableConcept generateCodeableConceptWithoutSnomedCode(CD codedData) {
        var text = StringUtils.isNotEmpty(codedData.getOriginalText())
            ? codedData.getOriginalText()
            : codedData.getDisplayName();

        return new CodeableConcept()
            .setText(text);
    }

    private CodeableConcept generateCodeableConcept(CD codedData, boolean isMedicationResource) {

        var isSnomedCodeInMainCode = codedData.hasCodeSystem()
            && SNOMED_SYSTEM_CODE.equals(codedData.getCodeSystem());

        var isSnomedCodeInTranslationElement = codedData.getTranslation()
            .stream()
            .anyMatch(translation -> SNOMED_SYSTEM_CODE.equals(translation.getCodeSystem()));

        if (!isSnomedCodeInMainCode && !isSnomedCodeInTranslationElement) {
            return generateCodeableConceptWithoutSnomedCode(codedData);
        }

        var mainCode = isSnomedCodeInMainCode
            ? codedData
            : codedData.getTranslation()
            .stream()
            .filter(translation -> SNOMED_SYSTEM_CODE.equals(translation.getCodeSystem()))
            .findFirst()
            .get();

        var translationMainCode = isSnomedCodeInTranslationElement
            ? codedData
            : null;

        var partitionIdentifier = getPartitionIdentifier(mainCode.getCode());
        var isCodeConceptId = isCodeConceptId(partitionIdentifier);
        var isCodeDescriptionId = isCodeDescriptionId(partitionIdentifier);

        if (!isCodeConceptId && !isCodeDescriptionId) {
            return generateCodeableConceptWithoutSnomedCode(codedData);
        }

        return isCodeConceptId
            ? generateCodeableConceptUsingConceptId(mainCode, translationMainCode, isMedicationResource)
            : generateCodeableConceptUsingDescriptionId(mainCode, translationMainCode, isMedicationResource);
    }

    private CodeableConcept generateCodeableConceptUsingDescriptionId(CD codedData, CD translationMainCode, boolean isMedicationResource) {
        var descriptionId = codedData.getCode();

        var displayName = hasDisplayNameInTranslationMainCode(translationMainCode)
            ? translationMainCode.getDisplayName()
            : codedData.getDisplayName();

        var originalText = hasOriginalTextInTranslationMainCode(translationMainCode)
            ? translationMainCode.getOriginalText()
            : codedData.getOriginalText();

        if (isMedicationResource) {
            return new CodeableConcept().setText(getTextFieldValue(originalText, displayName));
        }

        var description = snomedCTDao.getSnomedDescriptionUsingDescriptionId(descriptionId);

        if (Objects.isNull(description)) {
            if (Objects.nonNull(translationMainCode)) {
                displayName = getDisplayForSnomedTranslationElement(codedData, translationMainCode);
            }

            return new CodeableConcept().setText(getTextFieldValue(originalText, displayName));
        }

        var preferredTerm = snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(description.getConceptid());

        if (Objects.isNull(preferredTerm)) {
            return null;
        }

        String code;
        String display;
        String text;
        String descriptionDisplay = null;

        if (isDescriptionPreferredTerm(description, preferredTerm)) {
            code = description.getConceptid();
            display = description.getTerm();
            text = !displayName.equals(description.getTerm())
                ? displayName
                : null;
        } else {
            code = preferredTerm.getConceptid();
            display = preferredTerm.getTerm();
            descriptionDisplay = description.getTerm();
            text = !displayName.equals(preferredTerm.getTerm())
                ? displayName
                : null;
        }

        var extension = createExtension(description.getId(), descriptionDisplay);
        var textFieldValue = getTextFieldValue(originalText, text);

        return createCodeableConcept(code, SNOMED_SYSTEM, display, textFieldValue, extension);
    }

    private CodeableConcept generateCodeableConceptUsingConceptId(CD mainCode, CD translationMainCode, boolean isMedicationResource) {
        var conceptId = mainCode.getCode();
        var displayName = getDisplayName(mainCode, translationMainCode);
        var originalText = getOriginalText(mainCode, translationMainCode);
        var preferredTerm = snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(conceptId);
        var description = StringUtils.isEmpty(displayName)
            ? snomedCTDao.getSnomedDescriptionUsingConceptId(conceptId)
            : snomedCTDao.getSnomedDescriptionUsingConceptIdAndDisplayName(conceptId, displayName);

        String display;
        String text;
        String extensionId = null;
        String extensionDisplay =  null;

        if (StringUtils.isEmpty(displayName) && hasPreferredValueAndDescriptionValuePresent(preferredTerm, description)) {
            display = preferredTerm.getTerm();
            text = getPreferredText(originalText, translationMainCode, preferredTerm);
            extensionId = description.getId();
        } else if (hasPreferredValuePresentAndNoDescriptionValue(preferredTerm, description)) {
            display = preferredTerm.getTerm();
            text = getTextFieldValue(originalText, displayName);
            extensionDisplay = displayName;
        } else if (hasPreferredValueAndDescriptionValuePresent(preferredTerm, description)) {
            text = getTextFieldValue(originalText, displayName);

            if (isDescriptionPreferredTerm(description, preferredTerm)) {
                display = displayName;
                extensionId = preferredTerm.getId();
                extensionDisplay = preferredTerm.getTerm();
            } else {
                display = preferredTerm.getTerm();
                extensionId = description.getId();
                extensionDisplay = description.getTerm();
            }
        } else if (hasPreferredValueAndDescriptionValueNotPresent(preferredTerm, description)) {
            var translationDisplay = hasDisplayNameInTranslationMainCode(translationMainCode)
                ? translationMainCode.getDisplayName()
                : null;

            display = StringUtils.isNotEmpty(displayName)
                ? displayName
                : translationDisplay;
            text = originalText;
        } else {
            return null;
        }

        var extension = isMedicationResource || hasPreferredValueAndDescriptionValueNotPresent(preferredTerm, description)
            ? null
            : createExtension(extensionId, extensionDisplay);

        return createCodeableConcept(conceptId, SNOMED_SYSTEM, display, text, extension);
    }

    private void addNonSnomedCodesToCodeableConcept(CodeableConcept codeableConcept, CD codedData) {
        addNonSnomedCodeIfPresent(codeableConcept, codedData, 0);
        var translationCodes = codedData.getTranslation();

        for (int index = 0; index < translationCodes.size(); index++) {
            addNonSnomedCodeIfPresent(codeableConcept, translationCodes.get(index), index + 1);
        }
    }

    private void addNonSnomedCodeIfPresent(CodeableConcept codeableConcept, CD codedData, int index) {
        if (codedData.hasCodeSystem() && !SNOMED_SYSTEM_CODE.equals(codedData.getCodeSystem())) {
            var system = CodeSystemsUtil.getFhirCodeSystem(codedData.getCodeSystem());
            system = OidUtil.tryParseToUrn(system).orElse(system);

            var coding = new Coding()
                    .setCode(codedData.getCode())
                    .setSystem(system)
                    .setDisplay(codedData.getDisplayName());

            codeableConcept.getCoding().add(index, coding);
        }
    }

    private Extension createExtension(String descriptionId, String descriptionDisplay) {
        var extension = new Extension()
            .setUrl(EXTENSION_URL);

        if (StringUtils.isNotEmpty(descriptionId)) {
            var idExtension = new Extension()
                .setUrl(EXTENSION_DESCRIPTION_ID_URL)
                .setValue(new IdType(descriptionId));

            extension.addExtension(idExtension);
        }
        if (StringUtils.isNotEmpty(descriptionDisplay)) {
            var displayExtension = new Extension()
                .setUrl(EXTENSION_DESCRIPTION_DISPLAY_URL)
                .setValue(new StringType(descriptionDisplay));

            extension.addExtension(displayExtension);
        }

        return extension;
    }

    private String getDisplayName(CD mainCode, CD translationMainCode) {
        return translationMainCode != null && !mainCode.hasDisplayName()
            ? translationMainCode.getDisplayName()
            : mainCode.getDisplayName();
    }

    private String getOriginalText(CD mainCode, CD translationMainCode) {
        return translationMainCode != null && StringUtils.isNotEmpty(translationMainCode.getOriginalText())
            ? translationMainCode.getOriginalText()
            : mainCode.getOriginalText();
    }

    private String getPreferredText(String originalText, CD translationMainCode, SnomedCTDescription preferredTerm) {
        if (StringUtils.isEmpty(originalText) && hasDisplayNameInTranslationMainCode(translationMainCode)) {
            return !translationMainCode.getDisplayName().equals(preferredTerm.getTerm())
                ? translationMainCode.getDisplayName()
                : preferredTerm.getTerm();
        }
        return originalText;
    }

    private String getDisplayForSnomedTranslationElement(CD codedData, CD translation) {
        return translation.getDisplayName() != null
            ? translation.getDisplayName()
            : codedData.getDisplayName();
    }

    private String getPartitionIdentifier(String code) {
        var codeLength = code.length();
        return code.substring(codeLength - MAX_CHARACTERS_FROM_RIGHT, codeLength - MIN_CHARACTERS_FROM_RIGHT);
    }

    private String getTextFieldValue(String text, String newText) {
        return StringUtils.isNotEmpty(text)
            ? text
            : newText;
    }

    private boolean isCodeConceptId(String partitionIdentifier) {
        return CONCEPT_PARTITION_SHORT.equals(partitionIdentifier)
            || CONCEPT_PARTITION_LONG.equals(partitionIdentifier);
    }

    private boolean isCodeDescriptionId(String partitionIdentifier) {
        return DESCRIPTION_PARTITION_SHORT.equals(partitionIdentifier)
            || DESCRIPTION_PARTITION_LONG.equals(partitionIdentifier);
    }

    private boolean hasPreferredValuePresentAndNoDescriptionValue(SnomedCTDescription preferredTerm, SnomedCTDescription description) {
        return Objects.nonNull(preferredTerm)
            && Objects.isNull(description);
    }

    private boolean hasPreferredValueAndDescriptionValuePresent(SnomedCTDescription preferredTerm, SnomedCTDescription description) {
        return Objects.nonNull(preferredTerm)
            && Objects.nonNull(description);
    }

    private boolean hasPreferredValueAndDescriptionValueNotPresent(SnomedCTDescription preferredTerm, SnomedCTDescription description) {
        return Objects.isNull(preferredTerm)
            && Objects.isNull(description);
    }

    private boolean hasOriginalTextInTranslationMainCode(CD translationMainCode) {
        return Objects.nonNull(translationMainCode)
            && StringUtils.isNotEmpty(translationMainCode.getOriginalText());
    }

    private boolean hasDisplayNameInTranslationMainCode(CD mainCodeFromTranslation) {
        return Objects.nonNull(mainCodeFromTranslation)
            && StringUtils.isNotEmpty(mainCodeFromTranslation.getDisplayName());
    }

    private boolean isDescriptionPreferredTerm(SnomedCTDescription description, SnomedCTDescription preferredTerm) {
        return description.getId().equals(preferredTerm.getId())
            && description.getTerm().equals(preferredTerm.getTerm());
    }
}
