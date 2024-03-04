package uk.nhs.adaptors.pss.translator.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;

import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void setMockAttachment() {
        mockAttachment = InboundMessage.Attachment.builder()
            .contentType("text/plain")
            .isBase64("true")
            .description(TEXT_ATTACHMENT_DESCRIPTION)
            .payload("SGVsbG8gV29ybGQh")
            .build();
    }

    @BeforeEach
    void setMockMissingFilenameAttachment() {
        mockMissingFilenameAttachment = InboundMessage.Attachment.builder()
            .contentType("text/plain")
            .isBase64("true")
            .description("Compressed=Yes")
            .payload("SGVsbG8gV29ybGQh")
            .build();
    }

    @BeforeEach
    void setMockMissingContentTypeAttachment() {
        mockMissingContentTypeAttachment = InboundMessage.Attachment.builder()
            .contentType("text/plain")
            .isBase64("true")
            .description("Filename=\"helloWorld.txt\"")
            .payload("SGVsbG8gV29ybGQh")
            .build();
    }

    @Test
    public void When_InlineAttachmentFromAttachment_Expect_OriginalFileNameIsCorrect() throws ParseException {
        InlineAttachment inlineAttachment = InlineAttachment.fromInboundMessageAttachment(mockAttachment);

        assertEquals(TEXT_ATTACHMENT_FILENAME, inlineAttachment.getOriginalFilename());
    }

    @Test
    public void When_InlineAttachmentFromAttachment_Expect_PayloadIsCorrect() throws ParseException {
        InlineAttachment inlineAttachment = InlineAttachment.fromInboundMessageAttachment(mockAttachment);

        assertEquals("SGVsbG8gV29ybGQh", inlineAttachment.getPayload());
    }

    @Test
    public void When_InlineAttachmentFromAttachment_Expect_DescriptionIsCorrect() throws ParseException {
        InlineAttachment inlineAttachment = InlineAttachment.fromInboundMessageAttachment(mockAttachment);

        assertEquals(TEXT_ATTACHMENT_DESCRIPTION, inlineAttachment.getDescription());
    }

    @Test
    public void When_InlineAttachmentFromAttachment_Expect_LengthIsCorrect() throws ParseException {
        final int MAGIC_NUMBER = 180;
        mockAttachment.setDescription(TEXT_ATTACHMENT_DESCRIPTION + " Length=" + MAGIC_NUMBER);
        InlineAttachment inlineAttachment = InlineAttachment.fromInboundMessageAttachment(mockAttachment);

        assertEquals(MAGIC_NUMBER, inlineAttachment.getLength());
    }

    @Test
    public void When_InlineAttachmentFromAttachment_Expect_CompressedIsCorrect() throws ParseException {
        InlineAttachment inlineAttachment = InlineAttachment.fromInboundMessageAttachment(mockAttachment);

        assertFalse(inlineAttachment.isCompressed());
    }

    @Test
    public void When_InlineAttachmentFromAttachment_Expect_Base64IsCorrect() throws ParseException {
        InlineAttachment inlineAttachment = InlineAttachment.fromInboundMessageAttachment(mockAttachment);

        assertTrue(inlineAttachment.isBase64());
    }

    @Test
    public void When_InlineAttachmentFromAttachment_WithMissingFilename_Expect_ParseException() {
        Exception exception = assertThrows(ParseException.class,
                () -> InlineAttachment.fromInboundMessageAttachment(mockMissingFilenameAttachment));

        assertThat(exception.getMessage()).contains("Unable to parse originalFilename field in description");
    }

    @Test
    public void When_InlineAttachmentFromAttachment_WithIsCompressedMissing_Expect_ParseException() {
        Exception exception = assertThrows(ParseException.class,
                () -> InlineAttachment.fromInboundMessageAttachment(mockMissingContentTypeAttachment));

        assertThat(exception.getMessage()).contains("Unable to parse isCompressed field in description");
    }


}
