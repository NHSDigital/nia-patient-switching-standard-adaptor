package uk.nhs.adaptors.connector.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PatientAttachmentLog {

    @NonNull
    private String mid;
    private String parentMid;
    private String filename;
    private String contentType;
    private Boolean compressed;
    private Boolean largeAttachment;
    private Boolean originalBase64;
    private Boolean skeleton;
    private Boolean uploaded;
    private Integer lengthNum;
    private Integer patientMigrationReqId;
    private Integer orderNum;
    private Boolean deleted;
    private Integer postProcessedLengthNum;
    private Boolean isBase64;

    public String getFileDescription() {
        String description = String.format(
            "Filename=\"%s\" ContentType=%s Compressed=%s LargeAttachment=%s OriginalBase64=%s Length=%s",
            getFilename(),
            getContentType(),
            getYesNoString(getCompressed()),
            getYesNoString(getLargeAttachment()),
            getYesNoString(getOriginalBase64()),
            getLengthNum()
        );

        if (getSkeleton()) {
            description += " DomainData=\"X-GP2GP-Skeleton: Yes\"";
        }

        return description;
    }

    private String getYesNoString(boolean bool) {
        return bool ? "Yes" : "No";
    }
}
