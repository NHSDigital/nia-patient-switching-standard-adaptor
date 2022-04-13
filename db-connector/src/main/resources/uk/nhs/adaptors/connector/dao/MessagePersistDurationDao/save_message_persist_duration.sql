INSERT INTO message_persist_duration(message_type, persist_duration, calls_since_update)
VALUES (:messageType, :persistDuration, :callsSinceUpdate)
ON CONFLICT ON CONSTRAINT message_type_unique_constraint
DO UPDATE SET message_type = :messageType, persist_duration = :persistDuration, calls_since_update = :callsSinceUpdate;