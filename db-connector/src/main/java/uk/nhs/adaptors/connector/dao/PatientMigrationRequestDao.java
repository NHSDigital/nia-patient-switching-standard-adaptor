package uk.nhs.adaptors.connector.dao;

import java.time.OffsetDateTime;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import uk.nhs.adaptors.connector.model.PatientMigrationRequest;

public interface PatientMigrationRequestDao {

    @SqlUpdate("INSERT INTO patient_migration_request(patient_nhs_number, status, date) VALUES (:nhsNumber, :status, :date);")
    void addNewRequest(@Bind("nhsNumber") String patientNhsNumber, @Bind("status") String status, @Bind("date") OffsetDateTime date);

    @SqlQuery("SELECT * FROM patient_migration_request WHERE patient_nhs_number = :nhsNumber;")
    PatientMigrationRequest getMigrationRequest(@Bind("nhsNumber") String patientNhsNumber);
}
