INSERT INTO patient_migration_request(patient_nhs_number, conversation_id, losing_practice_ods_code, winning_practice_ods_code)
VALUES (:nhsNumber, :conversationId, :losingOdsCode, :winningOdsCode);

