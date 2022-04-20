package uk.nhs.adaptors.connector.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import uk.nhs.adaptors.connector.model.PatientAttachmentLog;

public interface PatientAttachmentLogDao {

    @SuppressWarnings("checkstyle:parameternumber")
    @SqlUpdate("insert_patient_attachment_log")
    @UseClasspathSqlLocator
    void addAttachmentLog(
        @Bind() String mid,
        @Bind() String filename,
        @Bind() String parentMid,
        @Bind() Integer patientMigrationReqId,
        @Bind() String contentType,
        @Bind() Boolean compressed,
        @Bind() Boolean largeAttachment,
        @Bind() Boolean base64,
        @Bind() Boolean skeleton,
        @Bind() Boolean uploaded,
        @Bind() Integer orderNum,
        @Bind() Integer lengthNum
    );

    @SqlUpdate("delete_patient_attachment_log")
    @UseClasspathSqlLocator
    void deleteAttachmentLog(
        @Bind() String mid,
        @Bind() String conversationId
    );

    @SqlQuery("select_patient_attachment_log")
    @UseClasspathSqlLocator
    PatientAttachmentLog findPatientAttachment(@Bind("mid") String mid, String conversationId);

    @SuppressWarnings("checkstyle:parameternumber")
    @SqlUpdate("update_patient_attachment_log")
    @UseClasspathSqlLocator
    void updateAttachmentLog(
        @Bind() String mid,
        @Bind() String conversationId,
        @Bind() String parentMid,
        @Bind() String contentType,
        @Bind() Boolean compressed,
        @Bind() Boolean largeAttachment,
        @Bind() Boolean base64,
        @Bind() Boolean skeleton,
        @Bind() Boolean uploaded,
        @Bind() Integer lengthNum,
        @Bind() Integer orderNum
    );
}
