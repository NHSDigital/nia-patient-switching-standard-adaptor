#!/usr/bin/env bash
set -x -e

source vars.sh

docker-compose build pss_db mhs-adaptor-mock activemq;
docker-compose up -d pss_db mhs-adaptor-mock activemq;

cd ../db-connector
./gradlew update
cd ../docker

docker-compose build gpc_facade gp2gp_translator;
docker-compose up gpc_facade gp2gp_translator;
