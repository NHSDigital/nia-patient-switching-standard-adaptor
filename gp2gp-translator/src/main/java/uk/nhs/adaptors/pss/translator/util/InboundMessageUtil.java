package uk.nhs.adaptors.pss.translator.util;


import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.service.XPathService;

@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class InboundMessageUtil {

    private static final String PATH_TO_TIMESTAMP = "/Envelope/Header/MessageHeader/MessageData/Timestamp";
    private ObjectMapper objectMapper;

    private XPathService xPathService;

    public InboundMessage readMessage(String message) throws JsonProcessingException {
        return objectMapper.readValue(message, InboundMessage.class);
    }

    public ZonedDateTime parseMessageTimestamp(String ebXML) throws SAXException, DateTimeParseException {
        Document document = xPathService.parseDocumentFromXml(ebXML);
        return ZonedDateTime.parse(xPathService.getNodeValue(document, PATH_TO_TIMESTAMP));
    }
}
