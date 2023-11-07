package uk.nhs.adaptors.pss.translator.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class XmlUnmarshallUtil {

    public static <T> T unmarshallFile(File xmlFile, Class<T> destinationClass) throws JAXBException {
        Unmarshaller unmarshaller = createUnmarshaller(destinationClass);
        JAXBElement<T> unmarshalledMessage = (JAXBElement) unmarshaller.unmarshal(xmlFile);
        return unmarshalledMessage.getValue();
    }

    public static <T> T unmarshallString(String xmlString, Class<T> destinationClass) throws JAXBException {
        Unmarshaller unmarshaller = createUnmarshaller(destinationClass);
        JAXBElement<T> unmarshalledMessage = (JAXBElement) unmarshaller.unmarshal(
            IOUtils.toInputStream(xmlString, UTF_8)
        );
        return unmarshalledMessage.getValue();
    }

    private static <T> Unmarshaller createUnmarshaller(Class<T> destinationClass) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(destinationClass);
        return jaxbContext.createUnmarshaller();
    }
}
