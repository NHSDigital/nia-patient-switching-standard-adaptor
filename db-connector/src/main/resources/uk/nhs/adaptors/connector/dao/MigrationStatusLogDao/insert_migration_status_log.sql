INSERT INTO migration_status_log(status, date, migration_request_id, message_id, error_code)
VALUES (:status, :date, :migrationRequestId, :messageId, :errorCode);