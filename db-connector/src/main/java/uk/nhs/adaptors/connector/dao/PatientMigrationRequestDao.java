package uk.nhs.adaptors.connector.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface PatientMigrationRequestDao {

    @SqlUpdate("INSERT INTO patient_migration_request(patient_nhs_number, status) VALUES (:nhsNumber, :status);")
    void addNewRequest(@Bind("nhsNumber") String patientNhsNumber, @Bind("status") String status);

    @SqlQuery("SELECT EXISTS(SELECT 1 FROM patient_migration_request WHERE patient_nhs_number = :nhsNumber AND status IN ('IN_PROGRESS','RECEIVED'))")
    boolean isRequestInProgress(@Bind("nhsNumber") String patientNhsNumber);
}
