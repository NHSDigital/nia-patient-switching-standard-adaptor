#!/usr/bin/env bash
set -x

docker-compose stop -t 1 gpc_facade
docker-compose rm --force gpc_facade

source vars.sh

docker-compose up --build --force-recreate --detach gpc_facade
