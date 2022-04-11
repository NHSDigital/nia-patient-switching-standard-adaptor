UPDATE patient_attachment_log SET uploaded = :uploaded, inbound_message = :inboundMessage, filename = :filename
WHERE mid = :mid;