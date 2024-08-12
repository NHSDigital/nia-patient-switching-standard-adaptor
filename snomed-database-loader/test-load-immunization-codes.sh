#!/bin/bash
set -e;

dbName=patient_switching
snomedCtSchema=snomedct

rootImmunizationCodes=('787859002' '127785005' '304250009' '90351000119108' '713404003')
childImmunizationCodes=('2997511000001102' '308101000000104' '1036721000000101' '1373691000000102' '945831000000105')
immunizationCodesNotInImmunizationHierarchy=('542931000000103' '735981009' '90640007' '571631000119106' '764141000000106' '170399005' )


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

function checkImmunizationCodesAreLoaded() {
  for immunizationCode;
  do
    count=$(psql "${databaseUri}" -t -A -c "SELECT COUNT(DISTINCT concept_and_description_ids) FROM ${snomedCtSchema}.immunization_codes WHERE concept_and_description_ids ='${immunizationCode}'")
    if [ "${count}" != 1 ]
    then
      echo "immunization code not loaded: ${immunizationCode}"
      allCodesLoaded=false
    fi
  done
}

allCodesLoaded=true

echo "Checking immunizationCodes have loaded..."
checkImmunizationCodesAreLoaded "${rootImmunizationCodes[@]}"
checkImmunizationCodesAreLoaded "${childImmunizationCodes[@]}"
checkImmunizationCodesAreLoaded "${immunizationCodesNotInImmunizationHierarchy[@]}"


if [ "${allCodesLoaded}" = false ]
then
  echo "All immunization codes have not loaded successfully"
  exit 1
else
  echo "All immunization codes have loaded successfully"
fi