package uk.nhs.adaptors.pss.translator.service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.xml.bind.ValidationException;

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

        if (attachments == null) {
            return payloadStr;
        }

        String resultPayload = payloadStr;

        Set<String> expectedFilenames = attachments.stream()
            .filter(attachment -> !XmlParseUtilService.parseIsSkeleton(attachment.getDescription()))
            .map(this::convertAttachmentToInlineAttachment)
            .map(InlineAttachment::getOriginalFilename)
            .collect(Collectors.toCollection(HashSet::new));

        Pattern pattern = Pattern.compile(wrapWithReferenceElement("file://localhost/([^\"]+)"));
        Matcher matcher = pattern.matcher(payloadStr);

        while (matcher.find()) {
            String decodedFilename = UriUtils.decode(matcher.group(1), StandardCharsets.UTF_8);

            if (expectedFilenames.contains(decodedFilename)) {

                String fileLocation = storageManagerService.getFileLocation(decodedFilename, conversationId);
                String referenceElement = wrapWithReferenceElement(xmlEscape(fileLocation));

                resultPayload = resultPayload.replace(matcher.group(0), referenceElement);

                expectedFilenames.remove(decodedFilename);
            }
        }

        if (!expectedFilenames.isEmpty()) {
            throw new AttachmentNotFoundException("Unable to find attachment(s): " + expectedFilenames);
        }

        return resultPayload;

    }

    private String xmlEscape(String str) {
        return StringEscapeUtils.escapeXml10(str);
    }

    private String wrapWithReferenceElement(String filePattern) {
        return "<reference value=\"" + filePattern + "\"";
    }

    private InlineAttachment convertAttachmentToInlineAttachment(InboundMessage.Attachment attachment) {
        try {
            return InlineAttachment.fromInboundMessageAttachment(attachment);
        } catch (ParseException e) {
            throw new InlineAttachmentProcessingException("Unable to parse inline attachment description: " + e.getMessage(), e);
        }
    }
}
