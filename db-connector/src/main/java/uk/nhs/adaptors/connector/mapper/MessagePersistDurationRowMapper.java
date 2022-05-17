package uk.nhs.adaptors.connector.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Component;

import uk.nhs.adaptors.connector.model.MessagePersistDuration;

@Component
public class MessagePersistDurationRowMapper implements RowMapper<MessagePersistDuration> {

    @Override
    public MessagePersistDuration map(ResultSet rs, StatementContext ctx) throws SQLException {
        return MessagePersistDuration.builder()
            .id(rs.getInt("id"))
            .messageType(rs.getString("message_type"))
            .persistDuration(Duration.ofSeconds(rs.getInt("persist_duration")))
            .callsSinceUpdate(rs.getInt("calls_since_update"))
            .migrationRequestId(rs.getInt("migration_request_id"))
            .build();
    }
}
