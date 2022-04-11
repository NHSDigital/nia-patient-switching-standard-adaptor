INSERT INTO patient_migration_request(patient_nhs_number, conversation_id, loosing_practice_ods_code, winning_practice_ods_code)
VALUES (:nhsNumber, :conversationId, :loosingOdsCode, :winningOdsCode);