package uk.nhs.adaptors.connector.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Component;

import uk.nhs.adaptors.connector.model.ImmunizationSnomedCT;

@Component
public class ImmunizationSnomedCTMapper implements RowMapper<ImmunizationSnomedCT> {
    @Override
    public ImmunizationSnomedCT map(ResultSet rs, StatementContext ctx) throws SQLException {
        return ImmunizationSnomedCT.builder()
            .conceptId(rs.getString("conceptId"))
            .build();
    }
}
