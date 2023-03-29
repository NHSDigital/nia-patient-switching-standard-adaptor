SELECT conversationId 
FROM patient_migration_request
WHERE patient_nhs_number = :patient_nhs_number
ORDER BY id desc 
LIMIT 1