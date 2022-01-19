package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.v3.CD;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.junit.jupiter.api.Test;

public class CodeableConceptMapperTest {
    private static final String XML_RESOURCES_PATH = "src/test/resources/xml/";
    private final CodeableConceptMapper codeableConceptMapper = new CodeableConceptMapper();

    @Test
    public void mapNoSnomedCodeWithOriginalText() throws JAXBException {
        var codedData = unmarshallMessage(XML_RESOURCES_PATH + "no_snomed_code_with_original_text_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("Inactive Problem, minor");
        assertThat(codeableConcept.hasCoding()).isFalse();
    }

    @Test
    public void mapNoSnomedCodeWithoutOriginalText() throws JAXBException {
        var codedData = unmarshallMessage(XML_RESOURCES_PATH + "no_snomed_code_without_original_text_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("O/E - blood pressure reading");
        assertThat(codeableConcept.hasCoding()).isFalse();
    }

    @Test
    public void mapSnomedCodeInMainWithOriginalText() throws JAXBException {
        var codedData = unmarshallMessage(XML_RESOURCES_PATH + "snomed_code_with_original_text_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("Inactive Problem, minor");
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("24591000000103");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("O/E - blood pressure reading");
    }

    @Test
    public void mapSnomedCodeInMainWithoutOriginalText() throws JAXBException {
        var codedData = unmarshallMessage(XML_RESOURCES_PATH + "snomed_code_without_original_text_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.hasText()).isFalse();
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("24591000000103");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("O/E - blood pressure reading");
    }

    @Test
    public void mapSnomedCodeInTranslationWithoutDisplayName() throws JAXBException {
        var codedData = unmarshallMessage(XML_RESOURCES_PATH + "snomed_code_in_translation_without_display_name_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.hasText()).isFalse();
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("163020007");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("O/E - blood pressure reading");
    }

    @Test
    public void mapSnomedCodeInTranslationWithDisplayName() throws JAXBException {
        var codedData = unmarshallMessage(XML_RESOURCES_PATH + "snomed_code_in_translation_with_display_name_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.hasText()).isFalse();
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("163020007");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("blood pressure reading");
    }

    @Test
    public void mapSnomedCodeInTranslationWithDisplayNameWithOriginalText() throws JAXBException {
        var codedData = unmarshallMessage(XML_RESOURCES_PATH
            + "snomed_code_in_translation_with_display_name_and_original_text_example.xml");

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(codedData);

        assertThat(codeableConcept.getText()).isEqualTo("Inactive Problem, minor");
        assertThat(codeableConcept.getCodingFirstRep().getCode()).isEqualTo("163020007");
        assertThat(codeableConcept.getCodingFirstRep().getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("blood pressure reading");
    }

    private CD unmarshallMessage(String xmlFilePath) throws JAXBException {
        File xmlFile = new File(xmlFilePath);
        JAXBContext jaxbContext = JAXBContext.newInstance(RCMRIN030000UK06Message.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<RCMRIN030000UK06Message> unmarshalledMessage = (JAXBElement) unmarshaller.unmarshal(xmlFile);
        return unmarshalledMessage
            .getValue()
            .getControlActEvent()
            .getSubject()
            .getEhrExtract()
            .getComponent()
            .get(0)
            .getEhrFolder()
            .getComponent()
            .get(0)
            .getEhrComposition()
            .getCode();
    }
}
