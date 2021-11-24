#!/usr/bin/env bash
set -x

docker-compose stop -t 1 gp2gp_translator
docker-compose rm --force gp2gp_translator
docker-compose up --build --force-recreate --detach gp2gp_translator
