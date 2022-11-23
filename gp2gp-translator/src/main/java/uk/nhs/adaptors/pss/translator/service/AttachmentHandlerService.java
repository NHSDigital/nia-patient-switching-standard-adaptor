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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.ion.NullValueException;
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

    @Value("${base64.skipDecode}")
    private boolean skipDecoding;
    private final StorageManagerService storageManagerService;
    private final SupportedFileTypes supportedFileTypes;

    public void storeAttachments(List<InboundMessage.Attachment> attachments, String conversationId) throws ValidationException,
        InlineAttachmentProcessingException, UnsupportedFileTypeException {

        if (!StringUtils.hasText(conversationId)) {
            throw new ValidationException("ConversationId cannot be null or empty");
        }

        if (attachments != null) {
            for (InboundMessage.Attachment attachment : attachments) {
                try {
                    InlineAttachment inlineAttachment = new InlineAttachment(attachment);

                    String contentType = inlineAttachment.getContentType();
                    if (!checkIfFileTypeSupported(contentType)) {
                        throw new UnsupportedFileTypeException(
                            String.format("File type %s is unsupported", contentType));
                    }

                    if (inlineAttachment != null) {
                        if (inlineAttachment.getLength() > 0
                            && inlineAttachment.getLength() != inlineAttachment.getPayload().length()) {
                            throw new InlineAttachmentProcessingException("Incorrect payload length received");
                        }
                    }
                    byte[] decodedPayload = inlineAttachment.getPayload().getBytes(StandardCharsets.UTF_8);

                    if (!inlineAttachment.isBase64()) {
                        decodedPayload = Base64.getMimeDecoder().decode(inlineAttachment.getPayload());
                    }

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
                    storageManagerService.uploadFile(filename, dataWrapper, conversationId);

                } catch (StorageException ex) {
                    throw new InlineAttachmentProcessingException("Unable to upload inline attachment to storage: " + ex.getMessage());
                } catch (IOException ex) {
                    throw new InlineAttachmentProcessingException("Unable to decompress attachment: " + ex.getMessage());
                } catch (ParseException ex) {
                    throw new InlineAttachmentProcessingException("Unable to parse inline attachment description: " + ex.getMessage());
                } catch (UnsupportedFileTypeException ex) {
                    throw ex;
                }
            }
        }
    }

    public void storeAttachmentWithoutProcessing(String fileName, String payload, String conversationId,
        String contentType, Integer expectedLength)
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

        if (expectedLength != null) {
            if (expectedLength > 0 && expectedLength != payload.length()) {
                throw new InlineAttachmentProcessingException("Incorrect payload length received");
            }
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
                + ex.getMessage());
        }
    }

    public byte[] getAttachment(String filename, String conversationId) {
        if (!StringUtils.hasText(filename)) {
            throw new NullValueException();
        }
        return storageManagerService.downloadFile(filename, conversationId);
    }

    public void removeAttachment(String filename, String conversationId) {
        if (!StringUtils.hasText(filename)) {
            throw new NullValueException();
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

        List<InboundMessage.Attachment> attachmentsResponse = new ArrayList<InboundMessage.Attachment>();

        for (var  i = 0; i < attachmentLogs.size(); i++) {
            var log = attachmentLogs.get(i);

            var fileDescription =
                "Filename=" + "\"" + log.getFilename()  + "\" "
                    + "ContentType=" + log.getContentType() + " "
                    + "Compressed=" + log.getCompressed().toString() + " "
                    + "LargeAttachment=" + log.getLargeAttachment().toString() + " "
                    + "OriginalBase64=" + log.getBase64().toString() + " "
                    + "Length=" + log.getLengthNum();

            if (log.getSkeleton()) {
                fileDescription += " DomainData=\\\"X-GP2GP-Skeleton:Yes\\\"";
            }

            var payload = "";
            if (payloads == null || payloads.get(i) == null) {
                payload = getAttachment(log.getFilename(), conversationId).toString();
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

    private boolean checkIfFileTypeSupported(String fileType) {
        return supportedFileTypes.getAccepted() != null
            && supportedFileTypes.getAccepted().contains(fileType);
    }
}
