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
            .parent_mid(rs.getString("parent_mid"))
            .filename(rs.getString("filename"))
            .content_type(rs.getString("content_type"))
            .patient_migration_req_id(rs.getInt("patient_migration_req_id"))
            .compressed(rs.getBoolean("compressed"))
            .large_attachment(rs.getBoolean("large_attachment"))
            .base64(rs.getBoolean("base64"))
            .skeleton(rs.getBoolean("skeleton"))
            .uploaded(rs.getBoolean("uploaded"))
            .length_num(rs.getInt("length_num"))
            .order_num(rs.getInt("order_num"))
            .build();
    }
}
