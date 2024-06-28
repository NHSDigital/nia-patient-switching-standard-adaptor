import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import org.hl7.v3.CV;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.hl7.v3.RCMRIN030000UK07Message;
import org.hl7.v3.RCMRMT030101UKEhrFolder;

import org.junit.jupiter.api.Test;

import utility.FileFactory;

class JaxbTest {
    private static final String RCMR_IN030000UK06_TEST_FILE_NAME = "RCMR_IN030000UK06.xml";
    private static final String RCMR_IN030000UK07_TEST_FILE_NAME = "RCMR_IN030000UK07.xml";

    @Test
    void When_RCMRIN030000UK06MessageIsUnmarshalled_Expect_FieldsToBeParsable() throws JAXBException {
        // given
        final File file = FileFactory.getFileFor(RCMR_IN030000UK06_TEST_FILE_NAME);
        JAXBElement<RCMRIN030000UK06Message> unmarshalledMessage = unmarshallFromFile(file, RCMRIN030000UK06Message.class);

        // when
        var ehrFolder = unmarshalledMessage
            .getValue()
            .getControlActEvent()
            .getSubject()
            .getEhrExtract()
            .getComponent()
            .get(0)
            .getEhrFolder();

        var person = ehrFolder.getResponsibleParty().getAgentDirectory().getPart().get(2).getAgent().getAgentPerson();
        var place = ehrFolder.getComponent().get(0).getEhrComposition().getLocation().getLocatedEntity().getLocatedPlace();

        // then
        assertThat(person.getName().getFamily()).isEqualTo("Whitcombe");
        assertThat(person.getName().getGiven()).isEqualTo("Peter");
        assertThat(person.getName().getPrefix()).isEqualTo("Dr");
        assertThat(person.getName().getValidTime().getCenter().getValue()).isEqualTo("20100114");
        assertThat(place.getName()).isEqualTo("EMIS Test Practice Location");
    }

    @Test
    void When_RCMRIN030000UK07MessageUnmarshalled_Expect_ConfidentialityCodePresent() throws JAXBException {
        // given
        final File file = FileFactory.getFileFor(RCMR_IN030000UK07_TEST_FILE_NAME);
        final JAXBElement<RCMRIN030000UK07Message> unmarshalledMessage = unmarshallFromFile(file, RCMRIN030000UK07Message.class);
        final String code = "NOPAT";
        final String codeSystem = "2.16.840.1.113883.4.642.3.47";
        final String displayName = "no disclosure to patient, family or caregivers without attending provider's authorization";

        // when
        final RCMRMT030101UKEhrFolder ehrFolder = unmarshalledMessage
            .getValue()
            .getControlActEvent()
            .getSubject()
            .getEhrExtract()
            .getComponent()
            .get(0)
            .getEhrFolder();

        final CV observationStatementConfidentialityCode = ehrFolder
            .getComponent()
            .get(0)
            .getEhrComposition()
            .getComponent()
            .get(0)
            .getObservationStatement()
            .getConfidentialityCode()
            .orElseThrow();

        final CV medicationStatementConfidentialityCode = ehrFolder
            .getComponent()
            .get(2)
            .getEhrComposition()
            .getComponent()
            .get(1)
            .getMedicationStatement()
            .getConfidentialityCode()
            .orElseThrow();

        // then
        assertThat(medicationStatementConfidentialityCode.getCode()).isEqualTo(code);
        assertThat(medicationStatementConfidentialityCode.getCodeSystem()).isEqualTo(codeSystem);
        assertThat(medicationStatementConfidentialityCode.getDisplayName()).isEqualTo(displayName);
        assertThat(observationStatementConfidentialityCode.getCode()).isEqualTo(code);
        assertThat(observationStatementConfidentialityCode.getCodeSystem()).isEqualTo(codeSystem);
        assertThat(observationStatementConfidentialityCode.getDisplayName()).isEqualTo(displayName);
    }

    // Helper Methods
    private <T> JAXBElement<T> unmarshallFromFile(File source, Class<T> destination) throws JAXBException {
        final JAXBContext context = JAXBContext.newInstance(destination);
        final Unmarshaller unmarshaller = context.createUnmarshaller();

        return (JAXBElement<T>) unmarshaller.unmarshal(source);
    }
}
