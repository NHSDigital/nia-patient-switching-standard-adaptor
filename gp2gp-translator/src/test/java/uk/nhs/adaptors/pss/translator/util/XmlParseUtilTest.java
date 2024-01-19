package uk.nhs.adaptors.pss.translator.util;

import org.hl7.v3.RCMRIN030000UK06Message;
import org.hl7.v3.RCMRIN030000UK06ControlActEvent;
import org.hl7.v3.RCMRIN030000UK06Subject;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04PatientSubject;
import org.hl7.v3.II;
import org.hl7.v3.RCMRMT030101UKPatient;
import org.hl7.v3.RCMRMT030101UKPatientSubject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.ParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class XmlParseUtilTest {

    private static final String DOCX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private static final String DESCRIPTION =
        "\"Filename=\"E39E79A2-FA96-48FF-9373-7BBCB9D036E7_0.messageattachment\" ContentType=text/plain "
            + "DomainData=\"X-GP2GP-Skeleton: Yes\" Compressed=No LargeAttachment=No OriginalBase64=Yes Length=4718592";

    @Mock
    private RCMRIN030000UK06Message rcmrin030000UK06Message;
    @Mock
    private RCMRIN030000UK06ControlActEvent rcmrin030000UK06ControlActEvent;
    @Mock
    private RCMRIN030000UK06Subject rcmrin030000UK06Subject;
    @Mock
    private RCMRMT030101UK04EhrExtract rcmrmt030101UK04EhrExtract;
    @Mock
    private RCMRMT030101UKPatientSubject rcmrmt030101UK04PatientSubject;
    @Mock
    private RCMRMT030101UKPatient rcmrmt030101UK04Patient;
    @Mock
    private II id;

    @Test
    public void shouldParsePatientNhsNumberValue() {

        when(rcmrin030000UK06Message.getControlActEvent()).thenReturn(rcmrin030000UK06ControlActEvent);

        when(rcmrin030000UK06Message.getControlActEvent().getSubject()).thenReturn(rcmrin030000UK06Subject);

        when(rcmrin030000UK06Message.getControlActEvent().getSubject().getEhrExtract()).thenReturn(rcmrmt030101UK04EhrExtract);

        when(
                rcmrin030000UK06Message
                    .getControlActEvent()
                    .getSubject()
                    .getEhrExtract()
                    .getRecordTarget()
        ).thenReturn(rcmrmt030101UK04PatientSubject);

        when(
                rcmrin030000UK06Message
                    .getControlActEvent()
                    .getSubject()
                    .getEhrExtract()
                    .getRecordTarget()
                    .getPatient()
        ).thenReturn(rcmrmt030101UK04Patient);

        when(
                rcmrin030000UK06Message
                        .getControlActEvent()
                        .getSubject()
                        .getEhrExtract()
                        .getRecordTarget()
                        .getPatient()
                        .getId()
        ).thenReturn(id);

        when(rcmrin030000UK06Message.getControlActEvent()
                .getSubject()
                .getEhrExtract()
                .getRecordTarget()
                .getPatient()
                .getId()
                .getExtension()).thenReturn("123456");

        String actual = XmlParseUtilService.parseNhsNumber(rcmrin030000UK06Message);
        String expected = "123456";

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseFragmentFilenameAndReturnFilename() throws ParseException {

        String expected = "E39E79A2-FA96-48FF-9373-7BBCB9D036E7_0.messageattachment";
        // Act
        String actual = XmlParseUtilService.parseFilename(DESCRIPTION);
        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldReturnEmptyStringIfDoesNotContainFilename() {
        // Arrange
        String description = "Compressed=No LargeAttachment=No OriginalBase64=Yes";
        // Act
        Assertions.assertThrows(ParseException.class, () ->
            XmlParseUtilService.parseFilename(description)
        );
    }

    @Test
    public void shouldParseOriginalBase64Value() throws ParseException {
        // Arrange

        // Act
        boolean actual = XmlParseUtilService.parseOriginalBase64(DESCRIPTION);
        // Assert
        assertThat(actual).isTrue();
    }

    @Test()
    public void shouldThrowParseExceptionWhenOriginalBase64CantBeRead() {

        String description = "Compressed=No LargeAttachment=No";

        Assertions.assertThrows(ParseException.class, () ->
            XmlParseUtilService.parseOriginalBase64(description)
        );
    }

    @Test
    public void shouldParseLargeAttachmentValue() throws ParseException {

        // Act
        boolean actual = XmlParseUtilService.parseLargeAttachment(DESCRIPTION);
        // Assert
        assertThat(actual).isFalse();
    }

    @Test
    public void shouldThrowParseExceptionWhenLargeAttachmentCantBeRead() {
        String description = "Compressed=No ";

        Assertions.assertThrows(ParseException.class, () ->
            XmlParseUtilService.parseLargeAttachment(description)
        );
    }

    @Test
    public void shouldParseCompressedValue() throws ParseException {

        // Act
        boolean actual = XmlParseUtilService.parseCompressed(DESCRIPTION);
        // Assert
        assertThat(actual).isFalse();
    }

    @Test
    public void shouldThrowParseExceptionWhenCompressedCantBeRead() {
        String description = "LargeAttachment=No";

        Assertions.assertThrows(ParseException.class, () ->
            XmlParseUtilService.parseCompressed(description)
        );
    }

    @Test
    public void shouldParseContentTypeValue() throws ParseException {
        // Arrange
        // Act
        String actual = XmlParseUtilService.parseContentType(DESCRIPTION);
        String expected = "text/plain";
        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldParseContentTypeWithDotSyntax() throws ParseException {
        String actual = XmlParseUtilService.parseContentType("ContentType=" + DOCX_CONTENT_TYPE);

        assertThat(actual).isEqualTo(DOCX_CONTENT_TYPE);
    }

    @Test
    public void shouldThrowParseExceptionWhenContentTypeCantBeRead() {
        String description = "Compressed=No LargeAttachment=No";

        Assertions.assertThrows(ParseException.class, () ->
            XmlParseUtilService.parseContentType(description)
        );
    }

    @Test
    public void shouldParseFilenameValue() throws ParseException {
        String actual = XmlParseUtilService.parseFilename(DESCRIPTION);
        String expected = "E39E79A2-FA96-48FF-9373-7BBCB9D036E7_0.messageattachment";

        assertEquals(expected, actual);
    }

    @Test
    public void shouldThrowParseExceptionWhenFilenameCantBeRead() {

        Exception exceptionCase1 = assertThrows(
                ParseException.class,
                () -> XmlParseUtilService.parseFilename(DESCRIPTION.replace("Filename", ""))
        );

        Exception exceptionCase2 = assertThrows(
                ParseException.class,
                () -> XmlParseUtilService.parseFilename(
                    DESCRIPTION.replace("\"E39E79A2-FA96-48FF-9373-7BBCB9D036E7_0.messageattachment\"", ""))
        );

        Exception exceptionCase3 = assertThrows(
                ParseException.class,
                () -> XmlParseUtilService.parseFilename(
                        DESCRIPTION.replace("Filename=\"E39E79A2-FA96-48FF-9373-7BBCB9D036E7_0.messageattachment\"", "")
                )
        );

        String expected = "Unable to parse originalFilename";

        assertTrue(exceptionCase1.getMessage().contains(expected));
        assertTrue(exceptionCase2.getMessage().contains(expected));
        assertTrue(exceptionCase3.getMessage().contains(expected));

    }

    @Test
    public void shouldParseFileLengthValue()  {
        int actual = XmlParseUtilService.parseFileLength(DESCRIPTION);
        final int EXPECTED = 4718592;

        assertEquals(EXPECTED, actual);
    }

    @Test
    public void shouldThrowParseExceptionWhenFileLengthCantBeRead() {
        int actual1 = XmlParseUtilService.parseFileLength(DESCRIPTION.replace("Length", ""));
        int actual2 = XmlParseUtilService.parseFileLength(DESCRIPTION.replace("4718592", ""));
        int actual3 = XmlParseUtilService.parseFileLength(DESCRIPTION.replace("Length=4718592", ""));
        int expected = 0;

        assertEquals(expected, actual1);
        assertEquals(expected, actual2);
        assertEquals(expected, actual3);
    }

    @Test
    public void shouldParseSkeletonValue() {
        boolean actual = XmlParseUtilService.parseIsSkeleton(DESCRIPTION);
        boolean expected = true;

        assertEquals(expected, actual);
    }

    @Test
    public void shouldReturnXWhenSkeletonCantBeRead() {
        boolean actual1 = XmlParseUtilService.parseIsSkeleton(DESCRIPTION.replace("X-GP2GP-Skeleton: Yes", ""));
        boolean actual2 = XmlParseUtilService.parseIsSkeleton(DESCRIPTION.replace("X-GP2GP-Skeleton", ""));
        boolean actual3 = XmlParseUtilService.parseIsSkeleton(DESCRIPTION.replace("Yes", ""));

        boolean expected = false;

        assertEquals(expected, actual1);
        assertEquals(expected, actual2);
        assertEquals(expected, actual3);
    }
}