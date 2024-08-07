UPDATE patient_attachment_log AS PAL
SET
    parent_mid = COALESCE(:parentMid, PAL.parent_mid),
    content_type = COALESCE(:contentType, PAL.content_type),
    compressed = COALESCE(:compressed, PAL.compressed),
    large_attachment = COALESCE(:largeAttachment, PAL.large_attachment),
    original_base64 = COALESCE(:originalBase64, PAL.original_base64),
    skeleton = COALESCE(:skeleton, PAL.skeleton),
    uploaded = COALESCE(:uploaded, PAL.uploaded),
    length_num = COALESCE(:lengthNum, PAL.length_num),
    order_num = COALESCE(:orderNum, PAL.order_num),
    post_processed_length_num = COALESCE(:postProcessedLengthNum, PAL.post_processed_length_num),
    base64 = COALESCE(:isBase64, PAL.base64)
FROM patient_migration_request AS PMR
WHERE PMR.conversation_id = :conversationId AND PAL.mid = :mid