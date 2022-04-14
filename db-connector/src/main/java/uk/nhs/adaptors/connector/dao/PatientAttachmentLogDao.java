package uk.nhs.adaptors.connector.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import uk.nhs.adaptors.connector.model.PatientAttachmentLog;

public interface PatientAttachmentLogDao {

    @SqlUpdate("insert_patient_attachment_log")
    @UseClasspathSqlLocator
    void addAttachmentLog(
        @Bind() String mid,
        @Bind() String filename,
        @Bind() String parent_mid,
        @Bind() Integer patient_migration_req_id,
        @Bind() String content_type,
        @Bind() Boolean compressed,
        @Bind() Boolean large_attachment,
        @Bind() Boolean base64,
        @Bind() Boolean skeleton,
        @Bind() Boolean uploaded,
        @Bind() Integer order_num,
        @Bind() Integer length_num

    );
    @SqlUpdate("delete_patient_attachment_log")
    @UseClasspathSqlLocator
    void deleteAttachmentLog(
        @Bind() String mid,
        @Bind() String conversation_id
    );

    @SqlQuery("select_patient_attachment_log")
    @UseClasspathSqlLocator
    PatientAttachmentLog findPatientAttachment(@Bind("mid") String mid, String conversation_id);

    @SqlUpdate("update_patient_attachment_log")
    @UseClasspathSqlLocator
    void updateAttachmentLog(
        @Bind() String mid,
        @Bind() String conversation_id,
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
