package uk.nhs.adaptors.connector.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Component;

import uk.nhs.adaptors.connector.model.SnomedCTDescription;

@Component
public class SnomedCTDescriptionMapper implements RowMapper<SnomedCTDescription> {

    @Override
    public SnomedCTDescription map(ResultSet rs, StatementContext ctx) throws SQLException {
        return SnomedCTDescription.builder()
            .id(rs.getString("id"))
            .conceptid(rs.getString("conceptid"))
            .term(rs.getString("term"))
            .build();
    }
}
