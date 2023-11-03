package uk.nhs.adaptors.connector.dao;

import java.time.OffsetDateTime;
import java.util.List;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import uk.nhs.adaptors.common.enums.MigrationStatus;
import uk.nhs.adaptors.connector.model.MigrationStatusLog;

public interface MigrationStatusLogDao {

    @SqlUpdate("insert_migration_status_log")
    @UseClasspathSqlLocator
    void addMigrationStatusLog(@Bind("status") MigrationStatus status,
        @Bind("date") OffsetDateTime date, @Bind("migrationRequestId") int migrationRequestId,
        @Bind("messageId") String messageId, @Bind("errorCode") String errorCode
    );

    @SqlQuery("select_migration_status_log")
    @UseClasspathSqlLocator
    MigrationStatusLog getLatestMigrationStatusLog(@Bind("migrationRequestId") int migrationRequestId);

    @SqlQuery("select_migration_status_logs")
    @UseClasspathSqlLocator
    List<MigrationStatusLog> getLatestMigrationStatusLogs(@Bind("migrationRequestId") int migrationRequestId);
}
