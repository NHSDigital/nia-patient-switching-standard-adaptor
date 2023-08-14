package uk.nhs.adaptors.pss.translator.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;

public class InlineAttachmentTest {
    private static final String TEXT_ATTACHMENT_DESCRIPTION = """
        \t\t\t\tFilename="277F29F1-FEAB-4D38-8266-FEB7A1E6227D_LICENSE.txt"\s
        \t\t\t\tContentType=text/plain\s
        \t\t\t\tCompressed=No\s
        \t\t\t\tLargeAttachment=No\s
        \t\t\t\tOriginalBase64=Yes""";
    private static final String TEXT_ATTACHMENT_FILENAME = "277F29F1-FEAB-4D38-8266-FEB7A1E6227D_LICENSE.txt";

    private static InboundMessage.Attachment mockAttachment;
    private static InboundMessage.Attachment mockMissingFilenameAttachment;
    private static InboundMessage.Attachment mockMissingContentTypeAttachment;

    @BeforeAll
    static void setMockAttachment() {
        mockAttachment = InboundMessage.Attachment.builder()
            .contentType("text/plain")
            .isBase64("true")
            .description(TEXT_ATTACHMENT_DESCRIPTION)
            .payload("SGVsbG8gV29ybGQh")
            .build();
    }

    @BeforeAll
    static void setMockMissingFilenameAttachment() {
        mockMissingFilenameAttachment = InboundMessage.Attachment.builder()
            .contentType("text/plain")
            .isBase64("true")
            .description("Compressed=Yes")
            .payload("SGVsbG8gV29ybGQh")
            .build();
    }

    @BeforeAll
    static void setMockMissingContentTypeAttachment() {
        mockMissingContentTypeAttachment = InboundMessage.Attachment.builder()
            .contentType("text/plain")
            .isBase64("true")
            .description("Filename=\"helloWorld.txt\"")
            .payload("SGVsbG8gV29ybGQh")
            .build();
    }

    @Test
    public void When_InlineAttachmentConstructed_WithTextAttachment_Expect_OriginalFileNameIsCorrect() throws ParseException {
        InlineAttachment inlineAttachment = new InlineAttachment(mockAttachment);

        assertEquals(TEXT_ATTACHMENT_FILENAME, inlineAttachment.getOriginalFilename());
    }

    @Test
    public void When_InlineAttachmentConstructed_WithTextAttachment_Expect_CompressedIsCorrect() throws ParseException {
        InlineAttachment inlineAttachment = new InlineAttachment(mockAttachment);

        assertFalse(inlineAttachment.isCompressed());
    }

    @Test
    public void When_InlineAttachmentConstructed_WithTextAttachment_Expect_Base64IsCorrect() throws ParseException {
        InlineAttachment inlineAttachment = new InlineAttachment(mockAttachment);

        assertTrue(inlineAttachment.isBase64());
    }

    @Test
    public void When_InlineAttachmentConstructed_WithMissingFilename_Expect_ParseException() {
        Exception exception = assertThrows(ParseException.class, () -> new InlineAttachment(mockMissingFilenameAttachment));

        String expectedMessage = "Unable to parse originalFilename field in description";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void When_InlineAttachmentConstructed_WithIsCompressedMissing_Expect_ParseException() {
        Exception exception = assertThrows(ParseException.class, () -> new InlineAttachment(mockMissingContentTypeAttachment));

        String expectedMessage = "Unable to parse isCompressed field in description";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }


}
