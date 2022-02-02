package uk.nhs.adaptors.pss.translator.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class XmlUnmarshallUtil {
    public static <T> T unmarshallFile(File xmlFile, Class<T> destinationClass) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(destinationClass);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<T> unmarshalledMessage = (JAXBElement) unmarshaller.unmarshal(xmlFile);
        return unmarshalledMessage.getValue();
    }
}
