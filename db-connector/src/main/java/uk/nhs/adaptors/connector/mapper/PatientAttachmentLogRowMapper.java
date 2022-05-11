package uk.nhs.adaptors.connector.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Component;

import uk.nhs.adaptors.connector.model.PatientAttachmentLog;

@Component
public class PatientAttachmentLogRowMapper implements RowMapper<PatientAttachmentLog> {

    @Override
    public PatientAttachmentLog map(ResultSet rs, StatementContext ctx) throws SQLException {
        return PatientAttachmentLog.builder()
            .mid(rs.getString("mid"))
            .parentMid(rs.getString("parent_mid"))
            .filename(rs.getString("filename"))
            .contentType(rs.getString("content_type"))
            .patientMigrationReqId(rs.getInt("patient_migration_req_id"))
            .compressed(rs.getBoolean("compressed"))
            .largeAttachment(rs.getBoolean("large_attachment"))
            .base64(rs.getBoolean("base64"))
            .skeleton(rs.getBoolean("skeleton"))
            .uploaded(rs.getBoolean("uploaded"))
            .lengthNum(rs.getInt("length_num"))
            .orderNum(rs.getInt("order_num"))
            .deleted((rs.getBoolean("deleted")))
            .build();
    }
}
