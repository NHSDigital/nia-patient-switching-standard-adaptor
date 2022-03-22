#!/usr/bin/env bash
set -x -e

source vars.sh

if [[ "$(docker network ls | grep "ps-network")" == "" ]] ; then
    docker network create ps-network
fi
docker-compose build ps_db mhs-adaptor-mock activemq;
docker-compose up -d ps_db mhs-adaptor-mock activemq;

cd ../db-connector
./gradlew update
