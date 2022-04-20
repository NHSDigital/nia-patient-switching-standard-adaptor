package uk.nhs.adaptors.connector.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PatientAttachmentLog implements Cloneable {
    @NonNull
    private String mid;
    private String parentMid;
//    @NonNull
    private String filename;
    private String contentType;
    private Boolean compressed;
    private Boolean largeAttachment;
    private Boolean base64;
    private Boolean skeleton;
    private Boolean uploaded;
    private Integer lengthNum;
//    @NonNull
    private Integer patientMigrationReqId;
    private Integer orderNum;
}
