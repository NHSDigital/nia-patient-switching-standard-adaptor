UPDATE patient_attachment_log
SET
    parent_mid = COALESCE(:parentMid, PAL.parent_mid),
    content_type = COALESCE(:contentType, PAL.content_type),
    compressed = COALESCE(:compressed, PAL.compressed),
    large_attachment = COALESCE(:largeAttachment, PAL.large_attachment),
    base64 = COALESCE(:base64, PAL.base64),
    skeleton = COALESCE(:skeleton, PAL.skeleton),
    uploaded = COALESCE(:uploaded, PAL.uploaded),
    length_num = COALESCE(:lengthNum, PAL.length_num),
    order_num = COALESCE(:orderNum, PAL.order_num)
    FROM patient_attachment_log AS PAL
INNER JOIN patient_migration_request PMR ON PMR.id = PAL.patient_migration_req_id
WHERE PMR.conversation_id = :conversationId AND PAL.mid = :mid