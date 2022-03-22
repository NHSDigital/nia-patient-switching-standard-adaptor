#!/bin/bash
set -e;

basedir="$(pwd)"
dbName=patient_switching
snomedCtSchema=snomedct

if [ -z ${PS_DB_OWNER_NAME} ]
then
  echo "Please set the following env var: PS_DB_OWNER_NAME, e.g. \"export PS_DB_OWNER_NAME='postgres'\""
	exit -1
fi

if [ -z ${PS_DB_HOST} ]
then
  echo "Please set the following env var: DB_HOSTNAME, e.g. \"export DB_HOSTNAME='localhost'\""
	exit -1
fi

if [ -z ${PS_DB_PORT} ]
then
  echo "Please set the following env var: DB_PORT, e.g. \"export DB_PORT='5432'\""
	exit -1
fi

if [ -z ${PS_DB_OWNER_PASSWORD} ]
then
  echo "Please set the following env var: PGPASSWORD, e.g. \"export PGPASSWORD='********'\""
	exit -1
fi

psql -h ${PS_DB_HOST} -p ${PS_DB_PORT} -d ${dbName} -U ${PS_DB_OWNER_NAME} -c "\copy ${snomedCtSchema}.immunization_codes (conceptid, description, safetycode) FROM '${basedir}/$1' DELIMITER ',' CSV HEADER QUOTE '\"'"