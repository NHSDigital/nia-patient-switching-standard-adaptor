#!/bin/bash

set -e

export BUILD_TAG=1.1.0

git fetch
git checkout $BUILD_TAG

cd ..

export BASE_GIT_URL="https://github.com/NHSDigital/nia-patient-switching-standard-adaptor/blob/${BUILD_TAG}/"

function build() {
  docker buildx build -f ${1} . --platform linux/arm64/v8,linux/amd64 --tag ${2}:${BUILD_TAG} --label "org.opencontainers.image.source=${BASE_GIT_URL}${1}" --push
}

build docker/gp2gp-translator/Dockerfile nhsdev/nia-ps-adaptor
build docker/gpc-facade/Dockerfile nhsdev/nia-ps-facade
build docker/db-migration/Dockerfile nhsdev/nia-ps-db-migration
build docker/snomed-schema/Dockerfile nhsdev/nia-ps-snomed-schema
