package uk.nhs.adaptors.connector.model;

import org.jdbi.v3.core.enums.EnumByName;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PatientMigrationData {
    private String mid;
    @EnumByName
    private String parent_mid;
    private String filename;
    private String content_type;
    private Boolean compressed;
    private Boolean large_attachment;
    private Boolean base64;
    private Boolean skeleton;
    private Boolean uploaded;
    private Integer length;
    private String patient_req_link;
    private Integer patient_migration_req_id;
    private Integer Order;

}
