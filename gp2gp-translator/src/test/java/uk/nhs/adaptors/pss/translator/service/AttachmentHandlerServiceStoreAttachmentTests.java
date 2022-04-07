package uk.nhs.adaptors.pss.translator.service;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.bind.ValidationException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.storage.StorageDataUploadWrapper;
import uk.nhs.adaptors.pss.translator.storage.StorageManagerService;

@ExtendWith(MockitoExtension.class)
public class AttachmentHandlerServiceStoreAttachmentTests {
    private static List<InboundMessage.Attachment> mockAttachments;
    private static List<InboundMessage.Attachment> mockCompressedAttachments;
    private static List<InboundMessage.Attachment> mockMislabeledUncompressedAttachments;
    private static List<InboundMessage.Attachment> mockMissingDescriptionElementsAttachments;
    private static final String conversationId = "1";

    @Captor
    private ArgumentCaptor<StorageDataUploadWrapper> dataWrapperCaptor;

    @Captor
    private ArgumentCaptor<String> filenameCaptor;

    @Mock
    private StorageManagerService storageManagerService;

    @InjectMocks
    private AttachmentHandlerService attachmentHandlerService;

    @BeforeAll
    static void setMockedCompressedAttachments() throws IOException {
        mockCompressedAttachments = List.of(
            InboundMessage.Attachment.builder()
                .contentType("txt")
                .isBase64("true")
                .description("Filename=\"text_attachment_encoded_and_compressed.txt\" ContentType=text/plain Compressed=Yes "
                    + "LargeAttachment=No OriginalBase64=No")
                .payload(readFileAsString("InlineAttachments/text_attachment_encoded_and_compressed.txt"))
                .build(),
            InboundMessage.Attachment.builder()
                .contentType("application/pdf")
                .isBase64("true")
                .description("Filename=\"large_messages.pdf\" ContentType=application/pdf Compressed=Yes "
                    + "LargeAttachment=No OriginalBase64=No")
                .payload(readFileAsString("InlineAttachments/large_messages.pdf.txt"))
                .build()
        );
    }

    private static String readFileAsString(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return Files.readString(Paths.get(resource.getURI()));
    }

    private static byte[] readFileAsBytes(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return Files.readAllBytes(Paths.get(resource.getURI()));
    }

    @BeforeAll
    static void setMockedAttachments() throws IOException {
        mockAttachments = List.of(
            InboundMessage.Attachment.builder()
                .contentType("txt")
                .isBase64("true")
                .description("Filename=\"277F29F1-FEAB-4D38-8266-FEB7A1E6227D_LICENSE.txt\" ContentType=text/plain Compressed=No "
                    + "LargeAttachment=No OriginalBase64=Yes")
                .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=").build(),
            InboundMessage.Attachment.builder()
                .contentType("txt")
                .isBase64("true")
                .description("Filename=\"text_attachment_encoded.txt\" ContentType=text/plain Compressed=No "
                    + "LargeAttachment=No OriginalBase64=No")
                .payload(readFileAsString("InlineAttachments/text_attachment_encoded.txt"))
                .build()
        );
    }

    @BeforeAll
    static void setMockMislabeledUncompressedAttachments() throws IOException {
        mockMislabeledUncompressedAttachments = List.of(
            InboundMessage.Attachment.builder()
                .contentType("text/plain")
                .isBase64("true")
                .description("Filename=\"text_attachment_encoded.txt\" ContentType=text/plain Compressed=Yes "
                    + "LargeAttachment=No OriginalBase64=No")
                .payload(readFileAsString("InlineAttachments/text_attachment_encoded.txt"))
                .build()
        );
    }

    @BeforeAll
    static void setMockMissingDescriptionElementsAttachments() throws IOException {
        mockMissingDescriptionElementsAttachments = List.of(
            InboundMessage.Attachment.builder()
                .contentType("text/plain")
                .isBase64("true")
                .description("Incorrect format test")
                .payload(readFileAsString("InlineAttachments/text_attachment_encoded.txt"))
                .build()
        );
    }

    @Test
    public void When_ValidListOfAttachmentsAndConversationIdIsGiven_Expect_DoesNotThrow() throws ValidationException,
        InlineAttachmentProcessingException {

        attachmentHandlerService.storeAttachments(mockAttachments, conversationId);
        // No assertion required for a DoesNotThrow Test
    }

