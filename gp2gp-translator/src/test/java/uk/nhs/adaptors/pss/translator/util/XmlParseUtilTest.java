package uk.nhs.adaptors.pss.translator.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class XmlParseUtilTest {

    private static final String DESCRIPTION = "\"Filename=\"E39E79A2-FA96-48FF-9373-7BBCB9D036E7_0.messageattachment\" ContentType=text/plain " +
        "Compressed=No LargeAttachment=No OriginalBase64=Yes";

    @Test
    public void shouldParseFragmentFilenameAndReturnFilename() {

        String expected = "E39E79A2-FA96-48FF-9373-7BBCB9D036E7_0.messageattachment";
        // Act
        String actual = XmlParseUtil.parseFragmentFilename(DESCRIPTION);
        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldReturnEmptyStringIfDoesNotContainFilename() {
        // Arrange
        String description = "Compressed=No LargeAttachment=No OriginalBase64=Yes";
        // Act
        String actual = XmlParseUtil.parseFragmentFilename(description);
        // Assert
        assertThat(actual).isEmpty();
    }

    @Test
    public void shouldParseBase64Value() throws ParseException {
        // Arrange

        // Act
        boolean actual = XmlParseUtil.parseBase64(DESCRIPTION);
        // Assert
        assertThat(actual).isTrue();
    }

    @Test()
    public void shouldThrowParseExceptionWhenBase64CantBeRead() {

        String description = "Compressed=No LargeAttachment=No";

        Assertions.assertThrows(ParseException.class, ()->{
            XmlParseUtil.parseBase64(description);
        });
    }

    @Test
    public void shouldParseLargeAttachmentValue() throws ParseException {

        // Act
        boolean actual = XmlParseUtil.parseLargeAttachment(DESCRIPTION);
        // Assert
        assertThat(actual).isFalse();
    }

    @Test
    public void shouldThrowParseExceptionWhenLargeAttachmentCantBeRead() {
        String description = "Compressed=No ";

        Assertions.assertThrows(ParseException.class, ()->{
            XmlParseUtil.parseLargeAttachment(description);
        });
    }

    @Test
    public void shouldParseCompressedValue() throws ParseException {

        // Act
        boolean actual = XmlParseUtil.parseCompressed(DESCRIPTION);
        // Assert
        assertThat(actual).isFalse();
    }

    @Test
    public void shouldThrowParseExceptionWhenCompressedCantBeRead() {
        String description = "LargeAttachment=No";

        Assertions.assertThrows(ParseException.class, ()->{
            XmlParseUtil.parseCompressed(description);
        });
    }

    @Test
    public void shouldParseContentTypeValue() throws ParseException {
        // Arrange
        // Act
        String actual = XmlParseUtil.parseContentType(DESCRIPTION);
        String expected = "text/plain";
        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldThrowParseExceptionWhenContentTypeCantBeRead() {
        String description = "Compressed=No LargeAttachment=No";

        Assertions.assertThrows(ParseException.class, ()->{
            XmlParseUtil.parseContentType(description);
        });
    }

    @Test
    public void shouldParsePatientNhsNumberValue() {

    }

    @Test
    public void shouldReturnXWhenPatientNhsNumberCantBeRead() {

    }

    @Test
    public void shouldParseFilenameValue() {

    }

    @Test
    public void shouldThrowParseExceptionWhenFilenameCantBeRead() {

    }

    @Test
    public void shouldParseFileLengthValue() {

    }

    @Test
    public void shouldThrowParseExceptionWhenFileLengthCantBeRead() {

    }

    @Test
    public void shouldParseSkeletonValue() {

    }

    @Test
    public void shouldReturnXWhenSkeletonCantBeRead() {

    }
}