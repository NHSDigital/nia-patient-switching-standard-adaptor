#!/usr/bin/env bash

set -x -e

source ./vars.sh

docker stop $(docker ps -a -q) && docker rm $(docker ps -a -q)

cd mock-spine-mhs-outbound

chmod +x gradlew

./gradlew bootJar

cd ..


LIGHT_GREEN='\033[1;32m'
RED='\033[0;31m'
NC='\033[0m'

echo "${LIGHT_GREEN}Exporting environment variables${NC}"

if [[ -z "${MHS_SECRET_PARTY_KEY}" ]]; then
  echo "${RED}Secret key not set for MHS_SECRET_PARTY_KEY${NC}"
  exit 1
elif [[ -z "${MHS_SECRET_CLIENT_CERT}" ]]; then
  echo "${RED}Secret key not set for MHS_SECRET_CLIENT_CERT${NC}"
  exit 1
elif [[ -z "${MHS_SECRET_CLIENT_KEY}" ]]; then
  echo "${RED}Secret key not set for MHS_SECRET_CLIENT_KEY${NC}"
  exit 1
elif [[ -z "${MHS_SECRET_CA_CERTS}" ]]; then
  echo "${RED}Secret key not set for MHS_SECRET_CA_CERTS${NC}"
  exit 1
fi

echo "${LIGHT_GREEN}Running containers${NC}"

if [[ "$(docker network ls | grep "nia-ps")" == "" ]] ; then
    docker network create nia-ps
fi

docker-compose -f docker-compose-amd.yml up -d activemq redis dynamodb ps_db outbound inbound
docker-compose -f docker-compose-amd.yml up db_migration
docker-compose -f docker-compose-amd.yml rm -f db_migration

cd docker/snomed-database-loader
./load_release-postgresql.sh ${SNOMED_FILE_LOCATION}
cd ../..
cd docker/snomed-immunization-loader
./load_immunization_codes.sh
cd ../..

docker-compose -f docker-compose-amd.yml up -d ps_gp2gp_translator gpc_facade mock-spine-mhs
