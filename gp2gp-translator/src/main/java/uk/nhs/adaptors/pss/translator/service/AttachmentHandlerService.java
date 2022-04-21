package uk.nhs.adaptors.pss.translator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.InlineAttachment;
import uk.nhs.adaptors.pss.translator.storage.StorageDataUploadWrapper;
import uk.nhs.adaptors.pss.translator.storage.StorageException;
import uk.nhs.adaptors.pss.translator.storage.StorageManagerService;

import javax.xml.bind.ValidationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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

        if (conversationId == null || conversationId.isEmpty()) {
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
                    // We don't want to stop uploading a list of failures but we should log them here
                    // this is for a later ticket to manage
                } catch (IOException ex) {
                    throw new InlineAttachmentProcessingException("Unable to decompress attachment: " + ex.getMessage());
                } catch (ParseException ex) {
                    throw new InlineAttachmentProcessingException("Unable to parse inline attachment description: " + ex.getMessage());
                }
            }
        }
    }
}
