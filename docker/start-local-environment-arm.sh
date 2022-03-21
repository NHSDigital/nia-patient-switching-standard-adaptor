#!/usr/bin/env bash
set -x -e

source vars.sh

#if [[ "$(docker network ls | grep "ps-network")" == "" ]] ; then
#    docker network create ps-network
#fi
#docker-compose -f docker-compose-arm.yml build ps_db mhs-adaptor-mock activemq;
#docker-compose -f docker-compose-arm.yml up -d ps_db mhs-adaptor-mock activemq;
#
#cd ../db-connector
#./gradlew update
#cd ../docker
#
#docker-compose -f docker-compose-arm.yml build gpc_facade gp2gp_translator;
#docker-compose -f docker-compose-arm.yml up gpc_facade gp2gp_translator;

cd ../snomed-database-loader
./load_release-postgresql.sh /Users/scott.alexander/Downloads/uk_sct2cl_32.10.0_20220216000001Z.zip