import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import org.hl7.v3.RCMRIN030000UK06Message;
import org.junit.jupiter.api.Test;

public class JaxbTest {

    public static final File TEST_FILE = new File("src/test/resources/xml/9465698490_Daniels_full_20210602.xml");

    @Test
    public void testUnmarshal() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(RCMRIN030000UK06Message.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        JAXBElement<RCMRIN030000UK06Message> unmarshalledMessage = (JAXBElement) unmarshaller.unmarshal(TEST_FILE);

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
}
