package uk.nhs.adaptors.connector.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface PatientAttachmentLogDao {

    @SqlUpdate("insert_patient_attachment_log")
    @UseClasspathSqlLocator
    void addAttachmentLog(
        @Bind() String mid,
        @Bind() String filename,
        @Bind() Integer patient_migration_req_id,
        @Bind() Boolean uploaded,
        @Bind() Integer order_num
        );

    @SqlUpdate("delete_patient_attachment_log")
    @UseClasspathSqlLocator
    void deleteAttachmentLog(
        @Bind() String filename
    );

    @SqlUpdate("update_patient_attachment_log")
    @UseClasspathSqlLocator
    void updateAttachmentLog(
        @Bind() String mid,
        @Bind() String parent_mid,
        @Bind() String content_type,
        @Bind() Boolean compressed,
        @Bind() Boolean large_attachment,
        @Bind() Boolean base64,
        @Bind() Boolean skeleton,
        @Bind() Boolean uploaded,
        @Bind() Integer length_num,
        @Bind() Integer order_num

    );
}
