SELECT PAL.* FROM patient_attachment_log AS PAL
INNER JOIN patient_migration_request PMR ON PMR.id = PAL.patient_migration_req_id
WHERE PMR.conversation_id = :conversationId AND PAL.parent_mid = :parent_mid