    @Test
    public void When_ValidListOfAttachmentsAndConversationIdIsNull_Expect_ValidationException() {

        Exception exception = assertThrows(ValidationException.class, () -> {
            attachmentHandlerService.storeAttachments(mockAttachments, null);
        });

        String expectedMessage = "ConversationId cannot be null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void When_ValidListOfAttachmentsAndConversationIdIsEmpty_Expect_ValidationException() {

        Exception exception = assertThrows(ValidationException.class, () -> {
            attachmentHandlerService.storeAttachments(mockAttachments, "");
        });

        String expectedMessage = "ConversationId cannot be null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void When_DecompressionFails_Expect_InlineProcessingException() {

        Exception exception = assertThrows(InlineAttachmentProcessingException.class, () -> {
            attachmentHandlerService.storeAttachments(mockMislabeledUncompressedAttachments, conversationId);
        });

        String expectedMessage = "Unable to decompress attachment:";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void When_AttachmentDescriptionNotParsedCorrectly_Expect_InlineProcessingException() {

        Exception exception = assertThrows(InlineAttachmentProcessingException.class, () -> {
            attachmentHandlerService.storeAttachments(mockMissingDescriptionElementsAttachments, conversationId);
        });

        String expectedMessage = "Unable to parse inline attachment description:";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void When_ValidListOfAttachmentsAndConversationId_Expect_CallsStorageManagerUploadFile() throws ValidationException,
        InlineAttachmentProcessingException {

        attachmentHandlerService.storeAttachments(mockAttachments, conversationId);
        verify(storageManagerService, atLeast(1)).uploadFile(any(), any());
    }

    @Test
    public void When_CompressedListOfAttachmentsAndConversationId_Expect_PayloadIsDecodedAndDecompressed() throws ValidationException,
        IOException, InlineAttachmentProcessingException {

        attachmentHandlerService.storeAttachments(mockCompressedAttachments, conversationId);

        verify(storageManagerService, atLeast(1)).uploadFile(any(), dataWrapperCaptor.capture());

        List<String> dataStringList = dataWrapperCaptor.getAllValues().stream().map(dw -> new String(dw.getData(), UTF_8)).toList();

        assertEquals(readFileAsString("InlineAttachments/text_attachment.txt"), dataStringList.get(0));
    }

    @Test
    public void When_UncompressedListOfAttachmentsAndConversationId_Expect_PayloadIsDecoded() throws ValidationException, IOException,
        InlineAttachmentProcessingException {

        attachmentHandlerService.storeAttachments(mockAttachments, conversationId);

        verify(storageManagerService, atLeast(1)).uploadFile(any(), dataWrapperCaptor.capture());

        List<String> dataStringList = dataWrapperCaptor.getAllValues().stream().map(dw -> new String(dw.getData(), UTF_8)).toList();

        assertEquals("Hello World from Scott Alexander", dataStringList.get(0));
        assertEquals(readFileAsString("InlineAttachments/text_attachment.txt"), dataStringList.get(1));
    }

    @Test
    public void When_ValidListOfAttachmentsAndConversationId_Expect_FilenameIsCorrect() throws ValidationException,
        InlineAttachmentProcessingException {

        attachmentHandlerService.storeAttachments(mockAttachments, conversationId);

        verify(storageManagerService, atLeast(1)).uploadFile(filenameCaptor.capture(), any());

        List<String> captorValues = filenameCaptor.getAllValues();

        assertEquals("277F29F1-FEAB-4D38-8266-FEB7A1E6227D_LICENSE.txt", captorValues.get(0));
        assertEquals("text_attachment_encoded.txt", captorValues.get(1));
    }

    @Test
    public void When_CompressedListOfAttachmentContainsPdf_Expect_DecodedAndDecompressed() throws ValidationException, IOException,
        InlineAttachmentProcessingException {
        attachmentHandlerService.storeAttachments(mockCompressedAttachments, conversationId);

        verify(storageManagerService, atLeast(1)).uploadFile(any(), dataWrapperCaptor.capture());

        List<StorageDataUploadWrapper> dataStringList = dataWrapperCaptor.getAllValues();

        byte[] dataByteArray = dataStringList.get(1).getData();

        assertArrayEquals(readFileAsBytes("InlineAttachments/large_messages.pdf"), dataByteArray);
    }
}
