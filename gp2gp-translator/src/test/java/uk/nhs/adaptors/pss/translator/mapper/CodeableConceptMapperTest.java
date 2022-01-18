package uk.nhs.adaptors.pss.translator.mapper;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.v3.CV;
import org.junit.jupiter.api.Test;

public class CodeableConceptMapperTest {
    public static final File TEST_FILE = new File("src/test/resources/xml/code_example.xml");

    private final CodeableConceptMapper codeableConceptMapper = new CodeableConceptMapper();

    @Test
    public void testMapToCodeableConcept() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(CV.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        JAXBElement<CV> unmarshalledMessage = (JAXBElement) unmarshaller.unmarshal(TEST_FILE);

        CodeableConcept codeableConcept = codeableConceptMapper.mapToCodeableConcept(unmarshalledMessage.getValue());
    }
}
