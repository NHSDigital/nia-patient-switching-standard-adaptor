UPDATE patient_attachment_log SET
    deleted = true
WHERE filename = :filename;