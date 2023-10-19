#!/bin/bash

set -e

export BUILD_TAG=0.14

git fetch
git checkout $BUILD_TAG

cd ..

docker buildx build -f docker/gp2gp-translator/Dockerfile . --platform linux/arm64/v8,linux/amd64 --tag nhsdev/nia-ps-adaptor:${BUILD_TAG} --push
docker buildx build -f docker/gpc-facade/Dockerfile . --platform linux/arm64/v8,linux/amd64 --tag nhsdev/nia-ps-facade:${BUILD_TAG} --push
docker buildx build -f docker/db-migration/Dockerfile . --platform linux/arm64/v8,linux/amd64 --tag nhsdev/nia-ps-db-migration:${BUILD_TAG} --push
docker buildx build -f docker/snomed-schema/Dockerfile . --platform linux/arm64/v8,linux/amd64 --tag nhsdev/nia-ps-snomed-schema:${BUILD_TAG} --push