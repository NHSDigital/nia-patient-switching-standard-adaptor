UPDATE patient_attachment_log
SET
    parent_mid = COALESCE(:parent_mid, PAL.parent_mid),
    content_type = COALESCE(:content_type, PAL.content_type),
    compressed = COALESCE(:compressed, PAL.compressed),
    large_attachment = COALESCE(:large_attachment, PAL.large_attachment),
    base64 = COALESCE(:base64, PAL.base64),
    skeleton = COALESCE(:skeleton, PAL.skeleton),
    uploaded = COALESCE(:uploaded, PAL.uploaded),
    length_num = COALESCE(:length_num, PAL.length_num),
    order_num = COALESCE(:order_num, PAL.order_num)
    FROM patient_attachment_log AS PAL
INNER JOIN patient_migration_request PMR ON PMR.id = PAL.patient_migration_req_id
WHERE PMR.conversation_id = :conversation_id AND PAL.mid = :mid