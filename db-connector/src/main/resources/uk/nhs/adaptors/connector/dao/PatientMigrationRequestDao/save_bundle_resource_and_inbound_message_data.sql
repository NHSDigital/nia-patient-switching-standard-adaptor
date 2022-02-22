UPDATE patient_migration_request SET bundle_resource = :bundle, inbound_message = :inboundMessage
WHERE conversation_id = :conversationId;
