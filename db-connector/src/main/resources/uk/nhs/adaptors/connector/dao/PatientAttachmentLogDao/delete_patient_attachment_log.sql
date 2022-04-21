UPDATE patient_attachment_log AS PAL
SET
    deleted = true
FROM patient_migration_request AS PMR
WHERE PMR.conversation_id = :conversationId AND PAL.mid = :mid