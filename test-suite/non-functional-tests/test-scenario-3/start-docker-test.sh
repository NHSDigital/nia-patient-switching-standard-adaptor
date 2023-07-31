#!/bin/bash

openssl pkcs12 -export -out jmeterkeystore.p12 -inkey ../certs/client.key -in ../certs/client.crt -passout pass:password

echo Stopping docker container:
docker ps -a -q -f name=nf-test-3 | xargs -r docker stop
echo Removing Docker container:
docker ps -a -q -f name=nf-test-3 | xargs -r docker rm

docker compose build nf-test-3 
docker compose up nf-test-3
dockerContainer=$(docker ps -a -q -f name=nf-test-3)
docker cp "$dockerContainer":/jmeter-results/jmeter.log .
echo Log copied to ./jmeter.log
echo Removing Docker container:
docker rm "$dockerContainer"