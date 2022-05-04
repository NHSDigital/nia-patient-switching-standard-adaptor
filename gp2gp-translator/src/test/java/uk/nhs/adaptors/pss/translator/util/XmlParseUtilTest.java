package uk.nhs.adaptors.pss.translator.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;

import org.junit.jupiter.api.Assertions;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.text.ParseException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class XmlParseUtilTest {

    private static final String DESCRIPTION = "\"Filename=\"E39E79A2-FA96-48FF-9373-7BBCB9D036E7_0.messageattachment\" ContentType=text/plain " +
        "Compressed=No LargeAttachment=No OriginalBase64=Yes";

    private final String description =  "Filename=\"68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs.ukExample1.gzip\" \n" +
            "\t\t\t\tContentType=text/xml \n" +
            "\t\t\t\tCompressed=Yes \n" +
            "\t\t\t\tLargeAttachment=No \n" +
            "\t\t\t\tOriginalBase64=Yes \n" +
            "\t\t\t\tLength=4718592 \n" +
            "\t\t\t\tDomainData=\"X-GP2GP-Skeleton: Yes\"";


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

        //need to ask about this one
        RCMRIN030000UK06Message rcmrin030000UK06Message = new RCMRIN030000UK06Message();

        rcmrin030000UK06Message.setControlActEvent(any());
        rcmrin030000UK06Message.getControlActEvent().setSubject(any());
        rcmrin030000UK06Message.getControlActEvent().getSubject().setEhrExtract(any());
        rcmrin030000UK06Message.getControlActEvent().getSubject().getEhrExtract().setRecordTarget(any());
        rcmrin030000UK06Message.getControlActEvent().getSubject().getEhrExtract().getRecordTarget().setPatient(any());
        rcmrin030000UK06Message.getControlActEvent().getSubject().getEhrExtract().getRecordTarget().getPatient().setId(any());
        rcmrin030000UK06Message.getControlActEvent().getSubject().getEhrExtract().getRecordTarget().getPatient().getId().setExtension(any());

        when(rcmrin030000UK06Message.getControlActEvent()
                .getSubject()
                .getEhrExtract()
                .getRecordTarget()
                .getPatient()
                .getId()
                .getExtension()).thenReturn("123456");

        String actual = XmlParseUtil.parseNhsNumber(rcmrin030000UK06Message);
        String expected = "123456";

        assertEquals(expected, actual);
    }

    @Test
    public void shouldReturnXWhenPatientNhsNumberCantBeRead() {

    }

    @Test
    public void shouldParseFilenameValue() throws ParseException {
        String actual = XmlParseUtil.parseFilename(description);
        String expected = "68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs.ukExample1.gzip";

        assertEquals(expected, actual);
    }

    @Test
    public void shouldThrowParseExceptionWhenFilenameCantBeRead() throws ParseException {

        Exception exceptionCase1 = assertThrows(
                ParseException.class, () -> XmlParseUtil.parseFilename(description.replace("Filename", ""))
        );

        Exception exceptionCase2 = assertThrows(
                ParseException.class, () -> XmlParseUtil.parseFilename(description.replace("\"68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs.ukExample1.gzip\"", ""))
        );

        Exception exceptionCase3 = assertThrows(
                ParseException.class, () -> XmlParseUtil.parseFilename(description.replace("Filename=\"68E2A39F-7A24-449D-83CC-1B7CF1A9DAD7spine.nhs.ukExample1.gzip\"", ""))
        );

        String expected = "Unable to parse originalFilename";

        assertTrue(exceptionCase1.getMessage().contains(expected));
        assertTrue(exceptionCase2.getMessage().contains(expected));
        assertTrue(exceptionCase3.getMessage().contains(expected));

    }

    @Test
    public void shouldParseFileLengthValue()  {
        int actual = XmlParseUtil.parseFileLength(description);
        int expected = 4718592;

        assertEquals(expected, actual);
    }

    @Test
    public void shouldThrowParseExceptionWhenFileLengthCantBeRead() {
        int actual1 = XmlParseUtil.parseFileLength(description.replace("Length", ""));
        int actual2 = XmlParseUtil.parseFileLength(description.replace("4718592", ""));
        int actual3 = XmlParseUtil.parseFileLength(description.replace("Length=4718592", ""));
        int expected = 0;

        assertEquals(expected, actual1);
        assertEquals(expected, actual2);
        assertEquals(expected, actual3);
    }

    @Test
    public void shouldParseSkeletonValue() {
        boolean actual = XmlParseUtil.parseIsSkeleton(description);
        boolean expected = true;

        assertEquals(expected, actual);
    }

    @Test
    public void shouldReturnXWhenSkeletonCantBeRead() {
        boolean actual1 = XmlParseUtil.parseIsSkeleton(description.replace("X-GP2GP-Skeleton: Yes", ""));
        boolean actual2 = XmlParseUtil.parseIsSkeleton(description.replace("X-GP2GP-Skeleton", ""));
        boolean actual3 = XmlParseUtil.parseIsSkeleton(description.replace("Yes", ""));

        boolean expected = false;

        assertEquals(expected, actual1);
        assertEquals(expected, actual2);
        assertEquals(expected, actual3);
    }
}