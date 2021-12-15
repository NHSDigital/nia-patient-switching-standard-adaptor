#!/bin/bash
set -e
psql --username "postgres" --dbname "postgres" <<-EOSQL
  CREATE USER $PS_DB_OWNER_NAME WITH PASSWORD '$PS_DB_OWNER_PASSWORD' CREATEROLE;
  CREATE DATABASE patient_switching WITH OWNER $PS_DB_OWNER_NAME;
  GRANT ALL PRIVILEGES ON DATABASE patient_switching TO $PS_DB_OWNER_NAME;
EOSQL