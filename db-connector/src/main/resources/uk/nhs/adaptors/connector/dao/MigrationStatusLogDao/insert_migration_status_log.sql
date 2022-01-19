INSERT INTO migration_status_log(patient_nhs_number, status, date, migration_request_id)
VALUES (:nhsNumber, :status, :date, :migrationRequestId);