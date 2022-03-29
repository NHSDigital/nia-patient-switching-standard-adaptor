package uk.nhs.adaptors.pss.translator.service;

import org.springframework.stereotype.Service;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.storage.StorageManagerService;
import uk.nhs.adaptors.pss.translator.storage.StorageService;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
public class AttachmentHandlerService {

    public StorageManagerService _storageManagerService;

    public AttachmentHandlerService(StorageManagerService storageManagerService){
        _storageManagerService = storageManagerService;
    }

    public void StoreAttachments(List<InboundMessage.Attachment> attachments) {
        attachments.forEach((InboundMessage.Attachment attachment) -> {

            byte[] decodedPayload = Base64.getDecoder().decode(attachment.getPayload());

            _storageManagerService.UploadFile(
                    Timestamp.from(Instant.now()).toString().replace(" ", "-") + "." + attachment.getContentType(),
                    decodedPayload
            );
        });
    }

}
