UPDATE patient_attachment_log SET
    parent_mid = COALESCE(:parent_mid, parent_mid),
    content_type = COALESCE(:content_type, content_type),
    compressed = COALESCE(:compressed, compressed),
    large_attachment = COALESCE(:large_attachment, large_attachment),
    base64 = COALESCE(:base64, base64),
    skeleton = COALESCE(:skeleton, skeleton),
    uploaded = COALESCE(:uploaded, uploaded),
    length_num = COALESCE(:length_num, length_num),
    order_num = COALESCE(:order_num, order_num)
WHERE mid = :mid AND filename = :filename;