package uk.nhs.adaptors.pss.translator.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    private static InboundMessage.Attachment textAttachment;

    @BeforeAll
    static void setAttachment() {

        textAttachment = InboundMessage.Attachment.builder()
            .contentType("text/plain")
            .isBase64("true")
            .description(TEXT_ATTACHMENT_DESCRIPTION)
            .payload("SGVsbG8gV29ybGQgZnJvbSBTY290dCBBbGV4YW5kZXI=")
            .build();
    }

    @Test
    public void When_inlineAttachmentConstructed_withTextAttachment_Expect_OriginalFileNameIsCorrect() throws ParseException {
        InlineAttachment inlineAttachment = new InlineAttachment(textAttachment);

        assertEquals(TEXT_ATTACHMENT_FILENAME, inlineAttachment.getOriginalFilename());
    }

    @Test
    public void When_inlineAttachmentConstructed_withTextAttachment_Expect_CompressedIsCorrect() throws ParseException {
        InlineAttachment inlineAttachment = new InlineAttachment(textAttachment);

        assertFalse(inlineAttachment.isCompressed());
    }

    @Test
    public void When_inlineAttachmentConstructed_withTextAttachment_Expect_Base64IsCorrect() throws ParseException {
        InlineAttachment inlineAttachment = new InlineAttachment(textAttachment);

        assertTrue(inlineAttachment.isBase64());
    }
}
