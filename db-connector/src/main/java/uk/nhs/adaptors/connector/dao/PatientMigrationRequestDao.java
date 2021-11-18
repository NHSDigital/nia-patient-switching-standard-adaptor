package uk.nhs.adaptors.connector.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface PatientMigrationRequestDao {
    @SqlUpdate("INSERT INTO public.patient_migration_request(id) VALUES (:id)")
    int insertPatientMigrationRequest(@Bind("id") String id);

    @SqlUpdate("DELETE FROM public.patient_migration_request WHERE id = :id")
    void deletePatientMigrationRequest(@Bind("id") String id);
}
