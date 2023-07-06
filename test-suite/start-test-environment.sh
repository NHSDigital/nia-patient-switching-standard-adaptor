#!/usr/bin/env bash

set -x -e

source ./vars.sh

docker stop $(docker ps -a -q) && docker rm $(docker ps -a -q)

cd mock-spine-mhs-outbound

chmod +x gradlew

./gradlew bootJar

cd ..

echo "Exporting environment variables"

if [[ -z "${MHS_SECRET_PARTY_KEY}" ]]; then
  echo "Secret key not set for MHS_SECRET_PARTY_KEY"
  exit 1
elif [[ -z "${MHS_SECRET_CLIENT_CERT}" ]]; then
  echo "Secret key not set for MHS_SECRET_CLIENT_CERT"
  exit 1
elif [[ -z "${MHS_SECRET_CLIENT_KEY}" ]]; then
  echo "Secret key not set for MHS_SECRET_CLIENT_KEY"
  exit 1
elif [[ -z "${MHS_SECRET_CA_CERTS}" ]]; then
  echo "Secret key not set for MHS_SECRET_CA_CERTS"
  exit 1
fi

echo "Running containers"

if [[ "$(docker network ls | grep "nia-ps")" == "" ]] ; then
    docker network create nia-ps
fi

docker-compose -f docker-compose.yml up -d activemq redis dynamodb ps_db outbound inbound
docker-compose -f docker-compose.yml up db_migration
docker-compose -f docker-compose.yml rm -f db_migration

cd docker/snomed-database-loader
./load_release-postgresql.sh ${SNOMED_FILE_LOCATION}
cd ../..
cd docker/snomed-immunization-loader
./load_immunization_codes.sh
cd ../..

docker-compose -f docker-compose.yml up -d ps_gp2gp_translator gpc_facade mock-spine-mhs
