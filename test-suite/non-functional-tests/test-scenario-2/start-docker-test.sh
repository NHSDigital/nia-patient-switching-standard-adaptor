#!/bin/bash

openssl pkcs12 -export -out jmeterkeystore.p12 -inkey ../certs/client.key -in ../certs/client.crt -passout pass:password

echo Removing Docker container if present...
docker compose rm -s -f name=nf-test-2

docker compose build nf-test-2 
docker compose up nf-test-2
dockerContainer=$(docker ps -a -q -f name=nf-test-2)
docker cp "$dockerContainer":/jmeter-results/jmeter.log .
echo Log copied to ./jmeter.log
echo Removing Docker container:
docker rm "$dockerContainer"