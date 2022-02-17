UPDATE patient_migration_request SET bundle_resource = :bundle, inbound_message = :inboundMessage
WHERE patient_nhs_number = :nhsNumber;
