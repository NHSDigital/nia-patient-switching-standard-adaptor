package uk.nhs.adaptors.connector.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Component;

import uk.nhs.adaptors.connector.model.ImmunizationSnomedCT;

@Component
public class ImmunizationSnomedCTMapper implements RowMapper<ImmunizationSnomedCT> {
    private static final String COLUMN_NAME = "concept_and_description_ids";

    @Override
    public ImmunizationSnomedCT map(ResultSet rs, StatementContext ctx) throws SQLException {
        return ImmunizationSnomedCT.builder()
            .snomedId(rs.getString(COLUMN_NAME))
            .build();
    }
}