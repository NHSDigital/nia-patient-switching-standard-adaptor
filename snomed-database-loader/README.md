# SNOMED CT DATABASE

PostgreSQL SQL Scripts to create and populate a PostgreSQL database with a SNOMED CT terminology release.

**NOTE:** This script is based on [IHTSDO/snomed-database-loader](https://github.com/IHTSDO/snomed-database-loader/tree/master/PostgreSQL)

Script does not load full SNOMED DB - only the subset required by PS Adaptor.

## Minimum Specification

- PostgreSQL v.9

## How to run (Mac & Unix)

### Prerequsistes
1. Download SNOMED CT Release 
2. Set the following env vars:
- PS_DB_OWNER_NAME (database user, needs permissions to create/drop schemas), e.g. `export PS_DB_OWNER_NAME='postgres'`
- PGPASSWORD (database user password), e.g. `export PGPASSWORD='********'`
- PS_DB_HOST (database host), e.g. `export PS_DB_HOST='localhost'`
- PS_DB_PORT (database port), e.g. `export PS_DB_PORT='5432'`

### Run the script
e.g. `load_release-postgresql.sh ~/Downloads/uk_sct2cl_32.0.0_20210512000001Z.zip`
