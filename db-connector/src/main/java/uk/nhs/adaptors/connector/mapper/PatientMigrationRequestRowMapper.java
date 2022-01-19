package uk.nhs.adaptors.connector.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Component;

import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.model.RequestStatus;

@Component
public class PatientMigrationRequestRowMapper implements RowMapper<PatientMigrationRequest> {

    @Override
    public PatientMigrationRequest map(ResultSet rs, StatementContext ctx) throws SQLException {
        return PatientMigrationRequest.builder()
            .id(rs.getInt("id"))
            .patientNhsNumber(rs.getString("patient_nhs_number"))
            .build();
    }
}
