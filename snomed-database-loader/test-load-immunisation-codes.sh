#!/bin/bash
set -e;

dbName=patient_switching
snomedCtSchema=snomedct

rootImmunisationCodes=('787859002' '127785005' '304250009' '90351000119108' '713404003' '127785005')
childImmunisationCodes=('787859002')
immunisationCodesNotInImmunisationHierarchy=('127785005' '123456')


if [ -z ${PS_DB_OWNER_NAME} ]
then
  echo "Please set the following env var: PS_DB_OWNER_NAME, e.g. \"export PS_DB_OWNER_NAME='postgres'\""
	exit 1
fi

if [ -z ${PS_DB_HOST} ]
then
  echo "Please set the following env var: PS_DB_HOST, e.g. \"export PS_DB_HOST='localhost'\""
	exit 1
fi

if [ -z ${PS_DB_PORT} ]
then
  echo "Please set the following env var: PS_DB_PORT, e.g. \"export PS_DB_PORT='5432'\""
	exit 1
fi

if [ -z ${POSTGRES_PASSWORD} ]
then
  echo "Please set the following env var: POSTGRES_PASSWORD, e.g. \"export POSTGRES_PASSWORD='********'\""
	exit 1
fi

databaseUri="postgresql://${PS_DB_OWNER_NAME}:${POSTGRES_PASSWORD}@${PS_DB_HOST}:${PS_DB_PORT}/${dbName}"

function checkImmunisationCodesAreLoaded() {
  for immunizationCode;
  do
    count=$(psql "${databaseUri}" -t -A -c "SELECT COUNT(conceptId) FROM ${snomedCtSchema}.immunization_codes WHERE conceptId ='${immunizationCode}'")
    if [ "${count}" != 1 ]
    then
      echo "Immunisation code not loaded: ${immunizationCode}"
      allCodesLoaded=false
    fi
  done
}

allCodesLoaded=true

checkImmunisationCodesAreLoaded "${rootImmunisationCodes[@]}"
checkImmunisationCodesAreLoaded "${childImmunisationCodes[@]}"
checkImmunisationCodesAreLoaded "${immunisationCodesNotInImmunisationHierarchy[@]}"

if [ "${allCodesLoaded}" = false ]
then
  echo "All immunisation codes have not loaded successfully"
  exit 1
fi
