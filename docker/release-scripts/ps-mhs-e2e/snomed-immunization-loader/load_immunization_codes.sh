#!/bin/bash
set -e;

basedir="$(pwd)/snomed_immunization_codes.csv"
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
  echo "Please set the following env var: PS_DB_HOST, e.g. \"export PS_DB_HOST='5436'\""
	exit -1
fi

if [ -z ${POSTGRES_PASSWORD} ]
then
  echo "Please set the following env var: POSTGRES_PASSWORD, e.g. \"export POSTGRES_PASSWORD='********'\""
	exit -1
fi

databaseUri="postgresql://${PS_DB_OWNER_NAME}:${POSTGRES_PASSWORD}@${PS_DB_HOST}:${PS_DB_PORT}/${dbName}"
echo 'databaseuri'
echo ${databaseUri}

echo 'snowmedctschma'
echo ${snomedCtSchema}

echo ${databaseUri} -c "\copy ${snomedCtSchema}.immunization_codes (conceptid, description, safetycode) FROM '${basedir}/$1' DELIMITER ',' CSV HEADER QUOTE '\"'"
psql ${databaseUri} -c "\copy ${snomedCtSchema}.immunization_codes (conceptid, description, safetycode) FROM '${basedir}/$1' DELIMITER ',' CSV HEADER QUOTE '\"'"