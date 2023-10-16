package uk.nhs.adaptors.connector.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PatientAttachmentLogTest {

    public static final int ONE_HUNDRED = 100;
    public static final int ONE_HUNDRED_AND_FIVE = 105;

    @Test
    public void When_GetFileDescription_With_ValuesTrue_Expect_CorrectDescription() {
        PatientAttachmentLog patientAttachmentLog = PatientAttachmentLog.builder()
            .mid("TEST_MID")
            .filename("TEST_FILENAME.txt")
            .contentType("plain/text")
            .compressed(true)
            .largeAttachment(true)
            .originalBase64(true)
            .skeleton(true)
            .uploaded(true)
            .lengthNum(ONE_HUNDRED)
            .patientMigrationReqId(1)
            .orderNum(0)
            .deleted(true)
            .postProcessedLengthNum(ONE_HUNDRED_AND_FIVE)
            .isBase64(true)
            .build();

        String expectedDescription = "Filename=\"TEST_FILENAME.txt\" ContentType=plain/text Compressed=Yes "
            + "LargeAttachment=Yes OriginalBase64=Yes Length=100 DomainData=\"X-GP2GP-Skeleton: Yes\"";

        String description = patientAttachmentLog.getFileDescription();

        assertEquals(expectedDescription, description);
    }

    @Test
    public void When_GetFileDescription_With_ValuesFalse_Expect_CorrectDescription() {
        PatientAttachmentLog patientAttachmentLog = PatientAttachmentLog.builder()
            .mid("TEST_MID")
            .filename("TEST_FILENAME.txt")
            .contentType("plain/text")
            .compressed(false)
            .largeAttachment(false)
            .originalBase64(false)
            .skeleton(false)
            .uploaded(false)
            .lengthNum(ONE_HUNDRED)
            .patientMigrationReqId(1)
            .orderNum(0)
            .deleted(false)
            .postProcessedLengthNum(ONE_HUNDRED_AND_FIVE)
            .isBase64(false)
            .build();

        String expectedDescription = "Filename=\"TEST_FILENAME.txt\" ContentType=plain/text Compressed=No "
            + "LargeAttachment=No OriginalBase64=No Length=100";

        String description = patientAttachmentLog.getFileDescription();

        assertEquals(expectedDescription, description);
    }
}
