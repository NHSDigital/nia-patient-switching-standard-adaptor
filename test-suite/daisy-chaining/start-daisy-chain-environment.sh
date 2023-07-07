#!/usr/bin/env bash

set -x -e

cd ../
source ./vars.sh
cd daisy-chaining
source ./daisy_chaining_vars.sh
source ./vars-versions.sh

docker stop $(docker ps -a -q) && docker rm $(docker ps -a -q)

cd ../mock-spine-mhs-outbound

chmod +x gradlew

./gradlew bootJar

cd ../daisy-chaining


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

if [[ "$(docker network ls | grep "nia-daisy-chain")" == "" ]] ; then
    docker network create nia-daisy-chain
fi

docker-compose up -d activemq redis dynamodb ps_db outbound inbound wiremock mongodb gpcc
docker-compose up db_migration
docker-compose rm -f db_migration

cd ../docker/snomed-database-loader
./load_release-postgresql.sh ${SNOMED_FILE_LOCATION}
cd ../snomed-immunization-loader
./load_immunization_codes.sh
cd ../../daisy-chaining

docker-compose build ps_gp2gp_translator gpc_facade

docker-compose up -d ps_gp2gp_translator gpc_facade mock-spine-mhs gp2gp
