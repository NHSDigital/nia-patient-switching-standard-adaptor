package uk.nhs.adaptors.connector.dao;

import java.time.OffsetDateTime;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import uk.nhs.adaptors.connector.model.PatientMigrationRequest;

public interface PatientMigrationRequestDao {

    @SqlUpdate("insert_patient_migration_request")
    @UseClasspathSqlLocator
    void addNewRequest(@Bind("nhsNumber") String patientNhsNumber, @Bind("status") String status, @Bind("date") OffsetDateTime date);

    @SqlQuery("select_patient_migration_request")
    @UseClasspathSqlLocator
    PatientMigrationRequest getMigrationRequest(@Bind("nhsNumber") String patientNhsNumber);
}
