package uk.nhs.adaptors.pss.translator.service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.bind.ValidationException;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.pss.translator.exception.AttachmentNotFoundException;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.InlineAttachment;
import uk.nhs.adaptors.pss.translator.storage.StorageManagerService;
import uk.nhs.adaptors.pss.translator.util.XmlParseUtilService;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AttachmentReferenceUpdaterService {

    private final StorageManagerService storageManagerService;

    public String updateReferenceToAttachment(List<InboundMessage.Attachment> attachments, String conversationId, String payloadStr)
            throws ValidationException, AttachmentNotFoundException, InlineAttachmentProcessingException {

        if (conversationId == null || conversationId.isEmpty()) {
            throw new ValidationException("ConversationId cannot be null or empty");
        }

        String resultPayload = payloadStr;

        if (attachments != null) {

            Map<String, String> referenceMap = findReferences(resultPayload);

            for (InboundMessage.Attachment attachment : attachments) {

                try {
                    if (!XmlParseUtilService.parseIsSkeleton(attachment.getDescription())) {
                        InlineAttachment inlineAttachment = new InlineAttachment(attachment);
                        String filename = inlineAttachment.getOriginalFilename();

                        if (referenceMap.containsKey(filename)) {
                            String fileLocation = storageManagerService.getFileLocation(filename, conversationId);

                            Pattern pattern = Pattern.compile(wrapWithReferenceElement(referenceMap.get(filename)));
                            Matcher matcher = pattern.matcher(resultPayload);

                            resultPayload = matcher.replaceAll(wrapWithReferenceElement(xmlEscape(fileLocation)));

                        } else {
                            var message = String.format("Could not find file %s in payload", filename);
                            throw new AttachmentNotFoundException(message);
                        }
                    }
                } catch (ParseException ex) {
                    throw new InlineAttachmentProcessingException("Unable to parse inline attachment description: " + ex.getMessage(), ex);
                }
            }
        }

        return resultPayload;
    }

    private String xmlEscape(String str) {
        return StringEscapeUtils.escapeXml10(str);
    }

    /**
     *
     * @param payload The payload to search
     * @return a map where the decoded filename is the key and the encoded URL is the value
     */
    private Map<String, String> findReferences(String payload) {

        Pattern pattern = Pattern.compile(wrapWithReferenceElement("(file://localhost/([^\"]+))"));
        Matcher matcher = pattern.matcher(payload);

        return matcher.results()
            .collect(
                Collectors.toMap(
                    matchResult -> UriUtils.decode(matchResult.group(2), StandardCharsets.UTF_8),
                    matchResult -> matchResult.group(1)
                )
            );
    }

    private String wrapWithReferenceElement(String filePattern) {
        return "<reference value=\"" + filePattern + "\"";
    }
}
