package uk.nhs.adaptors.pss.translator.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;

import java.io.StringReader;
import java.util.List;

import jakarta.xml.bind.ValidationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xml.sax.InputSource;

import uk.nhs.adaptors.pss.translator.exception.AttachmentNotFoundException;
import uk.nhs.adaptors.pss.translator.exception.InlineAttachmentProcessingException;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.storage.StorageManagerService;

@ExtendWith(MockitoExtension.class)
public class AttachmentReferenceUpdaterServiceTests {

    private static final String XML_RESOURCES_BASE = "xml/RCMRIN030000UK06_LARGE_MSG/";
    private static final String PAYLOAD_XML = "payload.xml";
    private static final String FLAT_PAYLOAD_XML = "payload_flat.xml";
    private static final String ENCODED_PAYLOAD = "payload_encoded_urls.xml";
    private static final String EXPECTED_OUTPUT_FOR_ENCODED_PAYLOAD = "expected_encoded_urls.xml";
    private static final String CONVERSATION_ID = "1";
    @Mock
    private StorageManagerService storageManagerService;

    @InjectMocks
    private AttachmentReferenceUpdaterService attachmentReferenceUpdaterService;

    private static List<InboundMessage.Attachment> mockAttachment;
    private static List<InboundMessage.Attachment> mockThreeAttachments;

    private static List<InboundMessage.Attachment> mockTppStyleAttachment;

    private static List<InboundMessage.Attachment> mockEncodedAttachments;

    private static List<InboundMessage.Attachment> mockMissingAttachment;

    @BeforeAll
    static void setMockedAttachments() {
        mockAttachment = List.of(
                InboundMessage.Attachment.builder()
                        .contentType("txt")
                        .isBase64("true")
                        .description("Filename=\"277F29F1-FEAB-4D38-8266-FEB7A1E6227D_LICENSE.txt\" "
                                + "ContentType=text/plain Compressed=No "
                                + "LargeAttachment=No OriginalBase64=Yes")
                        .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=").build()
        );

        mockThreeAttachments = List.of(
                InboundMessage.Attachment.builder()
                        .contentType("txt")
                        .isBase64("true")
                        .description("Filename=\"277F29F1-FEAB-4D38-8266-FEB7A1E6227D_LICENSE.txt\" "
                                + "ContentType=text/plain Compressed=No "
                                + "LargeAttachment=No OriginalBase64=Yes")
                        .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=").build(),
                InboundMessage.Attachment.builder()
                        .contentType("txt")
                        .isBase64("true")
                        .description("Filename=\"7CCB6A77-360E-434E-8CF4-97C7C2B47D70_book.txt\" "
                                + "ContentType=text/plain Compressed=No "
                                + "LargeAttachment=No OriginalBase64=Yes")
                        .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=").build(),
                InboundMessage.Attachment.builder()
                        .contentType("txt")
                        .isBase64("true")
                        .description(
                                "Filename=\"8681AF4F-E577-4C8D-A2CE-43CABE3D5FB4_sample_mpeg4.mp4\" "
                                + "ContentType=video/mpeg Compressed=No "
                                + "LargeAttachment=No OriginalBase64=Yes")
                        .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=").build()
        );

        mockTppStyleAttachment = List.of(
                InboundMessage.Attachment.builder()
                        .contentType("txt")
                        .isBase64("true")
                        .description("8681AF4F-E577-4C8D-A2CE-43CABE3D5FB4_sample_mpeg4.mp4")
                        .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=")
                        .build()
        );

        mockEncodedAttachments = List.of(
                InboundMessage.Attachment.builder()
                        .contentType("txt")
                        .isBase64("true")
                        .description("Filename=\"277F29F1-FEAB-4D38-8266-FEB7A1E6227D_LICENSE &59.txt\" "
                            + "ContentType=text/plain Compressed=No "
                            + "LargeAttachment=No OriginalBase64=Yes")
                        .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=").build(),
                InboundMessage.Attachment.builder()
                        .contentType("txt")
                        .isBase64("true")
                        .description("7CCB6A77-360E-434E-8CF4-97C7C2B47D70_Hello Günter.txt")
                        .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=").build(),
                InboundMessage.Attachment.builder()
                        .contentType("txt")
                        .isBase64("true")
                        .description("8681AF4F-E577-4C8D-A2CE-43CABE3D5FB4_sample_mpeg4.mp4")
                        .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=").build()
        );

        mockMissingAttachment = List.of(
                InboundMessage.Attachment.builder()
                        .contentType("txt")
                        .isBase64("true")
                        .description("missing_attachment.txt")
                        .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=").build()
        );
    }

    @Test
    public void When_NoAttachmentsGiven_Expect_NoChanges()
            throws AttachmentNotFoundException, ValidationException, InlineAttachmentProcessingException {

        var content = getFileContent(PAYLOAD_XML);

        var result = attachmentReferenceUpdaterService.updateReferenceToAttachment(null, CONVERSATION_ID, content);

        assertEquals(content, result);
    }

    @Test
    public void When_AttachmentGiven_Expect_GetFileLocationHitOnce()
            throws AttachmentNotFoundException, ValidationException, InlineAttachmentProcessingException {

        var content = getFileContent(PAYLOAD_XML);

        when(storageManagerService.getFileLocation(any(), any())).thenReturn("https://location.com");

        attachmentReferenceUpdaterService.updateReferenceToAttachment(mockAttachment, CONVERSATION_ID, content);

        verify(storageManagerService, times(1)).getFileLocation(any(), any());
    }

