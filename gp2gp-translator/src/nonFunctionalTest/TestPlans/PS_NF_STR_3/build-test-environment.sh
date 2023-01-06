#!/usr/bin/env bash
set -x -e

docker ps -a -q -f name=gp2gp_translator_nft | xargs -r docker stop
docker ps -a -q -f name=gp2gp_translator_nft | xargs -r docker rm

source ../../../../../docker/vars.sh

docker-compose build gp2gp_translator_nft1 gp2gp_translator_nft2
docker-compose up -d gp2gp_translator_nft1 gp2gp_translator_nft2
