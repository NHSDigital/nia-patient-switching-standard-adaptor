package uk.nhs.adaptors.connector.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Component;

import uk.nhs.adaptors.connector.model.PatientMigrationRequest;

@Component
public class PatientMigrationRequestRowMapper implements RowMapper<PatientMigrationRequest> {

    @Override
    public PatientMigrationRequest map(ResultSet rs, StatementContext ctx) throws SQLException {
        return PatientMigrationRequest.builder()
            .id(rs.getInt("id"))
            .patientNhsNumber(rs.getString("patient_nhs_number"))
            .bundleResource(rs.getString("bundle_resource"))
            .inboundMessage(rs.getString("inbound_message"))
            .conversationId(rs.getString("conversation_id"))
            .losingPracticeOdsCode(rs.getString("losing_practice_ods_code"))
            .winningPracticeOdsCode(rs.getString("winning_practice_ods_code"))
            .build();
    }
}
