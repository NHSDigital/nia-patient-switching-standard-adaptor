SELECT *
FROM patient_migration_request
WHERE id IN (SELECT migration_request_id
             FROM (SELECT *,
                          ROW_NUMBER() OVER (PARTITION BY migration_request_id ORDER BY date DESC) AS row
                   FROM migration_status_log) AS a
             WHERE row = 1
               AND status IN (<statusList>));