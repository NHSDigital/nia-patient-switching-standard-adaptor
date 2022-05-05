package uk.nhs.adaptors.pss.translator.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.bind.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.ion.NullValueException;
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.InlineAttachment;
import uk.nhs.adaptors.pss.translator.storage.StorageDataUploadWrapper;
import uk.nhs.adaptors.pss.translator.storage.StorageException;
import uk.nhs.adaptors.pss.translator.storage.StorageManagerService;

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

    public byte[] getAttachment(String filename) {
        if (!StringUtils.hasText(filename)) {
            throw new NullValueException();
        }
        return storageManagerService.downloadFile(filename);
    }

    public void removeAttachment(String filename) {
        if (!StringUtils.hasText(filename)) {
            throw new NullValueException();
        }
        storageManagerService.deleteFile(filename);
    }

    public String buildSingleFileStringFromPatientAttachmentLogs(List<PatientAttachmentLog> attachmentLogs) {
        StringBuilder combinedFile = new StringBuilder("");
        for (PatientAttachmentLog log : attachmentLogs) {
            var filename = log.getFilename();
            var attachmentBytes = getAttachment(filename);
            combinedFile.append(new String(attachmentBytes, StandardCharsets.UTF_8));
        }

        return combinedFile.toString();
    }

    public List<InboundMessage.Attachment> buildInboundAttachmentsFromAttachmentLogs(
        List<PatientAttachmentLog> attachmentLogs,
        List<String> payloads) {

        List<InboundMessage.Attachment> attachmentsResponse = new ArrayList<InboundMessage.Attachment>();

        if (payloads == null) {
            payloads = new ArrayList<String>();
        }


        for (var  i =0; i < attachmentLogs.size(); i++) {
            var log = attachmentLogs.get(i);

            var fileDescription =
                "Filename=" + "\"" + log.getFilename()  + "\" "
                    + "ContentType=" + log.getContentType() + " "
                    + "Compressed=" + log.getCompressed().toString() + " "
                    + "LargeAttachment=" + log.getLargeAttachment().toString() + " "
                    + "OriginalBase64=" + log.getBase64().toString() + " "
                    + "Length=" + log.getLengthNum();

            var payload = "";
            if (payloads.get(i) == null) {
                payload = getAttachment(log.getFilename()).toString();
            } else {
                payload = payloads.get(i);
            }

            attachmentsResponse.add(
                InboundMessage.Attachment.builder()
                    .payload(payload)
                        .isBase64(log
                        .getBase64()
                        .toString())
                    .contentType(log.getContentType())
                    .description(fileDescription)
                    .build()
            );
        }

        return attachmentsResponse;
    }
}
