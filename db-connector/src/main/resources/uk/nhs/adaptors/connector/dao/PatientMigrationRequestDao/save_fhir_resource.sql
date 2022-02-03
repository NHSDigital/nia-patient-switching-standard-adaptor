UPDATE patient_migration_request SET fhir_resource=:resource
WHERE patient_nhs_number=:nhsNumber;
