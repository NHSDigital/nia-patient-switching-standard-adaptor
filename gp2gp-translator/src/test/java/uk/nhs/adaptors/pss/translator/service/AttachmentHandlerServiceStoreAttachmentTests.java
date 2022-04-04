package uk.nhs.adaptors.pss.translator.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.storage.StorageDataUploadWrapper;
import uk.nhs.adaptors.pss.translator.storage.StorageManagerService;

import javax.xml.bind.ValidationException;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class AttachmentHandlerServiceStoreAttachmentTests {
    private String conversationId = "1";

    private List<InboundMessage.Attachment> mockAttachments =
            Arrays.asList(InboundMessage.Attachment.builder()
                .contentType("txt")
                .isBase64("true")
                .description("example description")
                .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=").build());

    @Mock
    private StorageManagerService storageManagerService;

    @Mock
    private StorageDataUploadWrapper testStorageDataWrapper;

    @InjectMocks
    private AttachmentHandlerService attachmentHandlerService;


    @Test
    public void When_ValidListOfAttachmentsAndConversationIdIsGiven_Expect_DoesNotThrow() throws ValidationException {

        attachmentHandlerService.storeAttachments(mockAttachments, conversationId);
        // No assertion required for a DoesNotThrow Test
    }

    @Test
    public void When_ValidListOfAttachmentsAndConversationIdIsNull_Expect_ValidationException() throws ValidationException {

        Exception exception = assertThrows(ValidationException.class, () -> {
            attachmentHandlerService.storeAttachments(mockAttachments, null);
        });

        String expectedMessage = "ConversationId cannot be null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void When_ValidListOfAttachmentsAndConversationIdIsEmpty_Expect_ValidationException() throws ValidationException {

        Exception exception = assertThrows(ValidationException.class, () -> {
            attachmentHandlerService.storeAttachments(mockAttachments, "");
        });

        String expectedMessage = "ConversationId cannot be null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void When_ValidListOfAttachmentsAndConversationId_Expect_CallsStorageManagerUploadFile() throws ValidationException {

        attachmentHandlerService.storeAttachments(mockAttachments, conversationId);
        verify(storageManagerService).uploadFile(any(), any());
        // at this point in time we don;t mind what the object stores or the filename as that needs to be defined in a later ticket
    }
}
