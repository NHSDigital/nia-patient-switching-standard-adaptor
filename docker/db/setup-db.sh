#!/bin/bash
set -e
psql --username "postgres" --dbname "postgres" <<-EOSQL
  CREATE USER $PSS_DB_OWNER_NAME WITH PASSWORD '$PSS_DB_OWNER_PASSWORD' CREATEROLE;
  CREATE DATABASE patient_switching WITH OWNER $PSS_DB_OWNER_NAME;
  GRANT ALL PRIVILEGES ON DATABASE patient_switching TO $PSS_DB_OWNER_NAME;
EOSQL