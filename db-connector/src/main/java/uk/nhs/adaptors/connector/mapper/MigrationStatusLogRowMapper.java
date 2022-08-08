package uk.nhs.adaptors.connector.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Component;

import uk.nhs.adaptors.connector.model.MigrationStatusLog;
import uk.nhs.adaptors.connector.model.MigrationStatus;

@Component
public class MigrationStatusLogRowMapper implements RowMapper<MigrationStatusLog> {

    @Override
    public MigrationStatusLog map(ResultSet rs, StatementContext ctx) throws SQLException {
        return MigrationStatusLog.builder()
            .id(rs.getInt("id"))
            .migrationStatus(MigrationStatus.valueOf(rs.getString("status")))
            .date(rs.getObject("date", OffsetDateTime.class))
            .migrationRequestId(rs.getInt("migration_request_id"))
            .messageId(rs.getString("message_id"))
            .build();
    }
}
