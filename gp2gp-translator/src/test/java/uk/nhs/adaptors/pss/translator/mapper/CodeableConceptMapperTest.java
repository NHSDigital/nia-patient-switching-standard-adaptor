package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.v3.CD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.connector.dao.SnomedCTDao;
import uk.nhs.adaptors.connector.model.SnomedCTDescription;

@ExtendWith(MockitoExtension.class)
public class CodeableConceptMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/CD/";
    private static final String CONCEPT_ID = "22298006";
    private static final String DISPLAY_NAME_1 = "Myocardial infarction";
    private static final String DISPLAY_NAME_2 = "Heart attack";
    private static final String DISPLAY_NAME_3 = "Different display name";
    private static final String ORIGINAL_TEXT = "Test original text";
    private static final String DESCRIPTION_ID = "descriptionId";
    private static final String DESCRIPTION_DISPLAY = "descriptionDisplay";
    private static final SnomedCTDescription SNOMED_DESCRIPTION = SnomedCTDescription.builder()
        .id("test_description_id")
        .conceptid("test_concept_id")
        .term("Heart attack")
        .build();
    private static final SnomedCTDescription SNOMED_PREFERRED = SnomedCTDescription.builder()
        .id("test_preferred_id")
        .conceptid("test_concept_id")
        .term("Myocardial infarction")
        .build();

    @Mock
    private SnomedCTDao snomedCTDao;

    @InjectMocks
    private CodeableConceptMapper codeableConceptMapper;

    @Test
    public void mapSnomedCodeWithConceptIdDisplayNameDescriptionIsPreferredTerm() {
        when(snomedCTDao.getSnomedDescriptionUsingConceptIdAndDisplayName(any(), any())).thenReturn(SNOMED_PREFERRED);
        when(snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(any())).thenReturn(SNOMED_PREFERRED);
        var codedData = unmarshallCodeElement("concept-id-display-name-original-text-example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getCoding().get(0).getCode()).isEqualTo(CONCEPT_ID);
        assertThat(codeableConcept.getCoding().get(0).getDisplay()).isEqualTo(SNOMED_PREFERRED.getTerm());
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getUrl()).isEqualTo(DESCRIPTION_ID);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getValue())
            .isEqualTo(new IdType(SNOMED_PREFERRED.getId()));
        assertThat(codeableConcept.getText()).isEqualTo(ORIGINAL_TEXT);
    }

    @Test
    public void mapSnomedCodeWithConceptIdDisplayNameDescriptionIsNotPreferredTerm() {
        when(snomedCTDao.getSnomedDescriptionUsingConceptIdAndDisplayName(any(), any())).thenReturn(SNOMED_DESCRIPTION);
        when(snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(any())).thenReturn(SNOMED_PREFERRED);
        var codedData = unmarshallCodeElement("concept-id-display-name-original-text-example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getCoding().get(0).getCode()).isEqualTo("22298006");
        assertThat(codeableConcept.getCoding().get(0).getDisplay()).isEqualTo(SNOMED_PREFERRED.getTerm());
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().size() == 2).isTrue();
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getUrl()).isEqualTo(DESCRIPTION_ID);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getValue())
            .isEqualTo(new IdType(SNOMED_DESCRIPTION.getId()));
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(1).getUrl()).isEqualTo(DESCRIPTION_DISPLAY);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(1).getValue()
            .toString()).isEqualTo(SNOMED_DESCRIPTION.getTerm());
        assertThat(codeableConcept.getText()).isEqualTo(ORIGINAL_TEXT);
    }

    @Test
    public void mapSnomedCodeWithConceptIdDisplayNameOriginalTextNoMatchedDescription() {
        when(snomedCTDao.getSnomedDescriptionUsingConceptIdAndDisplayName(any(), any())).thenReturn(null);
        when(snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(any())).thenReturn(SNOMED_PREFERRED);
        var codedData = unmarshallCodeElement("concept-id-display-name-original-text-example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getCoding().get(0).getCode()).isEqualTo(CONCEPT_ID);
        assertThat(codeableConcept.getCoding().get(0).getDisplay()).isEqualTo(DISPLAY_NAME_1);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().size() == 1).isTrue();
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getUrl()).isEqualTo(DESCRIPTION_DISPLAY);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getValue().toString())
            .isEqualTo((SNOMED_PREFERRED.getTerm()));
        assertThat(codeableConcept.getText()).isEqualTo(ORIGINAL_TEXT);
    }

    @Test
    public void mapSnomedCodeWithConceptIdDisplayNameNoOriginalTextNoMatchedDescription() {
        when(snomedCTDao.getSnomedDescriptionUsingConceptIdAndDisplayName(any(), any())).thenReturn(null);
        when(snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(any())).thenReturn(SNOMED_PREFERRED);
        var codedData = unmarshallCodeElement("concept-id-display-name-no-original-text-example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getCoding().get(0).getCode()).isEqualTo(CONCEPT_ID);
        assertThat(codeableConcept.getCoding().get(0).getDisplay()).isEqualTo(DISPLAY_NAME_1);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().size() == 1).isTrue();
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getUrl()).isEqualTo(DESCRIPTION_DISPLAY);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getValue().toString())
            .isEqualTo((SNOMED_PREFERRED.getTerm()));
        assertThat(codeableConcept.getText()).isEqualTo(DISPLAY_NAME_1);
    }

    @Test
    public void mapSnomedCodeWithConceptIdNoDisplayName() {
        when(snomedCTDao.getSnomedDescriptionUsingConceptId(any())).thenReturn(SNOMED_DESCRIPTION);
        when(snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(any())).thenReturn(SNOMED_PREFERRED);
        var codedData = unmarshallCodeElement("concept-id-no-display-code-example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getCoding().get(0).getCode()).isEqualTo(CONCEPT_ID);
        assertThat(codeableConcept.getCoding().get(0).getDisplay()).isEqualTo(DISPLAY_NAME_1);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().size() == 1).isTrue();
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getUrl()).isEqualTo(DESCRIPTION_ID);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getValue())
            .isEqualTo(new IdType(SNOMED_DESCRIPTION.getId()));
    }

    @Test
    public void mapSnomedCodeWithConceptIdDisplayNameInTranslationMainCode() {
        when(snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(any())).thenReturn(SNOMED_PREFERRED);
        var codedData = unmarshallCodeElement("translation-concept-id-no-display-code-example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getCoding().get(0).getCode()).isEqualTo(CONCEPT_ID);
        assertThat(codeableConcept.getCoding().get(0).getDisplay()).isEqualTo(DISPLAY_NAME_1);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().size() == 1).isTrue();
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getUrl()).isEqualTo(DESCRIPTION_DISPLAY);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getValue().toString())
            .isEqualTo((SNOMED_DESCRIPTION.getTerm()));
        assertThat(codeableConcept.getText()).isEqualTo(ORIGINAL_TEXT);
    }

    @Test
    public void mapSnomedCodeWithConceptIdDisplayNameInTranslationMainCodeNoOriginalText() {
        when(snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(any())).thenReturn(SNOMED_PREFERRED);
        var codedData = unmarshallCodeElement("translation-concept-id-no-display-code-no-original-text-example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getCoding().get(0).getCode()).isEqualTo(CONCEPT_ID);
        assertThat(codeableConcept.getCoding().get(0).getDisplay()).isEqualTo(DISPLAY_NAME_1);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().size() == 1).isTrue();
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getUrl()).isEqualTo(DESCRIPTION_DISPLAY);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getValue().toString())
            .isEqualTo((SNOMED_DESCRIPTION.getTerm()));
        assertThat(codeableConcept.getText()).isEqualTo(DISPLAY_NAME_2);
    }

    @Test
    public void mapSnomedCodeWithConceptIdNoMatchingDescriptionOriginalText() {
        when(snomedCTDao.getSnomedDescriptionUsingConceptIdAndDisplayName(any(), any())).thenReturn(null);
        when(snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(any())).thenReturn(null);
        var codedData = unmarshallCodeElement("concept-id-display-name-original-text-example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getCoding().get(0).getCode()).isEqualTo(CONCEPT_ID);
        assertThat(codeableConcept.getCoding().get(0).getDisplay()).isEqualTo(DISPLAY_NAME_1);
        assertThat(codeableConcept.getCoding().get(0).getExtension()).isNullOrEmpty();
        assertThat(codeableConcept.getText()).isEqualTo(ORIGINAL_TEXT);
    }

    @Test
    public void mapSnomedCodeWithConceptIdNoMatchingDescriptionNoOriginalText() {
        when(snomedCTDao.getSnomedDescriptionUsingConceptIdAndDisplayName(any(), any())).thenReturn(null);
        when(snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(any())).thenReturn(null);
        var codedData = unmarshallCodeElement("concept-id-display-name-no-original-text-example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getCoding().get(0).getCode()).isEqualTo(CONCEPT_ID);
        assertThat(codeableConcept.getCoding().get(0).getDisplay()).isEqualTo(DISPLAY_NAME_1);
        assertThat(codeableConcept.getCoding().get(0).getExtension()).isNullOrEmpty();
        assertThat(codeableConcept.getText()).isNull();
    }

    @Test
    public void mapSnomedCodeWithConceptIdNoMatchingDescriptionNoDisplayInTranslationNoOriginalText() {
        when(snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(any())).thenReturn(null);
        var codedData = unmarshallCodeElement("translation-concept-id-no-display-code-no-original-text-example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getCoding().get(0).getCode()).isEqualTo(CONCEPT_ID);
        assertThat(codeableConcept.getCoding().get(0).getDisplay()).isEqualTo(DISPLAY_NAME_2);
        assertThat(codeableConcept.getCoding().get(0).getExtension()).isNullOrEmpty();
    }

    @Test
    public void mapSnomedCodeWithDescriptionIdDescriptionIsPreferredTerm() {
        when(snomedCTDao.getSnomedDescriptionUsingDescriptionId(any())).thenReturn(SNOMED_PREFERRED);
        when(snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(any())).thenReturn(SNOMED_PREFERRED);
        var codedData = unmarshallCodeElement("description-id-example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getCoding().get(0).getCode()).isEqualTo(SNOMED_PREFERRED.getConceptid());
        assertThat(codeableConcept.getCoding().get(0).getDisplay()).isEqualTo(SNOMED_PREFERRED.getTerm());
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().size() == 1).isTrue();
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getUrl()).isEqualTo(DESCRIPTION_ID);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getValue())
            .isEqualTo(new IdType(SNOMED_PREFERRED.getId()));
        assertThat(codeableConcept.getText()).isEqualTo(ORIGINAL_TEXT);
    }

    @Test
    public void mapSnomedCodeWithDescriptionIdPreferredTermDisplayMatchesTermNoOriginalText() {
        when(snomedCTDao.getSnomedDescriptionUsingDescriptionId(any())).thenReturn(SNOMED_PREFERRED);
        when(snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(any())).thenReturn(SNOMED_PREFERRED);
        var codedData = unmarshallCodeElement("description-id-no-original-text-example-1.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getCoding().get(0).getCode()).isEqualTo(SNOMED_PREFERRED.getConceptid());
        assertThat(codeableConcept.getCoding().get(0).getDisplay()).isEqualTo(SNOMED_PREFERRED.getTerm());
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().size() == 1).isTrue();
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getUrl()).isEqualTo(DESCRIPTION_ID);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getValue())
            .isEqualTo(new IdType(SNOMED_PREFERRED.getId()));
        assertThat(codeableConcept.getText()).isEqualTo(null);
    }

    @Test
    public void mapSnomedCodeWithDescriptionIdPreferredTermDisplayDoesNotMatchTermNoOriginalText() {
        when(snomedCTDao.getSnomedDescriptionUsingDescriptionId(any())).thenReturn(SNOMED_PREFERRED);
        when(snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(any())).thenReturn(SNOMED_PREFERRED);
        var codedData = unmarshallCodeElement("description-id-no-original-text-example-2.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getCoding().get(0).getCode()).isEqualTo(SNOMED_PREFERRED.getConceptid());
        assertThat(codeableConcept.getCoding().get(0).getDisplay()).isEqualTo(SNOMED_PREFERRED.getTerm());
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().size() == 1).isTrue();
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getUrl()).isEqualTo(DESCRIPTION_ID);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getValue())
            .isEqualTo(new IdType(SNOMED_PREFERRED.getId()));
        assertThat(codeableConcept.getText()).isEqualTo(DISPLAY_NAME_3);
    }

    @Test
    public void mapSnomedCodeWithDescriptionIdDescriptionIsNotPreferredTerm() {
        when(snomedCTDao.getSnomedDescriptionUsingDescriptionId(any())).thenReturn(SNOMED_DESCRIPTION);
        when(snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(any())).thenReturn(SNOMED_PREFERRED);
        var codedData = unmarshallCodeElement("description-id-example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getCoding().get(0).getCode()).isEqualTo(SNOMED_DESCRIPTION.getConceptid());
        assertThat(codeableConcept.getCoding().get(0).getDisplay()).isEqualTo(SNOMED_PREFERRED.getTerm());
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().size() == 2).isTrue();
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getUrl()).isEqualTo(DESCRIPTION_ID);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getValue())
            .isEqualTo(new IdType(SNOMED_DESCRIPTION.getId()));
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(1).getUrl()).isEqualTo(DESCRIPTION_DISPLAY);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(1).getValue().toString())
            .isEqualTo(SNOMED_DESCRIPTION.getTerm());
        assertThat(codeableConcept.getText()).isEqualTo(ORIGINAL_TEXT);
    }

    @Test
    public void mapSnomedCodeWithDescriptionIdNotPreferredTermDisplayMatchesTermNoOriginalText() {
        when(snomedCTDao.getSnomedDescriptionUsingDescriptionId(any())).thenReturn(SNOMED_DESCRIPTION);
        when(snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(any())).thenReturn(SNOMED_PREFERRED);
        var codedData = unmarshallCodeElement("description-id-no-original-text-example-1.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getCoding().get(0).getCode()).isEqualTo(SNOMED_DESCRIPTION.getConceptid());
        assertThat(codeableConcept.getCoding().get(0).getDisplay()).isEqualTo(SNOMED_PREFERRED.getTerm());
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().size() == 2).isTrue();
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getUrl()).isEqualTo(DESCRIPTION_ID);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getValue())
            .isEqualTo(new IdType(SNOMED_DESCRIPTION.getId()));
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(1).getUrl()).isEqualTo(DESCRIPTION_DISPLAY);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(1).getValue().toString())
            .isEqualTo(SNOMED_DESCRIPTION.getTerm());
        assertThat(codeableConcept.getText()).isEqualTo(null);
    }

    @Test
    public void mapSnomedCodeWithDescriptionIdNotPreferredTermDisplayDoesNotMatchTermNoOriginalText() {
        when(snomedCTDao.getSnomedDescriptionUsingDescriptionId(any())).thenReturn(SNOMED_DESCRIPTION);
        when(snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(any())).thenReturn(SNOMED_PREFERRED);
        var codedData = unmarshallCodeElement("description-id-no-original-text-example-2.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getCoding().get(0).getCode()).isEqualTo(SNOMED_DESCRIPTION.getConceptid());
        assertThat(codeableConcept.getCoding().get(0).getDisplay()).isEqualTo(SNOMED_PREFERRED.getTerm());
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().size() == 2).isTrue();
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getUrl()).isEqualTo(DESCRIPTION_ID);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(0).getValue())
            .isEqualTo(new IdType(SNOMED_DESCRIPTION.getId()));
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(1).getUrl()).isEqualTo(DESCRIPTION_DISPLAY);
        assertThat(codeableConcept.getCoding().get(0).getExtension().get(0).getExtension().get(1).getValue().toString())
            .isEqualTo(SNOMED_DESCRIPTION.getTerm());
        assertThat(codeableConcept.getText()).isEqualTo(DISPLAY_NAME_3);
    }

    @Test
    public void mapSnomedCodeWithDescriptionIdNoMatchingDescriptionWithDisplayNameNoOriginalText() {
        when(snomedCTDao.getSnomedDescriptionUsingDescriptionId(any())).thenReturn(null);
        var codedData = unmarshallCodeElement("description-id-no-original-text-example-1.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo(DISPLAY_NAME_1);
        assertThat(codeableConcept.getCoding()).isNullOrEmpty();
    }

    @Test
    public void mapSnomedCodeWithTranslationDescriptionIdNoMatchingDescriptionWithMainCodeDisplayNameNoOriginalText() {
        when(snomedCTDao.getSnomedDescriptionUsingDescriptionId(any())).thenReturn(null);
        var codedData = unmarshallCodeElement("translation-description-id-no-display-code-no-original-text.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo(DISPLAY_NAME_2);
        assertThat(codeableConcept.getCoding()).hasSize(1);
    }

    @Test
    public void mapSnomedCodeWithDescriptionIdNoMatchingDescriptionWithOriginalTextNoDisplayName() {
        when(snomedCTDao.getSnomedDescriptionUsingDescriptionId(any())).thenReturn(null);
        var codedData = unmarshallCodeElement("description-id-example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo(ORIGINAL_TEXT);
        assertThat(codeableConcept.getCoding()).isNullOrEmpty();
    }

    @Test
    public void mapSnomedCodeForMedicationResourceDescriptionIdExtensionIgnored() {
        when(snomedCTDao.getSnomedDescriptionUsingConceptIdAndDisplayName(any(), any())).thenReturn(SNOMED_PREFERRED);
        when(snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(any())).thenReturn(SNOMED_PREFERRED);
        var codedData = unmarshallCodeElement("concept-id-display-name-original-text-example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConceptForMedication(codedData);

        assertThat(codeableConcept.getCoding().get(0).getCode()).isEqualTo(CONCEPT_ID);
        assertThat(codeableConcept.getCoding().get(0).getDisplay()).isEqualTo(SNOMED_PREFERRED.getTerm());
        assertThat(codeableConcept.getCoding().get(0).getExtension()).isNullOrEmpty();
        assertThat(codeableConcept.getText()).isEqualTo(ORIGINAL_TEXT);
    }

    @Test
    public void mapSnomedCodeForMedicationResourceDescriptionIdAndDisplayExtensionsIgnored() {
        //
        when(snomedCTDao.getSnomedDescriptionUsingConceptIdAndDisplayName(any(), any())).thenReturn(SNOMED_DESCRIPTION);
        when(snomedCTDao.getSnomedDescriptionPreferredTermUsingConceptId(any())).thenReturn(SNOMED_PREFERRED);
        var codedData = unmarshallCodeElement("concept-id-display-name-original-text-example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConceptForMedication(codedData);

        assertThat(codeableConcept.getCoding().get(0).getCode()).isEqualTo("22298006");
        assertThat(codeableConcept.getCoding().get(0).getDisplay()).isEqualTo(SNOMED_PREFERRED.getTerm());
        assertThat(codeableConcept.getCoding().get(0).getExtension()).isNullOrEmpty();
        assertThat(codeableConcept.getText()).isEqualTo(ORIGINAL_TEXT);
    }

    @Test
    public void mapSnomedCodeForMedicationResourceUsingDescriptionId() {
        var codedData = unmarshallCodeElement("description-id-no-original-text-example-1.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConceptForMedication(codedData);

        assertThat(codeableConcept.getCoding()).isNullOrEmpty();
        assertThat(codeableConcept.getText()).isEqualTo(DISPLAY_NAME_1);
    }

    @Test
    public void mapNoSnomedCodeWithOriginalText() {
        var codedData = unmarshallCodeElement("no_snomed_code_with_original_text_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("Inactive Problem, minor");
        assertThat(codeableConcept.getCoding()).hasSize(3);
    }

    @Test
    public void mapNoSnomedCodeWithoutOriginalText() {
        var codedData = unmarshallCodeElement("no_snomed_code_without_original_text_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("O/E - blood pressure reading");
        assertThat(codeableConcept.getCoding()).hasSize(3);
    }

    @Test
    public void mapSnomedCodeInMainWithOriginalText() {
        var codedData = unmarshallCodeElement("snomed_code_with_original_text_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("Inactive Problem, minor");
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("24591000000103");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("O/E - blood pressure reading");
    }

    @Test
    public void mapSnomedCodeInMainWithoutOriginalText() {
        var codedData = unmarshallCodeElement("snomed_code_without_original_text_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.hasText()).isFalse();
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("24591000000103");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("O/E - blood pressure reading");
    }

    @Test
    public void mapSnomedCodeInTranslationWithoutDisplayName() {
        var codedData = unmarshallCodeElement("snomed_code_in_translation_without_display_name_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.hasText()).isFalse();
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("163020007");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("O/E - blood pressure reading");
    }

    @Test
    public void mapSnomedCodeInTranslationWithDisplayName() {
        var codedData = unmarshallCodeElement("snomed_code_in_translation_with_display_name_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.hasText()).isFalse();
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("163020007");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("blood pressure reading");
    }

    @Test
    public void mapSnomedCodeInTranslationWithDisplayNameWithOriginalText() {
        var codedData = unmarshallCodeElement("snomed_code_in_translation_with_display_name_and_original_text_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("Inactive Problem, minor");
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("163020007");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("blood pressure reading");
    }

    @Test
    public void mapKnownNonSnomedCodeInCodeWithOriginalText() {
        var inputXML = """
                <?xml version="1.0" encoding="UTF-8"?>
                <code xmlns="urn:hl7-org:v3" code="ALLERGY138185NEMIS" codeSystem="2.16.840.1.113883.2.1.6.3" displayName="Adverse reaction to Comirnaty Covid-19 mRna Vaccine">
                    <originalText>Test original text</originalText>
                </code>
                """;
        
        var codedData = unmarshallCodeElementFromXMLString(inputXML);
        var codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("Test original text");
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("ALLERGY138185NEMIS");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("https://fhir.hl7.org.uk/Id/egton-codes");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("Adverse reaction to Comirnaty Covid-19 mRna Vaccine");

    }   
    @Test
    public void mapKnownNonSnomedCodeInCodeWithoutOriginalText() {
        var inputXML = """
                <?xml version="1.0" encoding="UTF-8"?>
                <code xmlns="urn:hl7-org:v3" code="ALLERGY138185NEMIS" codeSystem="2.16.840.1.113883.2.1.6.3" displayName="Adverse reaction to Comirnaty Covid-19 mRna Vaccine" />
                """;
        
        var codedData = unmarshallCodeElementFromXMLString(inputXML);
        var codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("Adverse reaction to Comirnaty Covid-19 mRna Vaccine");
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("ALLERGY138185NEMIS");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("https://fhir.hl7.org.uk/Id/egton-codes");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("Adverse reaction to Comirnaty Covid-19 mRna Vaccine");

    }
    
    @Test
    public void mapKnownNonSnomedCodeInValue() {
        var inputXML = """
                <?xml version="1.0" encoding="UTF-8"?>
                <value xmlns="urn:hl7-org:v3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="CD" code="ALLERGY-SNOMED-13482891000006110" codeSystem="2.16.840.1.113883.2.1.6.3" displayName="Adverse reaction to Comirnaty Covid-19 mRna Vaccine" />
                """;

        var codedData = unmarshallCodeElementFromXMLString(inputXML);
        var codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("Adverse reaction to Comirnaty Covid-19 mRna Vaccine");
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("ALLERGY-SNOMED-13482891000006110");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("https://fhir.hl7.org.uk/Id/egton-codes");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("Adverse reaction to Comirnaty Covid-19 mRna Vaccine");
    }

    @Test
    public void mapKnownNonSnomedCodeInTranslationWithSnomedCodeWithOriginalText() {
        var inputXML = """
                <?xml version="1.0" encoding="UTF-8"?>
                <code xmlns="urn:hl7-org:v3" code="24591000000103" codeSystem="2.16.840.1.113883.2.1.3.2.4.15" displayName="O/E - blood pressure reading">
                    <translation code="ALLERGY138185NEMIS" codeSystem="2.16.840.1.113883.2.1.6.3" displayName="Adverse reaction to Comirnaty Covid-19 mRna Vaccine"/>
                    <originalText>Test original text</originalText>
                </code>
                """;

        var codedData = unmarshallCodeElementFromXMLString(inputXML);
        var codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("Test original text");
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("24591000000103");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("O/E - blood pressure reading");
        assertThat(codeableConcept.getCoding().get(1).getCode()).isEqualTo("ALLERGY138185NEMIS");
        assertThat(codeableConcept.getCoding().get(1).getSystem()).isEqualTo("https://fhir.hl7.org.uk/Id/egton-codes");
        assertThat(codeableConcept.getCoding().get(1).getDisplay()).isEqualTo("Adverse reaction to Comirnaty Covid-19 mRna Vaccine");
    }
    
    @Test
    public void mapKnownNonSnomedCodeInTranslationWithSnomedCodeWithoutOriginalText() {
        var inputXML = """
                <?xml version="1.0" encoding="UTF-8"?>
                <code xmlns="urn:hl7-org:v3" code="24591000000103" codeSystem="2.16.840.1.113883.2.1.3.2.4.15" displayName="O/E - blood pressure reading">
                    <translation code="ALLERGY138185NEMIS" codeSystem="2.16.840.1.113883.2.1.6.3" displayName="Adverse reaction to Comirnaty Covid-19 mRna Vaccine"/>
                </code>
                """;

        var codedData = unmarshallCodeElementFromXMLString(inputXML);
        var codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isNull();
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("24591000000103");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("O/E - blood pressure reading");
        assertThat(codeableConcept.getCoding().get(1).getCode()).isEqualTo("ALLERGY138185NEMIS");
        assertThat(codeableConcept.getCoding().get(1).getSystem()).isEqualTo("https://fhir.hl7.org.uk/Id/egton-codes");
        assertThat(codeableConcept.getCoding().get(1).getDisplay()).isEqualTo("Adverse reaction to Comirnaty Covid-19 mRna Vaccine");
    }
    
    @Test
    public void mapKnownNonSnomedCodeInMainCodeAndTranslationWithOriginalText() {
        var inputXML = """
                <?xml version="1.0" encoding="UTF-8"?>
                <code xmlns="urn:hl7-org:v3" code="ALLERGY-SNOMED-13482891000006110" codeSystem="2.16.840.1.113883.2.1.6.3" displayName="Adverse reaction to Comirnaty Covid-19 mRna Vaccine">
                    <translation code="ALLERGY138185NEMIS" codeSystem="2.16.840.1.113883.2.1.6.3" displayName="Translation - Adverse reaction to Comirnaty Covid-19 mRna Vaccine"/>
                    <originalText>Test original text</originalText>
                </code>
                """;

        var codedData = unmarshallCodeElementFromXMLString(inputXML);
        var codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("Test original text");
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("ALLERGY-SNOMED-13482891000006110");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("https://fhir.hl7.org.uk/Id/egton-codes");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("Adverse reaction to Comirnaty Covid-19 mRna Vaccine");
        assertThat(codeableConcept.getCoding().get(1).getCode()).isEqualTo("ALLERGY138185NEMIS");
        assertThat(codeableConcept.getCoding().get(1).getSystem()).isEqualTo("https://fhir.hl7.org.uk/Id/egton-codes");
        assertThat(codeableConcept.getCoding().get(1).getDisplay()).isEqualTo("Translation - Adverse reaction to Comirnaty Covid-19 mRna Vaccine");
    }

    @Test
    public void mapKnownNonSnomedCodeInMainCodeAndTranslationWithoutOriginalText() {
        var inputXML = """
                <?xml version="1.0" encoding="UTF-8"?>
                <code xmlns="urn:hl7-org:v3" code="ALLERGY-SNOMED-13482891000006110" codeSystem="2.16.840.1.113883.2.1.6.3" displayName="Adverse reaction to Comirnaty Covid-19 mRna Vaccine">
                    <translation code="ALLERGY138185NEMIS" codeSystem="2.16.840.1.113883.2.1.6.3" displayName="Translation - Adverse reaction to Comirnaty Covid-19 mRna Vaccine"/>
                </code>
                """;

        var codedData = unmarshallCodeElementFromXMLString(inputXML);
        var codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("Adverse reaction to Comirnaty Covid-19 mRna Vaccine");
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("ALLERGY-SNOMED-13482891000006110");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("https://fhir.hl7.org.uk/Id/egton-codes");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("Adverse reaction to Comirnaty Covid-19 mRna Vaccine");
        assertThat(codeableConcept.getCoding().get(1).getCode()).isEqualTo("ALLERGY138185NEMIS");
        assertThat(codeableConcept.getCoding().get(1).getSystem()).isEqualTo("https://fhir.hl7.org.uk/Id/egton-codes");
        assertThat(codeableConcept.getCoding().get(1).getDisplay()).isEqualTo("Translation - Adverse reaction to Comirnaty Covid-19 mRna Vaccine");
    }
    
    @Test
    public void mapUnknownNonSnomedCodeInMainCode() {
        var inputXML = """
                <?xml version="1.0" encoding="UTF-8"?>
                <code xmlns="urn:hl7-org:v3" code="TESTCODE" codeSystem="TESTCODE_SYSTEM" displayName="TEST_DISPLAY_NAME" />
                """;

        var codedData = unmarshallCodeElementFromXMLString(inputXML);
        var codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("TEST_DISPLAY_NAME");
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("TESTCODE");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("TESTCODE_SYSTEM");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("TEST_DISPLAY_NAME");
    }
    
    @Test
    public void mapUnknownNonSnomedCodeInTranslation() {
        var inputXML = """
                <?xml version="1.0" encoding="UTF-8"?>
                <code xmlns="urn:hl7-org:v3" code="24591000000103" codeSystem="2.16.840.1.113883.2.1.3.2.4.15" displayName="O/E - blood pressure reading">
                    <translation code="TESTCODE" codeSystem="TESTCODE_SYSTEM" displayName="TEST_DISPLAY_NAME"/>
                </code>
                """;

        var codedData = unmarshallCodeElementFromXMLString(inputXML);
        var codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isNull();
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("24591000000103");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("O/E - blood pressure reading");
        assertThat(codeableConcept.getCoding().get(1).getCode()).isEqualTo("TESTCODE");
        assertThat(codeableConcept.getCoding().get(1).getSystem()).isEqualTo("TESTCODE_SYSTEM");
        assertThat(codeableConcept.getCoding().get(1).getDisplay()).isEqualTo("TEST_DISPLAY_NAME");
    }
    
    @SneakyThrows
    private CD unmarshallCodeElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), CD.class);
    }

    @SneakyThrows
    private CD unmarshallCodeElementFromXMLString(String xmlString) {
        return unmarshallString(xmlString, CD.class);
    }
}
