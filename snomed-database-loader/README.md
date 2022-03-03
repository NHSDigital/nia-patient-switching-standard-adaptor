# SNOMED CT DATABASE

PostgreSQL SQL Scripts to create and populate a PostgreSQL database with a SNOMED CT terminology release.

**NOTE:** This script is based on [IHTSDO/snomed-database-loader](https://github.com/IHTSDO/snomed-database-loader/tree/master/PostgreSQL)

Script does not load full SNOMED DB - only the subset required by PS Adaptor.

## Minimum Specification

- PostgreSQL v.9

## How to run (Mac & Unix)

### Prerequsistes
1. Download SNOMED CT Release 
2. Set the follwing env vars:
- DB_OWNER_USERNAME (database user, needs permissions to create/drop schemas), e.g. `export DB_OWNER_USERNAME='postgres'`
- PGPASSWORD (database user password), e.g. `export PGPASSWORD='********'`
- DB_HOSTNAME (database host), e.g. `export DB_HOSTNAME='localhost'`
- DB_PORT (databse port), e.g. `export DB_PORT='5432'`

### Run the script
e.g. `load_release-postgresql.sh ~/Downloads/uk_sct2cl_32.0.0_20210512000001Z.zip`
