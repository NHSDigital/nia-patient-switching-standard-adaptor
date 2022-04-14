UPDATE patient_attachment_log
SET
    deleted = true
    FROM patient_attachment_log AS PAL
INNER JOIN patient_migration_request PMR ON PMR.id = PAL.patient_migration_req_id
WHERE PMR.conversation_id = :conversation_id AND PAL.mid = :mid