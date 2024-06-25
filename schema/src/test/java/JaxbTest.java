import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.hl7.v3.RCMRIN030000UK06Message;
import org.junit.jupiter.api.Test;

public class JaxbTest {

    public static final File TEST_FILE = new File("src/test/resources/xml/RCMR_IN030000UK06.xml");

    @Test
    void When_RCMRIN030000UK06MessageIsUnmarshalled_Expect_LooksOk() throws JAXBException {
        // given
        final JAXBElement<RCMRIN030000UK06Message> unmarshalledMessage
            = unmarshallFromFile(TEST_FILE, RCMRIN030000UK06Message.class);

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

        assertThat(person.getName().getFamily()).isEqualTo("Whitcombe");
        assertThat(person.getName().getGiven()).isEqualTo("Peter");
        assertThat(person.getName().getPrefix()).isEqualTo("Dr");
        assertThat(person.getName().getValidTime().getCenter().getValue()).isEqualTo("20100114");
        assertThat(place.getName()).isEqualTo("EMIS Test Practice Location");
    }

    // Helper Methods
    private <T> JAXBElement<T> unmarshallFromFile(final File source, Class<T> target) throws JAXBException {
        final JAXBContext jaxbContext = JAXBContext.newInstance(target);
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        return (JAXBElement<T>) unmarshaller.unmarshal(source);
    }
}
