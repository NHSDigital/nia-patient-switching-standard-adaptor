#!/bin/bash
set -e
psql --username "postgres" --dbname "postgres" <<-EOSQL
  CREATE USER patient_switching_owner WITH PASSWORD '$PS_DB_OWNER_PASSWORD' CREATEROLE;
  CREATE DATABASE patient_switching WITH OWNER patient_switching_owner;
  GRANT ALL PRIVILEGES ON DATABASE patient_switching TO patient_switching_owner;
EOSQL