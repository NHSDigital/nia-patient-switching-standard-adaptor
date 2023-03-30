SELECT * 
FROM patient_migration_request
WHERE patient_nhs_number = :patientNhsNumber
ORDER BY id desc 
LIMIT 1