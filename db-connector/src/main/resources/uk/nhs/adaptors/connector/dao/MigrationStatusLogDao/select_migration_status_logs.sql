SELECT * FROM migration_status_log WHERE migration_request_id = :migrationRequestId
ORDER BY "date";