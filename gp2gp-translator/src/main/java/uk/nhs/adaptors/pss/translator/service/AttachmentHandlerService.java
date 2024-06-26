package uk.nhs.adaptors.pss.translator.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;

import jakarta.xml.bind.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;
import uk.nhs.adaptors.pss.translator.config.SupportedFileTypes;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.exception.UnsupportedFileTypeException;
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
    private final SupportedFileTypes supportedFileTypes;

    public void storeAttachments(List<InboundMessage.Attachment> attachments, String conversationId) throws ValidationException,
        InlineAttachmentProcessingException, UnsupportedFileTypeException {

        byte[] decodedPayload;
        byte[] payload;

        if (!StringUtils.hasText(conversationId)) {
            throw new ValidationException("ConversationId cannot be null or empty");
        }

        if (attachments != null) {
            for (InboundMessage.Attachment attachment : attachments) {
                try {
                    InlineAttachment inlineAttachment = InlineAttachment.fromInboundMessageAttachment(attachment);

                    String contentType = inlineAttachment.getContentType();
                    if (!checkIfFileTypeSupported(contentType)) {
                        throw new UnsupportedFileTypeException(
                            String.format("File type %s is unsupported", contentType));
                    }

                    if (inlineAttachment.isBase64()) {
                        decodedPayload = Base64.getMimeDecoder().decode(inlineAttachment.getPayload());
                    } else {
                        decodedPayload = inlineAttachment.getPayload().getBytes(StandardCharsets.UTF_8);
                    }

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
                    storageManagerService.uploadFile(filename, dataWrapper, conversationId);

                } catch (StorageException ex) {
                    throw new InlineAttachmentProcessingException("Unable to upload inline attachment to storage: " + ex.getMessage(), ex);
                } catch (IOException ex) {
                    throw new InlineAttachmentProcessingException("Unable to decompress attachment: " + ex.getMessage(), ex);
                } catch (ParseException ex) {
                    throw new InlineAttachmentProcessingException("Unable to parse inline attachment description: " + ex.getMessage(), ex);
                } catch (UnsupportedFileTypeException ex) {
                    throw ex;
                }
            }
        }
    }

    public void storeAttachmentWithoutProcessing(String fileName, String payload, String conversationId, String contentType)
                                                            throws ValidationException, InlineAttachmentProcessingException {

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
            storageManagerService.uploadFile(fileName, dataWrapper, conversationId);
        } catch (StorageException ex) {
            throw new InlineAttachmentProcessingException("Unable to upload inline attachment to storage without processing: "
                + ex.getMessage(), ex);
        }
    }

    public byte[] getAttachment(String filename, String conversationId) {
        if (!StringUtils.hasText(filename)) {
            throw new IllegalArgumentException("filename must not be empty");
        }
        return storageManagerService.downloadFile(filename, conversationId);
    }

    public void removeAttachment(String filename, String conversationId) {
        if (!StringUtils.hasText(filename)) {
            throw new IllegalArgumentException("filename must not be empty");
        }
        storageManagerService.deleteFile(filename, conversationId);
    }

    public String buildSingleFileStringFromPatientAttachmentLogs(List<PatientAttachmentLog> attachmentLogs, String conversationId) {
        StringBuilder combinedFile = new StringBuilder("");
        for (PatientAttachmentLog log : attachmentLogs) {
            var filename = log.getFilename();
            var attachmentBytes = getAttachment(filename, conversationId);
            combinedFile.append(new String(attachmentBytes, StandardCharsets.UTF_8));
        }

        return combinedFile.toString();
    }

    public List<InboundMessage.Attachment> buildInboundAttachmentsFromAttachmentLogs(
        List<PatientAttachmentLog> attachmentLogs,
        List<String> payloads,
        String conversationId) {

        List<InboundMessage.Attachment> attachmentsResponse = new ArrayList<>();

        for (var  i = 0; i < attachmentLogs.size(); i++) {
            var log = attachmentLogs.get(i);

            var fileDescription = log.getFileDescription();

            var payload = "";
            if (payloads == null || payloads.get(i) == null) {
                payload = Arrays.toString(getAttachment(log.getFilename(), conversationId));
            } else {
                payload = payloads.get(i);
            }

            attachmentsResponse.add(
                InboundMessage.Attachment.builder()
                    .payload(payload)
                    .isBase64(log.getIsBase64().toString())
                    .contentType(log.getContentType())
                    .description(fileDescription)
                    .build()
            );
        }

        return attachmentsResponse;
    }

    private boolean checkIfFileTypeSupported(String fileType) {
        return supportedFileTypes.getAccepted() != null
            && supportedFileTypes.getAccepted().contains(fileType);
    }
}
