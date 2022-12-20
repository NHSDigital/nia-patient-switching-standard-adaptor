SELECT CASE WHEN EXISTS(
    SELECT * FROM patient_migration_request WHERE conversation_id = :conversationId
)
    THEN TRUE
    ELSE FALSE
END;