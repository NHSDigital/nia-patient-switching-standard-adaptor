UPDATE patient_migration_request SET bundle_resource=:bundle, ebxml_data=:ebXmlData
WHERE patient_nhs_number=:nhsNumber;
