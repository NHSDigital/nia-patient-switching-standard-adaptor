#!/usr/bin/env bash

set -e

LIGHT_GREEN='\033[1;32m'
RED='\033[0;31m'
NC='\033[0m'

echo "${LIGHT_GREEN}Exporting environment variables${NC}"
source vars.sh

cd snomed-database-loader
./load_release-postgresql.sh ${SNOMED_FILE_LOCATION}
cd ..
cd snomed-immunization-loader
./load_immunization_codes.sh
cd ..