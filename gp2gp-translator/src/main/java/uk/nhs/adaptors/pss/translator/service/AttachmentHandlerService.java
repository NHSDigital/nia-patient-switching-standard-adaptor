package uk.nhs.adaptors.pss.translator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.exception.SkeletonEhrProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.InlineAttachment;
import uk.nhs.adaptors.pss.translator.storage.StorageDataUploadWrapper;
import uk.nhs.adaptors.pss.translator.storage.StorageException;
import uk.nhs.adaptors.pss.translator.storage.StorageManagerService;

import javax.xml.bind.ValidationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AttachmentHandlerService {

    private final StorageManagerService storageManagerService;

    public void storeAttachments(List<InboundMessage.Attachment> attachments, String conversationId) throws ValidationException,
        InlineAttachmentProcessingException {

        if (!StringUtils.hasText(conversationId)) {
            throw new ValidationException("ConversationId cannot be null or empty");
        }

        if (attachments != null) {

            for (InboundMessage.Attachment attachment : attachments) {

                try {

                    InlineAttachment inlineAttachment = new InlineAttachment(attachment);

                    byte[] decodedPayload = Base64.getMimeDecoder().decode(inlineAttachment.getPayload());

                    byte[] payload;

                    if (inlineAttachment.isCompressed()) {
                        GZIPInputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(decodedPayload));
                        payload = inputStream.readAllBytes();
                    } else {
                        payload = decodedPayload;
                    }

                    StorageDataUploadWrapper dataWrapper = new StorageDataUploadWrapper(
                        attachment.getContentType(),
                        conversationId,
                        payload
                    );

                    String filename = inlineAttachment.getOriginalFilename();

                    storageManagerService.uploadFile(filename, dataWrapper);
                } catch (StorageException ex) {
                    throw new InlineAttachmentProcessingException("Unable to upload inline attachment to storage: " + ex.getMessage());
                } catch (IOException ex) {
                    throw new InlineAttachmentProcessingException("Unable to decompress attachment: " + ex.getMessage());
                } catch (ParseException ex) {
                    throw new InlineAttachmentProcessingException("Unable to parse inline attachment description: " + ex.getMessage());
                }
            }
        }
    }

    public void storeEhrExtract(String fileName, String payload, String conversationId, String contentType) throws ValidationException, StorageException, SkeletonEhrProcessingException {
        if (!StringUtils.hasText(fileName)) {
            throw new ValidationException("FileName cannot be null or empty");
        }
        if (!StringUtils.hasText(payload)) {
            throw new ValidationException("Payload cannot be null or empty");
        }
        if (!StringUtils.hasText(conversationId)) {
            throw new ValidationException("ConversationId cannot be null or empty");
        }
        if (!StringUtils.hasText(contentType)) {
            throw new ValidationException("ContentType cannot be null or empty");
        }

        StorageDataUploadWrapper dataWrapper = new StorageDataUploadWrapper(
                contentType,
                conversationId,
                payload.getBytes(StandardCharsets.UTF_8)
        );

        try {
            storageManagerService.uploadFile(fileName, dataWrapper);
        } catch (StorageException ex) {
            throw new SkeletonEhrProcessingException("Unable to upload EhrExtract to storage: " + ex.getMessage());
        }
    }
}
