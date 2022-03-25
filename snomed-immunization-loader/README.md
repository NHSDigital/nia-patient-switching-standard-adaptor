This script needs to be run after the db has been created in the snomed database loader
## Minimum Specification

- PostgreSQL v.9

## How to run (Mac & Unix)

### Prerequsistes
1. Format codes into CSV with 3 columns (Concept Id,Description,Safety Code)
2. Set the follwing env vars:
- DB_OWNER_USERNAME (database user, needs permissions to create/drop schemas), e.g. `export DB_OWNER_USERNAME='postgres'`
- POSTGRES_PASSWORD (database user password), e.g. `export POSTGRES_PASSWORD='********'`
- DB_HOSTNAME (database host), e.g. `export DB_HOSTNAME='localhost'`
- DB_PORT (databse port), e.g. `export DB_PORT='5432'`

### Run the script
e.g. `load_immunization_codes.sh snomed_immunization_codes.csv`