    @Test
    public void When_AttachmentGiven_Expect_PayloadXmlChanged()
            throws AttachmentNotFoundException, ValidationException, InlineAttachmentProcessingException {

        var content = getFileContent(PAYLOAD_XML);
        when(storageManagerService.getFileLocation(any(), any())).thenReturn("https://location.com");

        var result = attachmentReferenceUpdaterService.updateReferenceToAttachment(mockAttachment, CONVERSATION_ID, content);

        assertNotEquals(content, result);
        assertTrue(result.contains("https://location.com"));
        assertFalse(result.contains("file://localhost/277F29F1-FEAB-4D38-8266-FEB7A1E6227D_LICENSE.txt"));
    }

    @Test
    public void When_AttachmentGiven_Expect_ValidXml()
            throws AttachmentNotFoundException, ValidationException,
                InlineAttachmentProcessingException, ParserConfigurationException {

        var content = getFileContent(FLAT_PAYLOAD_XML);
        when(storageManagerService.getFileLocation(any(), any())).thenReturn("https://location.com");

        var result = attachmentReferenceUpdaterService.updateReferenceToAttachment(mockAttachment, CONVERSATION_ID, content);

        assertNotEquals(content, result);
        assertTrue(result.contains("https://location.com"));

        // the following asserts that the result payload is acceptable XML - i.e. nothing funky happened with RegEx
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        var documentBuilder = documentBuilderFactory.newDocumentBuilder();
        var inputSource = new InputSource(new StringReader(result));
        assertDoesNotThrow(() -> documentBuilder.parse(inputSource));
    }

    @Test
    public void When_MultipleAttachmentsGiven_Expect_GetFileLocationHitMultipleTimes()
            throws AttachmentNotFoundException, ValidationException, InlineAttachmentProcessingException {

        var content = getFileContent(PAYLOAD_XML);
        when(storageManagerService.getFileLocation(any(), any())).thenReturn("https://location.com");

        var result = attachmentReferenceUpdaterService.updateReferenceToAttachment(mockThreeAttachments, CONVERSATION_ID, content);

        verify(storageManagerService, times(mockThreeAttachments.size())).getFileLocation(any(), any());
        assertTrue(result.contains("https://location.com"));
        assertFalse(result.contains("file://localhost/277F29F1-FEAB-4D38-8266-FEB7A1E6227D_LICENSE.txt"));
        assertFalse(result.contains("file://localhost/7CCB6A77-360E-434E-8CF4-97C7C2B47D70_book.txt"));
        assertFalse(result.contains("file://localhost/8681AF4F-E577-4C8D-A2CE-43CABE3D5FB4_sample_mpeg4.mp4"));
    }

    @Test
    public void When_TppStyleAttachmentGiven_Expect_PayloadXmlChanged()
        throws AttachmentNotFoundException, ValidationException, InlineAttachmentProcessingException {

        var content = getFileContent(FLAT_PAYLOAD_XML);
        when(storageManagerService.getFileLocation(any(), any())).thenReturn("https://location.com");

        var result = attachmentReferenceUpdaterService.updateReferenceToAttachment(mockTppStyleAttachment, CONVERSATION_ID, content);

        verify(storageManagerService, times(mockTppStyleAttachment.size())).getFileLocation(any(), any());
        assertTrue(result.contains("https://location.com"));
        assertFalse(result.contains("file://localhost/8681AF4F-E577-4C8D-A2CE-43CABE3D5FB4_sample_mpeg4.mp4"));
    }

    @Test
    public void When_EncodedUrlsPresent_Expect_PayloadXmlChanged()
        throws AttachmentNotFoundException, ValidationException, InlineAttachmentProcessingException {

        var content = getFileContent(ENCODED_PAYLOAD);
        var expectedOutput = getFileContent(EXPECTED_OUTPUT_FOR_ENCODED_PAYLOAD);

        when(storageManagerService.getFileLocation(eq("277F29F1-FEAB-4D38-8266-FEB7A1E6227D_LICENSE &59.txt"), any()))
            .thenReturn("https://location.com/LICENCE.txt");
        when(storageManagerService.getFileLocation(eq("7CCB6A77-360E-434E-8CF4-97C7C2B47D70_Hello Günter.txt"), any()))
            .thenReturn("https://location.com/helloGunter.txt");
        when(storageManagerService.getFileLocation(eq("8681AF4F-E577-4C8D-A2CE-43CABE3D5FB4_sample_mpeg4.mp4"), any()))
            .thenReturn("https://location.com/sampleMpeg4.mp4");

        var result = attachmentReferenceUpdaterService.updateReferenceToAttachment(mockEncodedAttachments, CONVERSATION_ID, content);

        verify(storageManagerService, times(mockEncodedAttachments.size())).getFileLocation(any(), any());

        assertThat(result).isEqualToIgnoringWhitespace(expectedOutput);
    }

    @Test
    public void When_MissingAttachmentGiven_Expect_AttachmentNotFoundException() {
        var content = getFileContent(PAYLOAD_XML);

        assertThatThrownBy(
            () -> attachmentReferenceUpdaterService.updateReferenceToAttachment(mockMissingAttachment, CONVERSATION_ID, content)
        )
            .isInstanceOf(AttachmentNotFoundException.class)
            .hasMessageContaining("Unable to find attachment(s): [missing_attachment.txt]");
    }

    private String getFileContent(String filename) {

        return readResourceAsString("/" + XML_RESOURCES_BASE + filename);
    }
}
