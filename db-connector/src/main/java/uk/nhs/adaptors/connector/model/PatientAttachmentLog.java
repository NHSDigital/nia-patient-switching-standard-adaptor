package uk.nhs.adaptors.connector.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PatientAttachmentLog {
    private String mid;
    private String parent_mid;
    private String filename;
    private String content_type;
    private Boolean compressed;
    private Boolean large_attachment;
    private Boolean base64;
    private Boolean skeleton;
    private Boolean uploaded;
    private Integer length;
    private Integer patient_migration_req_id;
    private Integer order_num;

}
