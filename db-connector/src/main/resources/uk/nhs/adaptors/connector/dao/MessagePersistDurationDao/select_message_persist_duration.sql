SELECT *
FROM message_persist_duration
WHERE migration_request_id = :migrationRequestId
  AND message_type = :messageType FETCH FIRST ROW ONLY;