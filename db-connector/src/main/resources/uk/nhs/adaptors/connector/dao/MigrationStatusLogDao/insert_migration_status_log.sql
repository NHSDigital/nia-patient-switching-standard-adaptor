INSERT INTO migration_status_log(status, date, migration_request_id, message_id, gp2gp_error_code)
VALUES (:status, :date, :migrationRequestId, :messageId, :gp2gpErrorCode);