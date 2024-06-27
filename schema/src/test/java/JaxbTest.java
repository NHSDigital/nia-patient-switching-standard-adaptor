import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.junit.jupiter.api.Test;

class JaxbTest {
    private static final String BASE_FILE_PATH = "src/test/resources/xml/";
    private static final File RCMR_IN030000UK06_TEST_FILE = new File(BASE_FILE_PATH + "RCMR_IN030000UK06.xml");
    private static final File RCMR_IN030000UK07_TEST_FILE = new File(BASE_FILE_PATH + "RCMR_IN030000UK07.xml");

    @Test
    void When_RCMRIN030000UK07MessageIsUnmarshalled_Expect_FieldsToBeParsable() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(RCMRIN030000UK06Message.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        JAXBElement<RCMRIN030000UK06Message> unmarshalledMessage = (JAXBElement) unmarshaller.unmarshal(RCMR_IN030000UK06_TEST_FILE);

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
