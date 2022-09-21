package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class ContentTypeService {

    public static final String MHS_OUTBOUND_CONTENT_TYPE = "multipart/related; boundary=\"--=_MIME-Boundary\"; type=text/xml; " +
        "start=ebXMLHeader@spine.nhs.uk";

    public static final String AMENDED_CONTENT_TYPE = "multipart/related; boundary=\"--=_MIME-Boundary\"; type=\"text/xml\"; " +
        "start=\"ebXMLHeader@spine.nhs.uk\"";

    private static final Set<String> alteredMessages = new HashSet<>();

    public void markAsAlteredContentType(String messageId) {
        alteredMessages.add(messageId);
    }

    public boolean hasAlteredContentType(String messageId) {
        return alteredMessages.contains(messageId);
    }
}

