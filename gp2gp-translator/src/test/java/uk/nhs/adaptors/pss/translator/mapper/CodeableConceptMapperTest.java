package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import javax.xml.bind.JAXBException;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.v3.CD;
import org.junit.jupiter.api.Test;

import uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil;

public class CodeableConceptMapperTest {
    private static final String XML_RESOURCES_PATH = "src/test/resources/xml/";
    private final CodeableConceptMapper codeableConceptMapper = new CodeableConceptMapper();

    @Test
    public void mapNoSnomedCodeWithOriginalText() throws JAXBException {
        var codedData = unmarshallCodeElement("no_snomed_code_with_original_text_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("Inactive Problem, minor");
        assertThat(codeableConcept.hasCoding()).isFalse();
    }

    @Test
    public void mapNoSnomedCodeWithoutOriginalText() throws JAXBException {
        var codedData = unmarshallCodeElement("no_snomed_code_without_original_text_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("O/E - blood pressure reading");
        assertThat(codeableConcept.hasCoding()).isFalse();
    }

    @Test
    public void mapSnomedCodeInMainWithOriginalText() throws JAXBException {
        var codedData = unmarshallCodeElement("snomed_code_with_original_text_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("Inactive Problem, minor");
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("24591000000103");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("O/E - blood pressure reading");
    }

    @Test
    public void mapSnomedCodeInMainWithoutOriginalText() throws JAXBException {
        var codedData = unmarshallCodeElement("snomed_code_without_original_text_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.hasText()).isFalse();
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("24591000000103");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("O/E - blood pressure reading");
    }

    @Test
    public void mapSnomedCodeInTranslationWithoutDisplayName() throws JAXBException {
        var codedData = unmarshallCodeElement("snomed_code_in_translation_without_display_name_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.hasText()).isFalse();
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("163020007");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("O/E - blood pressure reading");
    }

    @Test
    public void mapSnomedCodeInTranslationWithDisplayName() throws JAXBException {
        var codedData = unmarshallCodeElement("snomed_code_in_translation_with_display_name_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.hasText()).isFalse();
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("163020007");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("blood pressure reading");
    }

    @Test
    public void mapSnomedCodeInTranslationWithDisplayNameWithOriginalText() throws JAXBException {
        var codedData = unmarshallCodeElement("snomed_code_in_translation_with_display_name_and_original_text_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("Inactive Problem, minor");
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("163020007");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("blood pressure reading");
    }

    private CD unmarshallCodeElement(String fileName) throws JAXBException {
        return XmlUnmarshallUtil.unmarshallFile(XML_RESOURCES_PATH + fileName, CD.class);
    }
}
