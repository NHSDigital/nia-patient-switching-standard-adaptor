package uk.nhs.adaptors.connector.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import uk.nhs.adaptors.connector.model.PatientMigrationRequest;

public interface PatientMigrationRequestDao {

    @SqlUpdate("insert_patient_migration_request")
    @UseClasspathSqlLocator
    void addNewRequest(@Bind("nhsNumber") String patientNhsNumber);

    @SqlQuery("select_patient_migration_request")
    @UseClasspathSqlLocator
    PatientMigrationRequest getMigrationRequest(@Bind("nhsNumber") String patientNhsNumber);

    @SqlQuery("select_patient_migration_request_id")
    @UseClasspathSqlLocator
    int getMigrationRequestId(@Bind("nhsNumber") String patientNhsNumber);

    @SqlUpdate("save_bundle_resource_and_ebxml_data")
    @UseClasspathSqlLocator
    void saveBundleAndEbXmlData(@Bind("nhsNumber") String patientNhsNumber, @Bind("bundle") String bundle, @Bind("ebXmlData") String ebXmlData);
}
