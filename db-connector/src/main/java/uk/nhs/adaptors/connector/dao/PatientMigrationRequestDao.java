package uk.nhs.adaptors.connector.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import uk.nhs.adaptors.connector.model.PatientMigrationRequest;

public interface PatientMigrationRequestDao {

    @SqlUpdate("insert_patient_migration_request")
    @UseClasspathSqlLocator
    void addNewRequest(@Bind("nhsNumber") String patientNhsNumber, @Bind("conversationId") String conversationId,
        @Bind("losingOdsCode") String losingOdsCode);

    @SqlQuery("select_patient_migration_request")
    @UseClasspathSqlLocator
    PatientMigrationRequest getMigrationRequest(@Bind("conversationId") String conversationId);

    @SqlQuery("select_patient_migration_request_id")
    @UseClasspathSqlLocator
    int getMigrationRequestId(@Bind("conversationId") String conversationId);

    @SqlUpdate("save_bundle_resource_and_inbound_message_data")
    @UseClasspathSqlLocator
    void saveBundleAndInboundMessageData(@Bind("conversationId") String conversationId, @Bind("bundle") String bundle,
        @Bind("inboundMessage") String inboundMessage);
}
