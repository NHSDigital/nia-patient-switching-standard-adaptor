package uk.nhs.adaptors.connector.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface PatientMigrationDataDao {

    @SqlUpdate("insert_patient_migration_data")
    @UseClasspathSqlLocator
    void addPatientData(@Bind() String status);
    INSERT INTO patient_migration_data(mid, filename, uploaded, patient_req_link, patient_migration_req_id, orderNum)

//    @SqlQuery("select_patient_migration_data")
//    @UseClasspathSqlLocator
//    PatientMigrationData getPatientMigrationData(@Bind() String migrationRequestId);
}
