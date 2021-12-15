#!/usr/bin/env bash
set -x -e

source vars.sh

if [[ "$(docker network ls | grep "pss-network")" == "" ]] ; then
    docker network create pss-network
fi
docker-compose build ps_db mhs-adaptor-mock activemq;
docker-compose up -d ps_db mhs-adaptor-mock activemq;

cd ../db-connector
./gradlew update
cd ../docker

docker-compose build gpc_facade gp2gp_translator;
docker-compose up gpc_facade gp2gp_translator;
