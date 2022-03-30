package uk.nhs.adaptors.pss.translator.service;

import lombok.Setter;
import lombok.SneakyThrows;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.v3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.pss.translator.generator.BundleGenerator;
import uk.nhs.adaptors.pss.translator.mapper.*;
import uk.nhs.adaptors.pss.translator.mapper.medication.MedicationRequestMapper;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage.Attachment;
import uk.nhs.adaptors.pss.translator.storage.StorageDataWrapper;
import uk.nhs.adaptors.pss.translator.storage.StorageException;
import uk.nhs.adaptors.pss.translator.storage.StorageManagerService;

import javax.xml.bind.ValidationException;
import java.util.*;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

@ExtendWith(MockitoExtension.class)
public class AttachmentHandlerService_StoreAttachmentTests {
    private String conversationId = "1";

    public List<InboundMessage.Attachment> mockAttachments =
            Arrays.asList(InboundMessage.Attachment.builder()
                .contentType("txt")
                .isBase64("true")
                .description("example description")
                .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=").build());

    @Mock
    private StorageManagerService storageManagerService;

    @Mock
    StorageDataWrapper testStorageDataWrapper;

    @InjectMocks
    private AttachmentHandlerService attachmentHandlerService;


    @Test
    public void StoreAttachments_WhenValidListOfAttachmentsAndConversationIdIsGiven_DoesNotThrow() throws ValidationException {

        attachmentHandlerService.StoreAttachments(mockAttachments, conversationId);
        // No assertion required for a DoesNotThrow Test
    }

    @Test
    public void StoreAttachments_WhenValidListOfAttachmentsAndConversationIdIsNull_ThrowsValidationException() throws ValidationException {

        Exception exception = assertThrows(ValidationException.class, () -> {
            attachmentHandlerService.StoreAttachments(mockAttachments, null);
        });

        String expectedMessage = "ConversationId cannot be null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void StoreAttachments_WhenValidListOfAttachmentsAndConversationIdIsEmpty_ThrowsValidationException() throws ValidationException {

        Exception exception = assertThrows(ValidationException.class, () -> {
            attachmentHandlerService.StoreAttachments(mockAttachments, "");
        });

        String expectedMessage = "ConversationId cannot be null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void StoreAttachments_WhenValidListOfAttachmentsAndConversationId_CallsStorageManagerUploadFile() throws ValidationException {

        attachmentHandlerService.StoreAttachments(mockAttachments, conversationId);
        verify(storageManagerService).UploadFile(any(),any());
        // at this point in time we don;t mind what the object stores or the filename as that needs to be defined in a later ticket
    }
}
