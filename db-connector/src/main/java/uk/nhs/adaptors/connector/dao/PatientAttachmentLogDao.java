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
        @Bind() Boolean uploaded,
        @Bind() String patient_req_link,
        @Bind() Integer patient_migration_req_id,
        @Bind() Integer order_num
        );

    @SqlUpdate("update_patient_attachment_log")
    @UseClasspathSqlLocator
    void updateAttachmentLog(
        @Bind() String mid,
        @Bind() String filename,
        @Bind() Boolean uploaded

    );
}
