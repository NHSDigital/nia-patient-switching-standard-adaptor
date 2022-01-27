package uk.nhs.adaptors.connector.dao;

import java.time.OffsetDateTime;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import uk.nhs.adaptors.connector.model.MigrationStatusLog;

public interface MigrationStatusLogDao {

    @SqlUpdate("insert_migration_status_log")
    @UseClasspathSqlLocator
    void addMigrationStatusLog(@Bind("status") String status,
        @Bind("date") OffsetDateTime date, @Bind("migrationRequestId") int migrationRequestId
    );

    @SqlQuery("select_migration_status_log")
    @UseClasspathSqlLocator
    MigrationStatusLog getMigrationStatusLog(@Bind("migrationRequestId") int migrationRequestId);
}
