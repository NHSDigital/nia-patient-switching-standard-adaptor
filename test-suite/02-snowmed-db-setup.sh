#!/usr/bin/env bash

set -e

LIGHT_GREEN='\033[1;32m'
RED='\033[0;31m'
NC='\033[0m'

echo "${LIGHT_GREEN}Exporting environment variables${NC}"
source vars.sh

cd docker/snowmed-database-loader
./load_release-postgresql.sh ${SNOWMED_FILE_LOCATION}
cd ../..
cd docker/snomed-immunization-loader
./load_immunization_codes.sh
cd ../..


docker-compose -f docker-compose-part-2.yml up -d