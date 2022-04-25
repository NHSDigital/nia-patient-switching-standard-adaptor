package uk.nhs.adaptors.pss.translator.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.nhs.adaptors.pss.translator.exception.AttachmentNotFoundException;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.storage.StorageManagerService;

import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;

@ExtendWith(MockitoExtension.class)
public class AttachmentReferenceUpdaterServiceTests {

    private static final String XML_RESOURCES_BASE = "xml/RCMRIN030000UK06_LARGE_MSG/";
    private static final String PAYLOAD_XML = "payload.xml";
    private static final String CONVERSATION_ID = "1";
    @Mock
    private StorageManagerService storageManagerService;

    @InjectMocks
    private AttachmentReferenceUpdaterService attachmentReferenceUpdaterService;

    private static List<InboundMessage.Attachment> mockAttachment;
    private static List<InboundMessage.Attachment> mockThreeAttachments;

    private static String readFileAsString(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return Files.readString(Paths.get(resource.getURI()));
    }
    
    @BeforeAll
    static void setMockedAttachments() throws IOException {
        mockAttachment = List.of(
                InboundMessage.Attachment.builder()
                        .contentType("txt")
                        .isBase64("true")
                        .description("Filename=\"277F29F1-FEAB-4D38-8266-FEB7A1E6227D_LICENSE.txt\" ContentType=text/plain Compressed=No "
                                + "LargeAttachment=No OriginalBase64=Yes")
                        .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=").build()
        );

        mockThreeAttachments = List.of(
                InboundMessage.Attachment.builder()
                        .contentType("txt")
                        .isBase64("true")
                        .description("Filename=\"277F29F1-FEAB-4D38-8266-FEB7A1E6227D_LICENSE.txt\" ContentType=text/plain Compressed=No "
                                + "LargeAttachment=No OriginalBase64=Yes")
                        .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=").build(),
                InboundMessage.Attachment.builder()
                        .contentType("txt")
                        .isBase64("true")
                        .description("Filename=\"7CCB6A77-360E-434E-8CF4-97C7C2B47D70_book.txt\" ContentType=text/plain Compressed=No "
                                + "LargeAttachment=No OriginalBase64=Yes")
                        .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=").build(),
                InboundMessage.Attachment.builder()
                        .contentType("txt")
                        .isBase64("true")
                        .description("Filename=\"8681AF4F-E577-4C8D-A2CE-43CABE3D5FB4_sample_mpeg4.mp4\" ContentType=video/mpeg Compressed=No "
                                + "LargeAttachment=No OriginalBase64=Yes")
                        .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=").build()
        );
    }
    
    @Test
    public void When_NoAttachmentsGiven_NothingChanges() throws AttachmentNotFoundException, ValidationException, InlineAttachmentProcessingException {
        //arrange


        var content = getFileContent(PAYLOAD_XML);

        //act
        var result = attachmentReferenceUpdaterService.updateReferenceToAttachment(null, CONVERSATION_ID, content);

        //assert
        assertEquals(content, result);
    }
    
    @Test
    public void When_AttachmentGiven_GetUrlHitOnce() throws AttachmentNotFoundException, ValidationException, InlineAttachmentProcessingException {

        var content = getFileContent(PAYLOAD_XML);

        attachmentReferenceUpdaterService.updateReferenceToAttachment(mockAttachment, CONVERSATION_ID, content);

        verify(storageManagerService, times(1)).getFileLocation(any());
    }

    @Test
    public void When_AttachmentGiven_PayloadXmlChanged() throws AttachmentNotFoundException, ValidationException, InlineAttachmentProcessingException {

        var content = getFileContent(PAYLOAD_XML);
        when(storageManagerService.getFileLocation(any())).thenReturn("https://location.com");

        var result = attachmentReferenceUpdaterService.updateReferenceToAttachment(mockAttachment, CONVERSATION_ID, content);

        assertNotEquals(content, result);
        assertTrue(result.contains("https://location.com"));
        assertFalse(result.contains("file://localhost/277F29F1-FEAB-4D38-8266-FEB7A1E6227D_LICENSE.txt"));
    }

    @Test
    public void When_MultipleAttachmentsGiven_GetFileLocationHitMultipleTimes() throws AttachmentNotFoundException, ValidationException, InlineAttachmentProcessingException {

        var content = getFileContent(PAYLOAD_XML);
        when(storageManagerService.getFileLocation(any())).thenReturn("https://location.com");

        var result = attachmentReferenceUpdaterService.updateReferenceToAttachment(mockThreeAttachments, CONVERSATION_ID, content);

        verify(storageManagerService, times(3)).getFileLocation(any());
        assertTrue(result.contains("https://location.com"));
        assertFalse(result.contains("file://localhost/277F29F1-FEAB-4D38-8266-FEB7A1E6227D_LICENSE.txt"));
        assertFalse(result.contains("file://localhost/7CCB6A77-360E-434E-8CF4-97C7C2B47D70_book.txt"));
        assertFalse(result.contains("file://localhost/8681AF4F-E577-4C8D-A2CE-43CABE3D5FB4_sample_mpeg4.mp4"));
    }

    private String getFileContent(String filename) {

        return readResourceAsString("/" + XML_RESOURCES_BASE + filename);
    }
}
