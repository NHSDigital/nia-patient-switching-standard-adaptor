package uk.nhs.adaptors.pss.translator.mapper;

import lombok.SneakyThrows;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.v3.CD;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.nhs.adaptors.pss.translator.testutil.XmlUnmarshallUtil.unmarshallFile;

public class CodeableConceptMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/CD/";

    private final CodeableConceptMapper codeableConceptMapper = new CodeableConceptMapper();

    @Test
    public void mapNoSnomedCodeWithOriginalText() {
        var codedData = unmarshallCodeElement("no_snomed_code_with_original_text_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("Inactive Problem, minor");
        assertThat(codeableConcept.hasCoding()).isFalse();
    }

    @Test
    public void mapNoSnomedCodeWithoutOriginalText() {
        var codedData = unmarshallCodeElement("no_snomed_code_without_original_text_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("O/E - blood pressure reading");
        assertThat(codeableConcept.hasCoding()).isFalse();
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

    @SneakyThrows
    private CD unmarshallCodeElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), CD.class);
    }
}
