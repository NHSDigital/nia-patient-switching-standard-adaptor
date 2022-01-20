package uk.nhs.adaptors.pss.translator.util;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.hl7.v3.ANY;

public class XmlUnmarshallUtil {
    public static <T extends ANY> T unmarshallFile(String xmlFilePath, Class<T> destinationClass) throws JAXBException {
        File xmlFile = new File(xmlFilePath);
        JAXBContext jaxbContext = JAXBContext.newInstance(destinationClass);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<T> unmarshalledMessage = (JAXBElement) unmarshaller.unmarshal(xmlFile);
        return unmarshalledMessage.getValue();
    }
}
