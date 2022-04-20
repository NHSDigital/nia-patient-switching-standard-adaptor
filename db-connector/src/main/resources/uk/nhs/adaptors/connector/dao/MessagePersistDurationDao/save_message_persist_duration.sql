INSERT INTO message_persist_duration(message_type, persist_duration, calls_since_update, migration_request_id)
VALUES (:messageType, :persistDuration, :callsSinceUpdate, :migrationRequestId)
ON CONFLICT ON CONSTRAINT message_type_must_be_unique_for_request
    DO UPDATE SET message_type         = :messageType,
                  persist_duration     = :persistDuration,
                  calls_since_update   = :callsSinceUpdate,
                  migration_request_id = :migrationRequestId;