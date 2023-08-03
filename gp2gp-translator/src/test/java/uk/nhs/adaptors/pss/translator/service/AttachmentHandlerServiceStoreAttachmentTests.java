package uk.nhs.adaptors.pss.translator.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import uk.nhs.adaptors.pss.translator.config.SupportedFileTypes;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.exception.UnsupportedFileTypeException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.storage.StorageDataUploadWrapper;
import uk.nhs.adaptors.pss.translator.storage.StorageException;
import uk.nhs.adaptors.pss.translator.storage.StorageManagerService;

import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AttachmentHandlerServiceStoreAttachmentTests {
    private static List<InboundMessage.Attachment> mockAttachments;
    private static List<InboundMessage.Attachment> mockCompressedAttachments;
    private static List<InboundMessage.Attachment> mockMislabeledUncompressedAttachments;
    private static List<InboundMessage.Attachment> mockMissingDescriptionElementsAttachments;
    private static final String CONVERSATION_ID = "1";

    @Captor
    private ArgumentCaptor<StorageDataUploadWrapper> dataWrapperCaptor;

    @Captor
    private ArgumentCaptor<String> filenameCaptor;

    @Mock
    private StorageManagerService storageManagerService;

    @InjectMocks
    private AttachmentHandlerService attachmentHandlerService;

    @Mock
    private SupportedFileTypes supportedFileTypesMock;

    @BeforeAll
    static void setMockedCompressedAttachments() throws IOException {
        mockCompressedAttachments = List.of(
            InboundMessage.Attachment.builder()
                .contentType("text/plain")
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
                .contentType("text/plain")
                .isBase64("true")
                .description("Filename=\"277F29F1-FEAB-4D38-8266-FEB7A1E6227D_LICENSE.txt\" ContentType=text/plain Compressed=No "
                    + "LargeAttachment=No OriginalBase64=Yes")
                .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=").build(),
            InboundMessage.Attachment.builder()
                .contentType("text/plain")
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
        InlineAttachmentProcessingException, UnsupportedFileTypeException {

        when(supportedFileTypesMock.getAccepted()).thenReturn(new HashSet<>(Arrays.asList("text/plain")));
        attachmentHandlerService.storeAttachments(mockAttachments, CONVERSATION_ID);
        verify(storageManagerService, times(2)).uploadFile(any(), any(), any());
    }

    @Test
    public void When_ValidListOfAttachmentsAndConversationIdIsNull_Expect_ValidationException() {

        Exception exception = assertThrows(ValidationException.class, () ->
            attachmentHandlerService.storeAttachments(mockAttachments, null)
        );

        String expectedMessage = "ConversationId cannot be null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void When_ValidListOfAttachmentsAndConversationIdIsEmpty_Expect_ValidationException() {

        Exception exception = assertThrows(ValidationException.class, () ->
            attachmentHandlerService.storeAttachments(mockAttachments, "")
        );

        String expectedMessage = "ConversationId cannot be null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void When_DecompressionFails_Expect_InlineProcessingException() {

        when(supportedFileTypesMock.getAccepted()).thenReturn(new HashSet<>(Arrays.asList("text/plain")));
        Exception exception = assertThrows(InlineAttachmentProcessingException.class, () ->
            attachmentHandlerService.storeAttachments(mockMislabeledUncompressedAttachments, CONVERSATION_ID)
        );

        String expectedMessage = "Unable to decompress attachment:";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void When_AttachmentDescriptionNotParsedCorrectly_Expect_InlineProcessingException() {

        Exception exception = assertThrows(InlineAttachmentProcessingException.class, () ->
            attachmentHandlerService.storeAttachments(mockMissingDescriptionElementsAttachments, CONVERSATION_ID)
        );

        String expectedMessage = "Unable to parse inline attachment description:";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void When_ValidListOfAttachmentsAndConversationId_Expect_CallsStorageManagerUploadFile() throws ValidationException,
        InlineAttachmentProcessingException, UnsupportedFileTypeException {

        when(supportedFileTypesMock.getAccepted()).thenReturn(new HashSet<>(Arrays.asList("text/plain")));
        attachmentHandlerService.storeAttachments(mockAttachments, CONVERSATION_ID);
        verify(storageManagerService, atLeast(1)).uploadFile(any(), any(), any());
    }

    @Test
    public void When_CompressedListOfAttachmentsAndConversationId_Expect_PayloadIsDecodedAndDecompressed() throws ValidationException,
        IOException, InlineAttachmentProcessingException, UnsupportedFileTypeException {

        when(supportedFileTypesMock.getAccepted()).thenReturn(new HashSet<>(Arrays.asList("text/plain", "application/pdf")));
        attachmentHandlerService.storeAttachments(mockCompressedAttachments, CONVERSATION_ID);

        verify(storageManagerService, atLeast(1)).uploadFile(any(), dataWrapperCaptor.capture(), any());

        List<String> dataStringList = dataWrapperCaptor.getAllValues().stream().map(dw -> new String(dw.getData(), UTF_8)).toList();

        assertEquals(readFileAsString("InlineAttachments/text_attachment.txt"), dataStringList.get(0));
    }

    @Test
    public void When_UncompressedListOfAttachmentsAndConversationId_Expect_PayloadIsDecoded() throws ValidationException, IOException,
        InlineAttachmentProcessingException, UnsupportedFileTypeException {

        when(supportedFileTypesMock.getAccepted()).thenReturn(new HashSet<>(Arrays.asList("text/plain")));
        attachmentHandlerService.storeAttachments(mockAttachments, CONVERSATION_ID);

        verify(storageManagerService, atLeast(1)).uploadFile(any(), dataWrapperCaptor.capture(), any());

        List<String> dataStringList = dataWrapperCaptor.getAllValues().stream().map(dw -> new String(dw.getData(), UTF_8)).toList();

        assertEquals("Hello World from Scott Alexander", dataStringList.get(0));
        assertEquals(readFileAsString("InlineAttachments/text_attachment.txt"), dataStringList.get(1));
    }

    @Test
    public void When_ValidListOfAttachmentsAndConversationId_Expect_FilenameIsCorrect() throws ValidationException,
        InlineAttachmentProcessingException, UnsupportedFileTypeException {

        when(supportedFileTypesMock.getAccepted()).thenReturn(new HashSet<>(Arrays.asList("text/plain")));
        attachmentHandlerService.storeAttachments(mockAttachments, CONVERSATION_ID);

        verify(storageManagerService, atLeast(1)).uploadFile(filenameCaptor.capture(), any(), any());

        List<String> captorValues = filenameCaptor.getAllValues();

        assertEquals("277F29F1-FEAB-4D38-8266-FEB7A1E6227D_LICENSE.txt", captorValues.get(0));
        assertEquals("text_attachment_encoded.txt", captorValues.get(1));
    }

    @Test
    public void When_CompressedListOfAttachmentContainsPdf_Expect_DecodedAndDecompressed() throws ValidationException, IOException,
        InlineAttachmentProcessingException, UnsupportedFileTypeException {

        when(supportedFileTypesMock.getAccepted()).thenReturn(new HashSet<>(Arrays.asList("text/plain", "application/pdf")));
        attachmentHandlerService.storeAttachments(mockCompressedAttachments, CONVERSATION_ID);

        verify(storageManagerService, atLeast(1)).uploadFile(any(), dataWrapperCaptor.capture(), any());

        List<StorageDataUploadWrapper> dataStringList = dataWrapperCaptor.getAllValues();

        byte[] dataByteArray = dataStringList.get(1).getData();

        assertArrayEquals(readFileAsBytes("InlineAttachments/large_messages.pdf"), dataByteArray);
    }

    @Test
    public void When_StoreAttachmentsWithoutProcessingFailsToUpload_Expect_ThrowInlineAttachmentProcessingException() {

        when(supportedFileTypesMock.getAccepted()).thenReturn(new HashSet<>(Arrays.asList("text/plain")));
        doThrow(StorageException.class)
                .when(storageManagerService)
                .uploadFile(any(), any(), any());


        assertThrows(InlineAttachmentProcessingException.class, () ->
            attachmentHandlerService.storeAttachments(mockAttachments, CONVERSATION_ID)
        );
    }

    @Test
    public void When_StoreEhrExtractParametersAreCorrectAndNotErrors_Expect_ExecuteStorageManagerServiceUploadFile()
            throws ValidationException, InlineAttachmentProcessingException {
        attachmentHandlerService.storeAttachmentWithoutProcessing("fileName", "payload", "conversationId", "contentType", 0, false);
        verify(storageManagerService).uploadFile(any(), any(), any());
    }

    @Test
    public void When_StoreAttachmentsFailsToUpload_Expect_ThrowInlineAttachmentProcessingException() {
        doThrow(StorageException.class)
                .when(storageManagerService)
                .uploadFile(any(), any(), any());

        assertThrows(InlineAttachmentProcessingException.class, () ->
            attachmentHandlerService.storeAttachmentWithoutProcessing("fileNAme", "Payload", "123456",
                "contentType", 0, false)
        );
    }

    @Test
    public void When_StoreEhrExtractParameterFileNameIsNullOrEmpty_Expect_ThrowsValidationException() {

        Exception exceptionNull = assertThrows(
                ValidationException.class,
                () ->
                    attachmentHandlerService.storeAttachmentWithoutProcessing(null, "payload",
                        "conversationId", "contentType", 0, false)

        );
        String actualNull = exceptionNull.getMessage();
        String expectedNull = "FileName cannot be null or empty";
        assertEquals(expectedNull, actualNull);

        Exception exceptionEmpty = assertThrows(
                ValidationException.class,
                () ->
                    attachmentHandlerService.storeAttachmentWithoutProcessing("", "payload",
                        "conversationId", "contentType", 0, false)

        );
        String actualEmpty = exceptionEmpty.getMessage();
        String expectedEmpty = "FileName cannot be null or empty";
        assertEquals(expectedEmpty, actualEmpty);
    }

    @Test
    public void When_StoreEhrExtractParameterPayloadIsNullOrEmpty_Expect_ThrowsValidationException() {
        Exception exceptionNull = assertThrows(
                ValidationException.class,
                () ->
                    attachmentHandlerService.storeAttachmentWithoutProcessing("Filename", null,
                        "conversationId", "contentType", 0, false)

        );
        String actualNull = exceptionNull.getMessage();
        String expectedNull = "Payload cannot be null or empty";
        assertEquals(expectedNull, actualNull);

        Exception exceptionEmpty = assertThrows(
                ValidationException.class,
                () ->
                    attachmentHandlerService.storeAttachmentWithoutProcessing("Filename", "",
                        "conversationId", "contentType", 0, false)

        );
        String actualEmpty = exceptionEmpty.getMessage();
        String expectedEmpty = "Payload cannot be null or empty";
        assertEquals(expectedEmpty, actualEmpty);
    }

    @Test
    public void When_StoreEhrExtractParameterConversationIdIsNullOrEmpty_Expect_ThrowsValidationException() {
        Exception exceptionNull = assertThrows(
                ValidationException.class,
                () ->
                    attachmentHandlerService.storeAttachmentWithoutProcessing("Filename", "payload",
                        null, "contentType", 0, false)

        );
        String actualNull = exceptionNull.getMessage();
        String expectedNull = "ConversationId cannot be null or empty";
        assertEquals(expectedNull, actualNull);

        Exception exceptionEmpty = assertThrows(
                ValidationException.class,
                () ->
                    attachmentHandlerService.storeAttachmentWithoutProcessing("Filename", "payload",
                        "", "contentType", 0, false)

        );
        String actualEmpty = exceptionEmpty.getMessage();
        String expectedEmpty = "ConversationId cannot be null or empty";
        assertEquals(expectedEmpty, actualEmpty);
    }

    @Test
    public void When_StoreEhrExtractParameterContentTypeIsNullOrEmpty_Expect_ThrowsValidationException() {
        Exception exceptionNull = assertThrows(
                ValidationException.class,
                () -> attachmentHandlerService.storeAttachmentWithoutProcessing("Filename", "payload",
                    "conversationId", null, 0, false)
        );
        String actualNull = exceptionNull.getMessage();
        String expectedNull = "ContentType cannot be null or empty";
        assertEquals(expectedNull, actualNull);

        Exception exceptionEmpty = assertThrows(
                ValidationException.class,
                () -> attachmentHandlerService.storeAttachmentWithoutProcessing("Filename", "payload",
                    "conversationId", "", 0, false)

        );
        String actualEmpty = exceptionEmpty.getMessage();
        String expectedEmpty = "ContentType cannot be null or empty";
        assertEquals(expectedEmpty, actualEmpty);
    }

    @Test
    public void When_AttachmentMismatchedPayloadLengthIsGiven_Expect_NotThrowsInlineAttachmentException() {

        when(supportedFileTypesMock.getAccepted()).thenReturn(new HashSet<>(Arrays.asList("text/plain")));
        var attachment = List.of(InboundMessage.Attachment.builder()
            .contentType("text/plain")
            .isBase64("true") // file has to be base64 otherwise length can not be checked
            .description("Filename=\"277F29F1-FEAB-4D38-8266-FEB7A1E6227D_LICENSE.txt\" ContentType=text/plain Compressed=No "
                + "LargeAttachment=No OriginalBase64=No; Length=45")
            .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=").build());

        assertDoesNotThrow( () -> attachmentHandlerService.storeAttachments(attachment, CONVERSATION_ID),
                            "PS Adaptor is OK when payload size is different from the declared one");

    }

    @Test
    public void When_AttachmentCorrectPayloadLengthIsGiven_Expect_DoesNotThrow() throws ValidationException,
        InlineAttachmentProcessingException, UnsupportedFileTypeException {

        when(supportedFileTypesMock.getAccepted()).thenReturn(new HashSet<>(Arrays.asList("text/plain")));
        var attachment = List.of(InboundMessage.Attachment.builder()
            .contentType("text/plain")
            .isBase64("false")
            .description("Filename=\"277F29F1-FEAB-4D38-8266-FEB7A1E6227D_LICENSE.txt\" ContentType=text/plain Compressed=No "
                + "LargeAttachment=No OriginalBase64=No; Length=44")
            .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=").build());

        when(supportedFileTypesMock.getAccepted()).thenReturn(new HashSet<>(Arrays.asList("text/plain")));
        attachmentHandlerService.storeAttachments(attachment, CONVERSATION_ID);
    }

    @Test
    public void When_AttachmentHasFileExtensionThatIsNotApproved_Expect_ThrowsUnsupportedFileTypeException() {

        when(supportedFileTypesMock.getAccepted()).thenReturn(new HashSet<>(Arrays.asList("text/notsupported")));
        var attachment = List.of(InboundMessage.Attachment.builder()
            .isBase64("false")
            .contentType("text/plain")
            .description("Filename=\"277F29F1-FEAB-4D38-8266-FEB7A1E6227D_LICENSE.abc\" ContentType=text/plain Compressed=No "
                + "LargeAttachment=No OriginalBase64=No; Length=45")
            .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=").build());

        Exception exception = assertThrows(UnsupportedFileTypeException.class, () ->
            attachmentHandlerService.storeAttachments(attachment, CONVERSATION_ID)
        );

        String expectedMessage = "File type text/plain is unsupported";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }


}
