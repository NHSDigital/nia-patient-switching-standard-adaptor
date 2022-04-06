package uk.nhs.adaptors.pss.translator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.storage.StorageDataUploadWrapper;
import uk.nhs.adaptors.pss.translator.storage.StorageException;
import uk.nhs.adaptors.pss.translator.storage.StorageManagerService;

import javax.xml.bind.ValidationException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AttachmentHandlerService {

    private final StorageManagerService storageManagerService;

    public void storeAttachments(List<InboundMessage.Attachment> attachments, String conversationId) throws ValidationException {

        if (conversationId == null || conversationId.isEmpty()) {
            throw new ValidationException("ConversationId cannot be null or empty");
        }

        if (attachments != null) {
            attachments.forEach((InboundMessage.Attachment attachment) -> {

                try {
                    // Filename format will be further defined with later tickets
                    StorageDataUploadWrapper dataWrapper = new StorageDataUploadWrapper(
                        attachment.getContentType(),
                        conversationId,
                        Base64.getDecoder().decode(attachment.getPayload())
                    );

                    String filename = conversationId + "-"
                        + Timestamp.from(Instant.now()).toString().replace(" ", "-")
                        + "." + attachment.getContentType()
                        + attachment.getContentType();

                    storageManagerService.uploadFile(filename, dataWrapper);
                } catch (StorageException ex)  {
                    // We don't want to stop uploading a list of failures but we should log them here
                    // this is for a later ticket to manage
                }
            });
        }
    }

}
