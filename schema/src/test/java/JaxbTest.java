import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.hl7.v3.CV;
import org.hl7.v3.RCCTMT120101UK01Person;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.hl7.v3.RCMRIN030000UK07Message;
import org.hl7.v3.RCMRMT030101UK04Place;
import org.hl7.v3.RCMRMT030101UKEhrFolder;
import org.junit.jupiter.api.Test;

public class JaxbTest {
    private static final File RCMR_IN030000UK06_TEST_FILE = new File("src/test/resources/xml/RCMR_IN030000UK06.xml");
    private static final File RCMR_IN030000UK07_TEST_FILE = new File("src/test/resources/xml/RCMR_IN030000UK07.xml");

    @Test
    void When_RCMRIN030000UK06MessageIsUnmarshalled_Expect_PersonAndPlacePresent() throws JAXBException {
        // given
        final String familyName = "Whitcombe";
        final String givenName = "Peter";
        final String prefix = "Dr";
        final String time = "20100114";
        final String practiceLocation = "EMIS Test Practice Location";
        final JAXBElement<RCMRIN030000UK06Message> unmarshalledMessage
            = unmarshallFromFile(RCMR_IN030000UK06_TEST_FILE, RCMRIN030000UK06Message.class);

        // when
        final RCMRMT030101UKEhrFolder ehrFolder = unmarshalledMessage
            .getValue()
            .getControlActEvent()
            .getSubject()
            .getEhrExtract()
            .getComponent()
            .get(0)
            .getEhrFolder();

        final RCCTMT120101UK01Person person = ehrFolder.getResponsibleParty()
            .getAgentDirectory()
            .getPart()
            .get(2)
            .getAgent()
            .getAgentPerson();

        final RCMRMT030101UK04Place place = ehrFolder.getComponent()
            .get(0)
            .getEhrComposition()
            .getLocation()
            .getLocatedEntity()
            .getLocatedPlace();

        // then
        assertThat(person.getName().getFamily()).isEqualTo(familyName);
        assertThat(person.getName().getGiven()).isEqualTo(givenName);
        assertThat(person.getName().getPrefix()).isEqualTo(prefix);
        assertThat(person.getName().getValidTime().getCenter().getValue()).isEqualTo(time);
        assertThat(place.getName()).isEqualTo(practiceLocation);
    }

    @Test
    void When_RCMRIN030000UK07MessageIsUnmarshalled_Expect_ConfidentialityCodePresent() throws JAXBException {
        // given
        final String code = "NOPAT";
        final String codeSystem = "2.16.840.1.113883.4.642.3.47";
        final String displayName = "no disclosure to patient, family or caregivers without attending provider's authorization";
        final JAXBElement<RCMRIN030000UK07Message> unmarshalledMessage
            = unmarshallFromFile(RCMR_IN030000UK07_TEST_FILE, RCMRIN030000UK07Message.class);

        // when
        final RCMRMT030101UKEhrFolder ehrFolder = unmarshalledMessage
            .getValue()
            .getControlActEvent()
            .getSubject()
            .getEhrExtract()
            .getComponent()
            .get(0)
            .getEhrFolder();

        final CV confidentialityCode = ehrFolder
            .getComponent()
            .get(0)
            .getEhrComposition()
            .getComponent()
            .get(0)
            .getObservationStatement()
            .getConfidentialityCode()
            .orElseThrow();

        // then
        assertThat(confidentialityCode.getCode()).isEqualTo(code);
        assertThat(confidentialityCode.getCodeSystem()).isEqualTo(codeSystem);
        assertThat(confidentialityCode.getDisplayName()).isEqualTo(displayName);
    }

    private <T> JAXBElement<T> unmarshallFromFile(final File source, Class<T> target) throws JAXBException {
        final JAXBContext jaxbContext = JAXBContext.newInstance(target);
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        return (JAXBElement<T>) unmarshaller.unmarshal(source);
    }
}